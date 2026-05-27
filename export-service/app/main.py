from datetime import datetime
from typing import Annotated
from uuid import uuid4

from fastapi import Depends, FastAPI, Header, HTTPException, Response, status
from fastapi.responses import JSONResponse

from app.config import get_settings
from app.csv_writer import render_product_csv
from app.job_store import job_store
from app.models import ProductExportRequest
from app.product_client import ProductClient
from app.tasks import export_csv

app = FastAPI(title="NEKI Export Service")


def require_admin(
    x_user_email: Annotated[str | None, Header(alias="X-User-Email")] = None,
    x_user_roles: Annotated[str | None, Header(alias="X-User-Roles")] = None,
) -> str:
    if not x_user_email:
        raise HTTPException(status_code=status.HTTP_401_UNAUTHORIZED, detail="Missing user email")

    roles = {
        role.strip().upper().removeprefix("ROLE_")
        for role in (x_user_roles or "").split(",")
        if role.strip()
    }
    if "ADMIN" not in roles:
        raise HTTPException(status_code=status.HTTP_403_FORBIDDEN, detail="Admin role required")

    return x_user_email


@app.get("/health")
async def health() -> dict[str, str]:
    return {"status": "ok"}


@app.post("/api/v1/admin/exports/products")
async def export_products(
    request: ProductExportRequest,
    requester_email: Annotated[str, Depends(require_admin)],
):
    settings = get_settings()
    product_client = ProductClient()
    row_count = await product_client.count_products(
        request.filters,
        request.sort_by,
        request.sort_direction,
    )

    if row_count <= settings.direct_export_max_rows:
        page = await product_client.fetch_products_page(
            request.filters,
            request.sort_by,
            request.sort_direction,
            page=0,
            size=max(row_count, 1),
        )
        csv_bytes = render_product_csv(page.get("content", []))
        filename = f"products-export-{datetime.now():%Y%m%d-%H%M%S}.csv"
        return Response(
            content=csv_bytes,
            media_type="text/csv; charset=utf-8",
            headers={"Content-Disposition": f'attachment; filename="{filename}"'},
        )

    job_id = str(uuid4())
    job_store.create(
        job_id,
        {
            "status": "QUEUED",
            "mailStatus": None,
            "requesterEmail": requester_email,
            "rowCount": row_count,
            "filters": request.filters,
            "sortBy": request.sort_by,
            "sortDirection": request.sort_direction,
        },
    )
    export_csv.apply_async(
        args=[
            job_id,
            requester_email,
            request.filters,
            request.sort_by,
            request.sort_direction,
            row_count,
        ],
        queue="exports",
    )

    return JSONResponse(
        status_code=status.HTTP_202_ACCEPTED,
        content={
            "jobId": job_id,
            "status": "QUEUED",
            "rowCount": row_count,
            "message": "Export is being prepared and will be emailed when ready.",
        },
    )


@app.get("/api/v1/admin/exports/{job_id}")
async def get_export_status(
    job_id: str,
    requester_email: Annotated[str, Depends(require_admin)],
):
    job = job_store.get(job_id)
    if job is None or job.get("requesterEmail") != requester_email:
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="Export job not found")
    return job

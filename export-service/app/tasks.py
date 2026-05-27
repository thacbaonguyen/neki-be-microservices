import os
import tempfile
from datetime import datetime, timezone
from typing import Any

from app.celery_app import celery_app
from app.config import get_settings
from app.csv_writer import ProductCsvFileWriter
from app.job_store import job_store
from app.mailer import send_export_ready_email
from app.product_client import ProductClient
from app.r2_storage import R2Storage


def _short_error(exc: Exception) -> str:
    message = str(exc)
    return message[:500] if message else exc.__class__.__name__


@celery_app.task(bind=True, name="export_csv", max_retries=3, default_retry_delay=5)
def export_csv(
    self,
    job_id: str,
    requester_email: str,
    filters: dict[str, Any],
    sort_by: str | None,
    sort_direction: str | None,
    row_count: int,
) -> None:
    try:
        settings = get_settings()
        job_store.update(job_id, status="PROCESSING", error=None)

        client = ProductClient()
        storage = R2Storage()
        now = datetime.now(timezone.utc)
        key = f"exports/products/{now:%Y/%m}/{job_id}.csv"

        temp_file = tempfile.NamedTemporaryFile(prefix=f"{job_id}-", suffix=".csv", delete=False)
        temp_file_path = temp_file.name
        temp_file.close()

        try:
            with ProductCsvFileWriter(temp_file_path) as writer:
                page = 0
                while True:
                    product_page = client.fetch_products_page_sync(
                        filters,
                        sort_by,
                        sort_direction,
                        page,
                        settings.product_export_page_size,
                    )
                    rows = product_page.get("content", [])
                    writer.write_rows(rows)

                    if product_page.get("last", True):
                        break
                    page += 1

            presigned_url, expires_at = storage.upload_and_presign(temp_file_path, key)
        finally:
            if os.path.exists(temp_file_path):
                os.remove(temp_file_path)

        job_store.update(
            job_id,
            status="DONE",
            mailStatus="PENDING",
            r2Key=key,
            fileUrl=presigned_url,
            fileExpiresAt=expires_at,
            rowCount=row_count,
        )
        send_mail.apply_async(
            args=[job_id, requester_email, presigned_url, expires_at, row_count],
            queue="exports",
        )
    except Exception as exc:
        if self.request.retries >= self.max_retries:
            job_store.update(job_id, status="FAILED", error=_short_error(exc))
            raise
        raise self.retry(exc=exc, countdown=5)


@celery_app.task(bind=True, name="send_mail", max_retries=3, default_retry_delay=5)
def send_mail(
    self,
    job_id: str,
    to_email: str,
    presigned_url: str,
    expires_at: str,
    row_count: int,
) -> None:
    try:
        send_export_ready_email(to_email, presigned_url, expires_at, row_count)
        job_store.update(job_id, mailStatus="SENT", mailError=None)
    except Exception as exc:
        if self.request.retries >= self.max_retries:
            job_store.update(job_id, mailStatus="FAILED", mailError=_short_error(exc))
            raise
        raise self.retry(exc=exc, countdown=5)

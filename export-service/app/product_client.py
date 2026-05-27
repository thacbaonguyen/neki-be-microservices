from typing import Any

import httpx

from app.config import get_settings


class ProductClient:
    def __init__(self) -> None:
        self.settings = get_settings()
        self.base_url = self.settings.product_service_url.rstrip("/")

    def _headers(self) -> dict[str, str]:
        return {"X-Internal-Token": self.settings.internal_api_token}

    def _payload(
        self,
        filters: dict[str, Any],
        sort_by: str | None,
        sort_direction: str | None,
        page: int | None = None,
        size: int | None = None,
    ) -> dict[str, Any]:
        payload: dict[str, Any] = {
            "filters": filters or {},
            "sortBy": sort_by,
            "sortDirection": sort_direction,
        }
        if page is not None:
            payload["page"] = page
        if size is not None:
            payload["size"] = size
        return payload

    @staticmethod
    def _unwrap(response: httpx.Response) -> Any:
        response.raise_for_status()
        body = response.json()
        return body.get("data", body)

    async def count_products(
        self,
        filters: dict[str, Any],
        sort_by: str | None,
        sort_direction: str | None,
    ) -> int:
        async with httpx.AsyncClient(timeout=self.settings.http_timeout_seconds) as client:
            response = await client.post(
                f"{self.base_url}/internal/products/export/count",
                headers=self._headers(),
                json=self._payload(filters, sort_by, sort_direction),
            )
        data = self._unwrap(response)
        return int(data.get("rowCount", 0))

    async def fetch_products_page(
        self,
        filters: dict[str, Any],
        sort_by: str | None,
        sort_direction: str | None,
        page: int,
        size: int,
    ) -> dict[str, Any]:
        async with httpx.AsyncClient(timeout=self.settings.http_timeout_seconds) as client:
            response = await client.post(
                f"{self.base_url}/internal/products/export/page",
                headers=self._headers(),
                json=self._payload(filters, sort_by, sort_direction, page, size),
            )
        return self._unwrap(response)

    def fetch_products_page_sync(
        self,
        filters: dict[str, Any],
        sort_by: str | None,
        sort_direction: str | None,
        page: int,
        size: int,
    ) -> dict[str, Any]:
        with httpx.Client(timeout=self.settings.http_timeout_seconds) as client:
            response = client.post(
                f"{self.base_url}/internal/products/export/page",
                headers=self._headers(),
                json=self._payload(filters, sort_by, sort_direction, page, size),
            )
        return self._unwrap(response)

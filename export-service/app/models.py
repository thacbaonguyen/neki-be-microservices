from typing import Any

from pydantic import BaseModel, ConfigDict, Field


class ProductExportRequest(BaseModel):
    model_config = ConfigDict(populate_by_name=True)

    filters: dict[str, Any] = Field(default_factory=dict)
    sort_by: str | None = Field(None, alias="sortBy")
    sort_direction: str | None = Field(None, alias="sortDirection")

    def product_payload(self, page: int | None = None, size: int | None = None) -> dict[str, Any]:
        payload: dict[str, Any] = {
            "filters": self.filters,
            "sortBy": self.sort_by,
            "sortDirection": self.sort_direction,
        }
        if page is not None:
            payload["page"] = page
        if size is not None:
            payload["size"] = size
        return payload

import csv
import io
from typing import Any, Iterable

CSV_COLUMNS = [
    "id",
    "name",
    "slug",
    "categoryName",
    "subCategoryName",
    "brandName",
    "basePrice",
    "salePrice",
    "currentPrice",
    "gender",
    "isFeatured",
    "isNew",
    "isActive",
    "isOnSale",
    "inStock",
    "totalSold",
    "averageRating",
    "reviewCount",
    "createdAt",
    "updatedAt",
    "primaryImage",
]

CSV_INJECTION_PREFIXES = ("=", "+", "-", "@")


def sanitize_csv_cell(value: Any) -> Any:
    if value is None:
        return ""
    if isinstance(value, bool):
        return "true" if value else "false"
    text = str(value)
    if text.startswith(CSV_INJECTION_PREFIXES):
        return "'" + text
    return text


def render_product_csv(rows: Iterable[dict[str, Any]]) -> bytes:
    buffer = io.StringIO(newline="")
    writer = csv.DictWriter(buffer, fieldnames=CSV_COLUMNS, extrasaction="ignore")
    writer.writeheader()
    for row in rows:
        writer.writerow({column: sanitize_csv_cell(row.get(column)) for column in CSV_COLUMNS})
    return buffer.getvalue().encode("utf-8-sig")


class ProductCsvFileWriter:
    def __init__(self, file_path: str) -> None:
        self.file_path = file_path
        self.file = open(file_path, "w", newline="", encoding="utf-8-sig")
        self.writer = csv.DictWriter(self.file, fieldnames=CSV_COLUMNS, extrasaction="ignore")
        self.writer.writeheader()

    def write_rows(self, rows: Iterable[dict[str, Any]]) -> None:
        for row in rows:
            self.writer.writerow({column: sanitize_csv_cell(row.get(column)) for column in CSV_COLUMNS})

    def close(self) -> None:
        self.file.close()

    def __enter__(self) -> "ProductCsvFileWriter":
        return self

    def __exit__(self, exc_type, exc, traceback) -> None:
        self.close()

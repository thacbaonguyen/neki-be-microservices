from functools import lru_cache

from pydantic import Field
from pydantic_settings import BaseSettings, SettingsConfigDict


class Settings(BaseSettings):
    model_config = SettingsConfigDict(case_sensitive=False)

    app_name: str = Field("NEKI E-Commerce", validation_alias="APP_NAME")
    redis_url: str = Field("redis://redis:6379/1", validation_alias="REDIS_URL")
    product_service_url: str = Field("http://product-service:8082", validation_alias="PRODUCT_SERVICE_URL")
    internal_api_token: str = Field(..., validation_alias="INTERNAL_API_TOKEN")

    r2_access_key: str = Field(..., validation_alias="R2_ACCESS_KEY")
    r2_secret_key: str = Field(..., validation_alias="R2_SECRET_KEY")
    r2_endpoint: str = Field(..., validation_alias="R2_ENDPOINT")
    r2_bucket: str = Field(..., validation_alias="R2_BUCKET")
    r2_region: str = Field("auto", validation_alias="R2_REGION")
    r2_presign_expires_seconds: int = Field(604800, validation_alias="R2_PRESIGN_EXPIRES_SECONDS")

    ses_smtp_host: str = Field(..., validation_alias="SES_SMTP_HOST")
    ses_smtp_port: int = Field(587, validation_alias="SES_SMTP_PORT")
    ses_smtp_user: str = Field(..., validation_alias="SES_SMTP_USER")
    ses_smtp_pass: str = Field(..., validation_alias="SES_SMTP_PASS")
    smtp_secure: bool = Field(False, validation_alias="SMTP_SECURE")
    mail_from: str = Field(..., validation_alias="MAIL_FROM")

    export_job_ttl_seconds: int = Field(691200, validation_alias="EXPORT_JOB_TTL_SECONDS")
    product_export_page_size: int = Field(500, validation_alias="PRODUCT_EXPORT_PAGE_SIZE")
    direct_export_max_rows: int = Field(100, validation_alias="DIRECT_EXPORT_MAX_ROWS")
    http_timeout_seconds: float = Field(30.0, validation_alias="HTTP_TIMEOUT_SECONDS")


@lru_cache
def get_settings() -> Settings:
    return Settings()

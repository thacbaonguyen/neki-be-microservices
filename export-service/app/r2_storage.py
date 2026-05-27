from datetime import datetime, timedelta, timezone

import boto3
from botocore.config import Config

from app.config import get_settings


class R2Storage:
    def __init__(self) -> None:
        self.settings = get_settings()
        self.client = boto3.client(
            "s3",
            endpoint_url=self.settings.r2_endpoint,
            aws_access_key_id=self.settings.r2_access_key,
            aws_secret_access_key=self.settings.r2_secret_key,
            region_name=self.settings.r2_region,
            config=Config(signature_version="s3v4"),
        )

    def upload_and_presign(self, file_path: str, key: str) -> tuple[str, str]:
        self.client.upload_file(
            file_path,
            self.settings.r2_bucket,
            key,
            ExtraArgs={"ContentType": "text/csv; charset=utf-8"},
        )
        url = self.client.generate_presigned_url(
            "get_object",
            Params={"Bucket": self.settings.r2_bucket, "Key": key},
            ExpiresIn=self.settings.r2_presign_expires_seconds,
        )
        expires_at = datetime.now(timezone.utc) + timedelta(
            seconds=self.settings.r2_presign_expires_seconds
        )
        return url, expires_at.isoformat()

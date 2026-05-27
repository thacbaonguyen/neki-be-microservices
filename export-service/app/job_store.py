import json
from datetime import datetime, timezone
from typing import Any

import redis

from app.config import get_settings


def _now_iso() -> str:
    return datetime.now(timezone.utc).isoformat()


class JobStore:
    def __init__(self) -> None:
        self.settings = get_settings()
        self.redis = redis.Redis.from_url(self.settings.redis_url, decode_responses=True)

    def key(self, job_id: str) -> str:
        return f"export_job:{job_id}"

    def create(self, job_id: str, data: dict[str, Any]) -> dict[str, Any]:
        payload = {
            "jobId": job_id,
            "createdAt": _now_iso(),
            "updatedAt": _now_iso(),
            **data,
        }
        self.redis.setex(
            self.key(job_id),
            self.settings.export_job_ttl_seconds,
            json.dumps(payload, ensure_ascii=False),
        )
        return payload

    def get(self, job_id: str) -> dict[str, Any] | None:
        raw = self.redis.get(self.key(job_id))
        if raw is None:
            return None
        return json.loads(raw)

    def update(self, job_id: str, **fields: Any) -> dict[str, Any]:
        payload = self.get(job_id) or {"jobId": job_id, "createdAt": _now_iso()}
        payload.update(fields)
        payload["updatedAt"] = _now_iso()
        self.redis.setex(
            self.key(job_id),
            self.settings.export_job_ttl_seconds,
            json.dumps(payload, ensure_ascii=False),
        )
        return payload


job_store = JobStore()

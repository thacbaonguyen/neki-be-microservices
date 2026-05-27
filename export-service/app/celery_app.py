from celery import Celery

from app.config import get_settings

settings = get_settings()

celery_app = Celery(
    "export_service",
    broker=settings.redis_url,
    backend=settings.redis_url,
    include=["app.tasks"],
)

celery_app.conf.update(
    task_default_queue="exports",
    task_routes={
        "export_csv": {"queue": "exports"},
        "send_mail": {"queue": "exports"},
    },
    task_acks_late=True,
    worker_prefetch_multiplier=1,
    result_expires=settings.export_job_ttl_seconds,
)

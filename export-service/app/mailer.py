import smtplib
import ssl
from email.message import EmailMessage

from app.config import get_settings


def send_export_ready_email(to_email: str, presigned_url: str, expires_at: str, row_count: int) -> None:
    settings = get_settings()
    subject = "NEKI product export is ready"
    plain = (
        f"Your product export is ready.\n\n"
        f"Rows: {row_count}\n"
        f"Download: {presigned_url}\n"
        f"Link expires at: {expires_at}\n"
    )
    html = f"""
    <p>Your product export is ready.</p>
    <p><strong>Rows:</strong> {row_count}</p>
    <p><a href="{presigned_url}">Download CSV</a></p>
    <p>This link expires at {expires_at}.</p>
    """

    message = EmailMessage()
    message["From"] = settings.mail_from
    message["To"] = to_email
    message["Subject"] = subject
    message.set_content(plain)
    message.add_alternative(html, subtype="html")

    if settings.smtp_secure:
        with smtplib.SMTP_SSL(settings.ses_smtp_host, settings.ses_smtp_port) as smtp:
            smtp.login(settings.ses_smtp_user, settings.ses_smtp_pass)
            smtp.send_message(message)
        return

    with smtplib.SMTP(settings.ses_smtp_host, settings.ses_smtp_port) as smtp:
        smtp.starttls(context=ssl.create_default_context())
        smtp.login(settings.ses_smtp_user, settings.ses_smtp_pass)
        smtp.send_message(message)

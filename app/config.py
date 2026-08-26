import os
from pathlib import Path

from pydantic_settings import BaseSettings, SettingsConfigDict

PROJECT_ROOT = Path(__file__).resolve().parents[1]


class Settings(BaseSettings):
    bot_token: str = ""
    admin_ids: str = ""
    # Baza va yuklangan rasmlar shu papkada saqlanadi. Serverda buni doimiy
    # diskka (volume) yo'naltiring, aks holda har qayta joylashtirishda
    # ma'lumotlar yo'qoladi.
    data_dir: str = str(PROJECT_ROOT)
    database_url: str = ""
    webapp_url: str = ""
    api_base_url: str = ""
    # Bo'sh bo'lsa bot polling rejimida ishlaydi. To'ldirilsa (yoki
    # WEBAPP_URL bor bo'lsa) Telegram yangilanishlarni shu manzilga yuboradi —
    # bu uxlab qoladigan bepul hostinglar uchun zarur.
    use_webhook: bool = False
    webhook_secret: str = ""
    # Birinchi ishga tushishda katalog bo'sh bo'lsa demo ma'lumot qo'shiladi
    seed_demo: bool = False

    @property
    def public_url(self) -> str:
        """Ilovaning ochiq manzili (Mini App ham, webhook ham shundan oladi).

        Ba'zi hostinglar (masalan Render) manzilni o'zi beradi — shunda
        WEBAPP_URL ni qo'lda yozish shart emas.
        """
        url = self.webapp_url or os.environ.get("RENDER_EXTERNAL_URL", "")
        return url.rstrip("/")

    @property
    def webhook_path(self) -> str:
        return "/telegram/webhook"

    @property
    def webhook_url(self) -> str:
        return f"{self.public_url}{self.webhook_path}" if self.public_url else ""

    model_config = SettingsConfigDict(env_file=".env", env_file_encoding="utf-8")

    @property
    def admin_id_list(self) -> list[int]:
        return [int(x) for x in self.admin_ids.split(",") if x.strip()]

    @property
    def data_path(self) -> Path:
        return Path(self.data_dir)

    @property
    def db_url(self) -> str:
        """Async SQLAlchemy uchun baza manzili.

        Hostinglar odatda `postgres://...` yoki `postgresql://...` beradi —
        SQLAlchemy'ning async rejimi esa `postgresql+asyncpg://...` kutadi,
        shuning uchun manzil shu ko'rinishga keltiriladi.
        """
        url = self.database_url
        if not url:
            return f"sqlite+aiosqlite:///{self.data_path / 'bot.db'}"
        if url.startswith("postgres://"):
            url = "postgresql://" + url[len("postgres://") :]
        if url.startswith("postgresql://"):
            url = "postgresql+asyncpg://" + url[len("postgresql://") :]
        if url.startswith("sqlite://"):
            url = "sqlite+aiosqlite://" + url[len("sqlite://") :]
        return url

    @property
    def is_postgres(self) -> bool:
        return self.db_url.startswith("postgresql")


settings = Settings()

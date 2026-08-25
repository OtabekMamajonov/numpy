from pathlib import Path

from pydantic_settings import BaseSettings, SettingsConfigDict

PROJECT_ROOT = Path(__file__).resolve().parents[1]
STATIC_DIR = PROJECT_ROOT / "static"
IMAGES_DIR = STATIC_DIR / "images"


class Settings(BaseSettings):
    bot_token: str = ""
    admin_ids: str = ""
    database_url: str = "sqlite+aiosqlite:///./bot.db"
    webapp_url: str = ""
    api_base_url: str = "http://localhost:8000/api"

    model_config = SettingsConfigDict(env_file=".env", env_file_encoding="utf-8")

    @property
    def admin_id_list(self) -> list[int]:
        return [int(x) for x in self.admin_ids.split(",") if x.strip()]


settings = Settings()

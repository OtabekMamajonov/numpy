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

    model_config = SettingsConfigDict(env_file=".env", env_file_encoding="utf-8")

    @property
    def admin_id_list(self) -> list[int]:
        return [int(x) for x in self.admin_ids.split(",") if x.strip()]

    @property
    def data_path(self) -> Path:
        return Path(self.data_dir)

    @property
    def images_dir(self) -> Path:
        return self.data_path / "images"

    @property
    def db_url(self) -> str:
        if self.database_url:
            return self.database_url
        return f"sqlite+aiosqlite:///{self.data_path / 'bot.db'}"


settings = Settings()

import asyncio
import logging

from aiogram import Bot, Dispatcher
from aiogram.client.default import DefaultBotProperties
from aiogram.enums import ParseMode
from aiogram.fsm.storage.memory import MemoryStorage

from app.bot import instance
from app.bot.handlers import admin, catalog, catalog_admin, registration
from app.config import settings
from app.db.database import init_db


def create_bot() -> Bot:
    if not settings.bot_token:
        raise RuntimeError("BOT_TOKEN .env faylida ko'rsatilmagan")
    bot = Bot(token=settings.bot_token, default=DefaultBotProperties(parse_mode=ParseMode.HTML))
    # API ham shu bot orqali xabar yuboradi (yangi buyurtma bildirishnomasi)
    instance.set_bot(bot)
    return bot


def create_dispatcher() -> Dispatcher:
    dp = Dispatcher(storage=MemoryStorage())
    dp.include_router(admin.router)
    dp.include_router(catalog_admin.router)
    dp.include_router(registration.router)
    dp.include_router(catalog.router)
    return dp


async def setup_webhook() -> Bot:
    """Webhook rejimini yoqadi: Telegram yangilanishlarni API'ga yuboradi.

    Yangilanishlarni qabul qilish `app/api/webhook.py` da — bu funksiya faqat
    botni tayyorlab, Telegram'ga manzilni bildiradi.
    """
    from app.api import webhook as webhook_api

    bot = create_bot()
    dp = create_dispatcher()
    webhook_api.bind(bot, dp)

    await bot.set_webhook(
        url=settings.webhook_url,
        secret_token=settings.webhook_secret or None,
        drop_pending_updates=True,
    )
    logging.getLogger(__name__).info("Webhook o'rnatildi: %s", settings.webhook_url)
    return bot


async def run_bot() -> None:
    """Botni polling rejimida ishga tushiradi."""
    bot = create_bot()
    dp = create_dispatcher()
    await bot.delete_webhook(drop_pending_updates=True)
    await dp.start_polling(bot)


async def main() -> None:
    logging.basicConfig(level=logging.INFO)
    await init_db()
    await run_bot()


if __name__ == "__main__":
    asyncio.run(main())

"""Bot va API'ni birga ishga tushiradi.

Ikki rejim bor:

* **polling** — bot Telegram'dan yangilanishlarni o'zi so'rab turadi.
  Xizmat uzluksiz ishlab turishi kerak.
* **webhook** — Telegram yangilanishlarni API manziliga yuboradi. Uxlab
  qoladigan bepul hostinglar uchun shu rejim kerak: kelgan so'rov xizmatni
  uyg'otadi. `USE_WEBHOOK=true` va `WEBAPP_URL` bilan yoqiladi.
"""
import asyncio
import logging
import os

import uvicorn

from app.api.main import app as api_app
from app.bot.main import run_bot, setup_webhook
from app.config import settings
from app.db.database import init_db

logger = logging.getLogger(__name__)


async def main() -> None:
    logging.basicConfig(level=logging.INFO)
    await init_db()

    if settings.seed_demo:
        from app.db.seed import seed_if_empty

        if await seed_if_empty():
            logger.info("Katalog bo'sh edi — demo ma'lumotlar qo'shildi")

    port = int(os.environ.get("PORT", "8000"))
    server = uvicorn.Server(
        uvicorn.Config(api_app, host="0.0.0.0", port=port, log_level="info")
    )

    bot = None
    tasks = [asyncio.create_task(server.serve(), name="api")]

    if not settings.bot_token:
        logger.warning("BOT_TOKEN ko'rsatilmagan — faqat API ishga tushdi")
    elif settings.use_webhook:
        if not settings.webhook_url:
            raise RuntimeError("USE_WEBHOOK yoqilgan, lekin WEBAPP_URL ko'rsatilmagan")
        bot = await setup_webhook()
    else:
        tasks.append(asyncio.create_task(run_bot(), name="bot"))

    try:
        # Biror qism to'xtasa, qolganini ham to'xtatamiz: yarim ishlaydigan
        # holatda qolib ketgandan ko'ra, xizmat qayta ishga tushgani yaxshiroq.
        done, pending = await asyncio.wait(tasks, return_when=asyncio.FIRST_COMPLETED)
        for task in pending:
            task.cancel()
        await asyncio.gather(*pending, return_exceptions=True)
        for task in done:
            task.result()
    finally:
        if bot is not None:
            await bot.session.close()


if __name__ == "__main__":
    asyncio.run(main())

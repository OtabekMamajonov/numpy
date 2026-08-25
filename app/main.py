"""Bot va API'ni bitta jarayonda birga ishga tushiradi.

Ma'lumotlar SQLite faylida saqlangani uchun bot va API bir xil fayl tizimini
ko'rishi shart — shuning uchun ular alohida konteynerlarda emas, bitta
jarayonda, bitta asyncio siklida ishlaydi.
"""
import asyncio
import logging
import os

import uvicorn

from app.api.main import app as api_app
from app.bot.main import run_bot
from app.config import settings
from app.db.database import init_db

logger = logging.getLogger(__name__)


async def main() -> None:
    logging.basicConfig(level=logging.INFO)
    await init_db()

    port = int(os.environ.get("PORT", "8000"))
    server = uvicorn.Server(
        uvicorn.Config(api_app, host="0.0.0.0", port=port, log_level="info")
    )

    tasks = [asyncio.create_task(server.serve(), name="api")]

    if settings.bot_token:
        tasks.append(asyncio.create_task(run_bot(), name="bot"))
    else:
        logger.warning("BOT_TOKEN ko'rsatilmagan — faqat API ishga tushdi")

    # Biror qism to'xtasa, qolganini ham to'xtatamiz: yarim ishlaydigan
    # holatda qolib ketgandan ko'ra, xizmat qayta ishga tushgani yaxshiroq.
    done, pending = await asyncio.wait(tasks, return_when=asyncio.FIRST_COMPLETED)

    for task in pending:
        task.cancel()
    await asyncio.gather(*pending, return_exceptions=True)

    for task in done:
        task.result()


if __name__ == "__main__":
    asyncio.run(main())

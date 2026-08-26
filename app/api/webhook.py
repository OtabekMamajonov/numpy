"""Telegram webhook endpointi.

Polling rejimi xizmat uzluksiz ishlab turishini talab qiladi. Bepul
hostinglarda esa xizmat harakatsizlikdan keyin uxlab qoladi va polling
to'xtaydi — bot javob bermay qo'yadi. Webhook rejimida esa aksincha:
Telegram yangilanishni o'zi yuboradi va shu so'rov xizmatni uyg'otadi.
"""
import logging

from aiogram import Bot, Dispatcher
from aiogram.types import Update
from fastapi import APIRouter, Header, HTTPException, Request, status

from app.config import settings

logger = logging.getLogger(__name__)

router = APIRouter()

# app.main webhook rejimida shularni to'ldiradi
bot: Bot | None = None
dispatcher: Dispatcher | None = None


def bind(bot_instance: Bot, dp: Dispatcher) -> None:
    global bot, dispatcher
    bot, dispatcher = bot_instance, dp


@router.post(settings.webhook_path)
async def telegram_webhook(
    request: Request,
    secret_token: str = Header(default="", alias="X-Telegram-Bot-Api-Secret-Token"),
):
    if bot is None or dispatcher is None:
        raise HTTPException(status_code=status.HTTP_503_SERVICE_UNAVAILABLE, detail="Bot ishlamayapti")

    # Manzil ochiq bo'lgani uchun, so'rov haqiqatan Telegram'dan kelganini
    # setWebhook'da berilgan maxfiy kalit orqali tekshiramiz.
    if settings.webhook_secret and secret_token != settings.webhook_secret:
        raise HTTPException(status_code=status.HTTP_403_FORBIDDEN, detail="Noto'g'ri kalit")

    try:
        update = Update.model_validate(await request.json(), context={"bot": bot})
    except Exception:
        logger.exception("Webhook: yangilanishni o'qib bo'lmadi")
        raise HTTPException(status_code=status.HTTP_400_BAD_REQUEST, detail="Buzuq yangilanish")

    # Xatolik bo'lsa ham Telegram'ga 200 qaytaramiz: aks holda u bir xil
    # yangilanishni qayta-qayta yuboraveradi.
    try:
        await dispatcher.feed_update(bot, update)
    except Exception:
        logger.exception("Webhook: yangilanishni qayta ishlashda xato")
    return {"ok": True}

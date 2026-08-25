"""Telegram Mini App'dan kelgan so'rovlarni tekshirish.

Mini App har bir so'rovda `initData` yuboradi — bu Telegram tomonidan bot
tokeni bilan imzolangan qator. Imzoni tekshirmasdan turib foydalanuvchi
ma'lumotlariga ishonib bo'lmaydi: aks holda istalgan odam o'zini boshqa
foydalanuvchi sifatida ko'rsatib ro'yxatdan o'tishi mumkin.

Hujjat: https://core.telegram.org/bots/webapps#validating-data-received-via-the-mini-app
"""
import hashlib
import hmac
import json
import time
from urllib.parse import parse_qsl

from fastapi import Header, HTTPException, status

from app.config import settings

# initData shu muddatdan eski bo'lsa qabul qilinmaydi
MAX_AGE_SECONDS = 24 * 60 * 60


def _unauthorized(detail: str) -> HTTPException:
    return HTTPException(status_code=status.HTTP_401_UNAUTHORIZED, detail=detail)


def validate_init_data(init_data: str) -> dict:
    """initData imzosini tekshirib, ichidagi foydalanuvchi ma'lumotini qaytaradi."""
    if not settings.bot_token:
        raise HTTPException(
            status_code=status.HTTP_503_SERVICE_UNAVAILABLE,
            detail="BOT_TOKEN sozlanmagan",
        )
    if not init_data:
        raise _unauthorized("initData yo'q")

    pairs = dict(parse_qsl(init_data))
    received_hash = pairs.pop("hash", None)
    if not received_hash:
        raise _unauthorized("initData imzosi yo'q")

    check_string = "\n".join(f"{key}={pairs[key]}" for key in sorted(pairs))
    secret_key = hmac.new(b"WebAppData", settings.bot_token.encode(), hashlib.sha256).digest()
    expected_hash = hmac.new(secret_key, check_string.encode(), hashlib.sha256).hexdigest()

    if not hmac.compare_digest(expected_hash, received_hash):
        raise _unauthorized("initData imzosi noto'g'ri")

    try:
        auth_date = int(pairs.get("auth_date", "0"))
    except ValueError:
        raise _unauthorized("auth_date noto'g'ri")
    if auth_date <= 0 or time.time() - auth_date > MAX_AGE_SECONDS:
        raise _unauthorized("initData muddati o'tgan")

    try:
        user = json.loads(pairs.get("user", "{}"))
    except json.JSONDecodeError:
        raise _unauthorized("foydalanuvchi ma'lumoti buzuq")

    if not isinstance(user, dict) or not user.get("id"):
        raise _unauthorized("foydalanuvchi aniqlanmadi")

    return user


async def telegram_user(authorization: str = Header(default="")) -> dict:
    """FastAPI dependency: `Authorization: tma <initData>` sarlavhasini tekshiradi."""
    scheme, _, init_data = authorization.partition(" ")
    if scheme.lower() != "tma":
        raise _unauthorized("Authorization sarlavhasi kutilgan formatda emas")
    return validate_init_data(init_data)

"""Ishlab turgan Bot obyektiga umumiy kirish nuqtasi.

Bot va API bitta jarayonda ishlaydi, shuning uchun API ham xabar yubora oladi
(masalan yangi buyurtma haqida adminlarga). Shu obyekt ana shu uchun saqlanadi.
"""
from aiogram import Bot

_bot: Bot | None = None


def set_bot(bot: Bot) -> None:
    global _bot
    _bot = bot


def get_bot() -> Bot | None:
    return _bot

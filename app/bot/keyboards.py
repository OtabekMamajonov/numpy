from aiogram.types import (
    InlineKeyboardButton,
    InlineKeyboardMarkup,
    KeyboardButton,
    ReplyKeyboardMarkup,
    ReplyKeyboardRemove,
    WebAppInfo,
)

from app.config import settings
from app.db.models import Brigade, Order

CITIES = [
    "Toshkent",
    "Samarqand",
    "Buxoro",
    "Andijon",
    "Farg'ona",
    "Namangan",
    "Qashqadaryo",
    "Surxondaryo",
    "Xorazm",
    "Navoiy",
    "Jizzax",
    "Sirdaryo",
    "Qoraqalpog'iston",
]


def phone_request_keyboard() -> ReplyKeyboardMarkup:
    return ReplyKeyboardMarkup(
        keyboard=[[KeyboardButton(text="📱 Telefon raqamni yuborish", request_contact=True)]],
        resize_keyboard=True,
        one_time_keyboard=True,
    )


def city_keyboard() -> ReplyKeyboardMarkup:
    rows = [CITIES[i : i + 2] for i in range(0, len(CITIES), 2)]
    keyboard = [[KeyboardButton(text=city) for city in row] for row in rows]
    return ReplyKeyboardMarkup(keyboard=keyboard, resize_keyboard=True, one_time_keyboard=True)


def main_menu_keyboard() -> ReplyKeyboardMarkup:
    return ReplyKeyboardMarkup(
        keyboard=[
            [KeyboardButton(text="🛠 Katalog", web_app=WebAppInfo(url=settings.webapp_url))],
            [KeyboardButton(text="📋 Mening buyurtmalarim")],
        ],
        resize_keyboard=True,
    )


def remove_keyboard() -> ReplyKeyboardRemove:
    return ReplyKeyboardRemove()


def brigade_assign_keyboard(order_id: int, brigades: list[Brigade]) -> InlineKeyboardMarkup:
    rows = [
        [InlineKeyboardButton(text=b.name, callback_data=f"assign:{order_id}:{b.id}")] for b in brigades
    ]
    return InlineKeyboardMarkup(inline_keyboard=rows)


def order_status_keyboard(order: Order) -> InlineKeyboardMarkup:
    rows = [
        [InlineKeyboardButton(text="🔧 Jarayonda", callback_data=f"status:{order.id}:in_progress")],
        [InlineKeyboardButton(text="✅ Bajarildi", callback_data=f"status:{order.id}:done")],
    ]
    return InlineKeyboardMarkup(inline_keyboard=rows)

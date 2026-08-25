from aiogram.types import (
    InlineKeyboardButton,
    InlineKeyboardMarkup,
    KeyboardButton,
    ReplyKeyboardMarkup,
    WebAppInfo,
)

from app.config import settings
from app.db.models import Brigade, Category, Order, Service

def main_menu_keyboard() -> ReplyKeyboardMarkup:
    return ReplyKeyboardMarkup(
        keyboard=[
            [KeyboardButton(text="🛠 Katalog", web_app=WebAppInfo(url=settings.webapp_url))],
            [KeyboardButton(text="📋 Mening buyurtmalarim")],
        ],
        resize_keyboard=True,
    )


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


# --- Katalogni boshqarish (admin) ---


def categories_keyboard(categories: list[Category]) -> InlineKeyboardMarkup:
    rows = [
        [
            InlineKeyboardButton(
                text=f"{c.name} ({len(c.services)})", callback_data=f"cat:view:{c.id}"
            )
        ]
        for c in categories
    ]
    rows.append([InlineKeyboardButton(text="➕ Yangi kategoriya", callback_data="cat:add")])
    return InlineKeyboardMarkup(inline_keyboard=rows)


def category_keyboard(category_id: int, services: list[Service]) -> InlineKeyboardMarkup:
    rows = [
        [
            InlineKeyboardButton(
                text=f"{'🟢' if s.is_active else '🔴'} {s.name} — {s.price}",
                callback_data=f"srv:view:{s.id}",
            )
        ]
        for s in services
    ]
    rows.append(
        [InlineKeyboardButton(text="➕ Xizmat qo'shish", callback_data=f"srv:add:{category_id}")]
    )
    rows.append(
        [
            InlineKeyboardButton(text="✏️ Nomi", callback_data=f"cat:rename:{category_id}"),
            InlineKeyboardButton(text="🗑 O'chirish", callback_data=f"cat:del:{category_id}"),
        ]
    )
    rows.append([InlineKeyboardButton(text="⬅️ Orqaga", callback_data="cat:list")])
    return InlineKeyboardMarkup(inline_keyboard=rows)


def service_keyboard(service: Service) -> InlineKeyboardMarkup:
    toggle_text = "🔴 Nofaol qilish" if service.is_active else "🟢 Faol qilish"
    rows = [
        [
            InlineKeyboardButton(text="✏️ Nomi", callback_data=f"srv:edit:name:{service.id}"),
            InlineKeyboardButton(text="💰 Narxi", callback_data=f"srv:edit:price:{service.id}"),
        ],
        [
            InlineKeyboardButton(
                text="📝 Izoh", callback_data=f"srv:edit:description:{service.id}"
            ),
            InlineKeyboardButton(text="🖼 Rasm", callback_data=f"srv:edit:image:{service.id}"),
        ],
        [InlineKeyboardButton(text=toggle_text, callback_data=f"srv:toggle:{service.id}")],
        [InlineKeyboardButton(text="🗑 O'chirish", callback_data=f"srv:del:{service.id}")],
        [
            InlineKeyboardButton(
                text="⬅️ Orqaga", callback_data=f"cat:view:{service.category_id}"
            )
        ],
    ]
    return InlineKeyboardMarkup(inline_keyboard=rows)


def confirm_delete_keyboard(kind: str, entity_id: int, back_callback: str) -> InlineKeyboardMarkup:
    return InlineKeyboardMarkup(
        inline_keyboard=[
            [
                InlineKeyboardButton(
                    text="✅ Ha, o'chirilsin", callback_data=f"{kind}:delyes:{entity_id}"
                )
            ],
            [InlineKeyboardButton(text="❌ Bekor qilish", callback_data=back_callback)],
        ]
    )


def skip_keyboard(callback_data: str) -> InlineKeyboardMarkup:
    return InlineKeyboardMarkup(
        inline_keyboard=[[InlineKeyboardButton(text="⏭ O'tkazib yuborish", callback_data=callback_data)]]
    )

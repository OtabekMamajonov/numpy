import json
from html import escape

from aiogram import F, Router
from aiogram.types import Message, WebAppData

from app.bot.keyboards import brigade_assign_keyboard
from app.config import settings
from app.db.crud import (
    create_order,
    get_or_create_user,
    get_service,
    get_user_orders,
    list_brigades,
)
from app.db.database import get_session
from app.db.models import OrderStatus

router = Router(name="catalog")

STATUS_LABELS = {
    OrderStatus.NEW: "🆕 Yangi",
    OrderStatus.ASSIGNED: "👷 Brigadaga berildi",
    OrderStatus.IN_PROGRESS: "🔧 Jarayonda",
    OrderStatus.DONE: "✅ Bajarildi",
    OrderStatus.CANCELLED: "❌ Bekor qilindi",
}


@router.message(F.web_app_data)
async def process_webapp_order(message: Message) -> None:
    data: WebAppData = message.web_app_data
    try:
        payload = json.loads(data.data)
        service_id = int(payload["service_id"])
    except (KeyError, ValueError, TypeError, json.JSONDecodeError):
        await message.answer("Buyurtmada xatolik yuz berdi, qaytadan urinib ko'ring.")
        return

    comment = payload.get("comment")
    address = payload.get("address")

    async with get_session() as session:
        user = await get_or_create_user(session, message.from_user.id)
        if not user.is_registered:
            await message.answer(
                "Avval ro'yxatdan o'ting — 🛠 Katalog tugmasini bosing."
            )
            return

        service = await get_service(session, service_id)
        if service is None or not service.is_active:
            await message.answer("Bu xizmat hozircha mavjud emas. Katalogni qaytadan oching.")
            return

        order = await create_order(session, user.id, service_id, comment, address)
        brigades = await list_brigades(session)

    await message.answer(
        f"✅ Buyurtmangiz qabul qilindi!\n\n"
        f"Xizmat: {escape(service.name)}\n"
        f"Narxi: {escape(service.price)}\n"
        f"Holati: {STATUS_LABELS[order.status]}\n\n"
        "Tez orada operatorimiz siz bilan bog'lanadi."
    )

    admin_text = (
        f"🆕 Yangi buyurtma #{order.id}\n\n"
        f"Mijoz: {escape(user.full_name or '—')}\n"
        f"Telefon: {escape(user.phone or '—')}\n"
        f"Manzil: {escape(user.city or '—')}, {escape(user.district or '—')}"
        + (f" ({escape(address)})" if address else "")
        + f"\n\nXizmat: {escape(service.name)}\n"
        f"Narxi: {escape(service.price)}\n"
        + (f"Izoh: {escape(comment)}\n" if comment else "")
    )
    keyboard = brigade_assign_keyboard(order.id, brigades) if brigades else None
    for admin_id in settings.admin_id_list:
        try:
            await message.bot.send_message(admin_id, admin_text, reply_markup=keyboard)
        except Exception:
            continue


@router.message(F.text == "📋 Mening buyurtmalarim")
async def my_orders(message: Message) -> None:
    async with get_session() as session:
        user = await get_or_create_user(session, message.from_user.id)
        if not user.is_registered:
            await message.answer(
                "Avval ro'yxatdan o'ting — 🛠 Katalog tugmasini bosing."
            )
            return
        orders = await get_user_orders(session, user.id)

    if not orders:
        await message.answer("Sizda hali buyurtmalar yo'q. Katalogdan xizmat tanlang.")
        return

    lines = ["📋 Sizning buyurtmalaringiz:\n"]
    for order in orders:
        brigade_line = f"\nBrigada: {escape(order.brigade.name)}" if order.brigade else ""
        lines.append(
            f"#{order.id} — {escape(order.service.name)}\n"
            f"Holati: {STATUS_LABELS[order.status]}{brigade_line}\n"
        )
    await message.answer("\n".join(lines))

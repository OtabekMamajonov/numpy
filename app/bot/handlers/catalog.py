from html import escape

from aiogram import F, Router
from aiogram.types import Message

from app.bot.notify import STATUS_LABELS
from app.db.crud import get_or_create_user, get_user_orders
from app.db.database import get_session

router = Router(name="catalog")


@router.message(F.text == "📋 Mening buyurtmalarim")
async def my_orders(message: Message) -> None:
    async with get_session() as session:
        user = await get_or_create_user(session, message.from_user.id)
        if not user.is_registered:
            await message.answer("Avval ro'yxatdan o'ting — 🛠 Katalog tugmasini bosing.")
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

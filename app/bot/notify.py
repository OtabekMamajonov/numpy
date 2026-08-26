"""Buyurtma bo'yicha xabarnomalar.

Buyurtma Mini App'dan API orqali yaratiladi, lekin xabarlarni bot yuboradi —
shuning uchun bu mantiq alohida modulda va ikkala tomondan ham chaqiriladi.
"""
import logging
from html import escape

from app.bot.instance import get_bot
from app.bot.keyboards import brigade_assign_keyboard
from app.config import settings
from app.db.models import Brigade, Order, OrderStatus

logger = logging.getLogger(__name__)

STATUS_LABELS = {
    OrderStatus.NEW: "🆕 Yangi",
    OrderStatus.ASSIGNED: "👷 Brigadaga berildi",
    OrderStatus.IN_PROGRESS: "🔧 Jarayonda",
    OrderStatus.DONE: "✅ Bajarildi",
    OrderStatus.CANCELLED: "❌ Bekor qilindi",
}


def client_confirmation(order: Order) -> str:
    return (
        f"✅ Buyurtmangiz qabul qilindi!\n\n"
        f"Raqami: #{order.id}\n"
        f"Xizmat: {escape(order.service.name)}\n"
        f"Narxi: {escape(order.service.price)}\n"
        f"Holati: {STATUS_LABELS[order.status]}\n\n"
        "Tez orada operatorimiz siz bilan bog'lanadi."
    )


def admin_notification(order: Order) -> str:
    client = order.client
    lines = [
        f"🆕 Yangi buyurtma #{order.id}\n",
        f"Mijoz: {escape(client.full_name or '—')}",
        f"Telefon: {escape(client.phone or '—')}",
        f"Manzil: {escape(client.city or '—')}, {escape(client.district or '—')}"
        + (f" ({escape(order.address)})" if order.address else ""),
        "",
        f"Xizmat: {escape(order.service.name)}",
        f"Narxi: {escape(order.service.price)}",
    ]
    if order.comment:
        lines.append(f"Izoh: {escape(order.comment)}")
    return "\n".join(lines)


async def announce_new_order(order: Order, brigades: list[Brigade]) -> None:
    """Mijozga tasdiq, adminlarga esa buyurtma va brigada tugmalarini yuboradi.

    Xabar ketmasa ham buyurtma bazada saqlanib qolgani uchun xatolik
    yutiladi — faqat log'ga yoziladi.
    """
    bot = get_bot()
    if bot is None:
        logger.warning("Bot ishlamayapti — buyurtma #%s haqida xabar yuborilmadi", order.id)
        return

    try:
        await bot.send_message(order.client.telegram_id, client_confirmation(order))
    except Exception:
        logger.exception("Mijozga tasdiq yuborilmadi (buyurtma #%s)", order.id)

    keyboard = brigade_assign_keyboard(order.id, brigades) if brigades else None
    text = admin_notification(order)
    for admin_id in settings.admin_id_list:
        try:
            await bot.send_message(admin_id, text, reply_markup=keyboard)
        except Exception:
            logger.exception("Adminga (%s) xabar yuborilmadi", admin_id)

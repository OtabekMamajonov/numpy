from aiogram import F, Router
from aiogram.filters import Command
from aiogram.fsm.context import FSMContext
from aiogram.types import CallbackQuery, Message

from app.bot.states import AddBrigade
from app.config import settings
from app.db.crud import (
    assign_brigade,
    create_brigade,
    get_order,
    list_brigades,
    list_orders_by_status,
    update_order_status,
)
from app.db.database import get_session
from app.db.models import OrderStatus

router = Router(name="admin")
router.message.filter(F.from_user.id.in_(settings.admin_id_list))
router.callback_query.filter(F.from_user.id.in_(settings.admin_id_list))

STATUS_LABELS = {
    OrderStatus.NEW: "🆕 Yangi",
    OrderStatus.ASSIGNED: "👷 Brigadaga berildi",
    OrderStatus.IN_PROGRESS: "🔧 Jarayonda",
    OrderStatus.DONE: "✅ Bajarildi",
    OrderStatus.CANCELLED: "❌ Bekor qilindi",
}


@router.message(Command("admin"))
async def admin_menu(message: Message) -> None:
    await message.answer(
        "🛠 Admin panel\n\n"
        "/new_orders — yangi buyurtmalar\n"
        "/brigades — brigadalar ro'yxati\n"
        "/addbrigade — yangi brigada qo'shish"
    )


@router.message(Command("new_orders"))
async def new_orders(message: Message) -> None:
    async with get_session() as session:
        orders = await list_orders_by_status(session, OrderStatus.NEW)
    if not orders:
        await message.answer("Yangi buyurtmalar yo'q.")
        return
    lines = []
    for order in orders:
        lines.append(
            f"#{order.id} — {order.service.name}\n"
            f"Mijoz: {order.client.full_name} ({order.client.phone})\n"
        )
    await message.answer("\n".join(lines))


@router.message(Command("brigades"))
async def list_all_brigades(message: Message) -> None:
    async with get_session() as session:
        brigades = await list_brigades(session, active_only=False)
    if not brigades:
        await message.answer("Brigadalar hali qo'shilmagan. /addbrigade orqali qo'shing.")
        return
    lines = [f"#{b.id} — {b.name} ({b.specialty or '—'}), tel: {b.phone or '—'}" for b in brigades]
    await message.answer("\n".join(lines))


@router.message(Command("addbrigade"))
async def add_brigade_start(message: Message, state: FSMContext) -> None:
    await state.set_state(AddBrigade.name)
    await message.answer("Brigada nomini kiriting:")


@router.message(AddBrigade.name, F.text)
async def add_brigade_name(message: Message, state: FSMContext) -> None:
    await state.update_data(name=message.text.strip())
    await state.set_state(AddBrigade.phone)
    await message.answer("Brigada telefon raqamini kiriting:")


@router.message(AddBrigade.phone, F.text)
async def add_brigade_phone(message: Message, state: FSMContext) -> None:
    await state.update_data(phone=message.text.strip())
    await state.set_state(AddBrigade.specialty)
    await message.answer("Brigada mutaxassisligini kiriting (masalan: Santexnika):")


@router.message(AddBrigade.specialty, F.text)
async def add_brigade_specialty(message: Message, state: FSMContext) -> None:
    data = await state.update_data(specialty=message.text.strip())
    async with get_session() as session:
        brigade = await create_brigade(session, data["name"], data["phone"], data["specialty"])
    await state.clear()
    await message.answer(f"✅ Brigada qo'shildi: #{brigade.id} {brigade.name}")


@router.callback_query(F.data.startswith("assign:"))
async def assign_brigade_callback(callback: CallbackQuery) -> None:
    _, order_id_str, brigade_id_str = callback.data.split(":")
    order_id, brigade_id = int(order_id_str), int(brigade_id_str)

    async with get_session() as session:
        order = await assign_brigade(session, order_id, brigade_id)

    if order is None:
        await callback.answer("Buyurtma topilmadi.", show_alert=True)
        return

    await callback.message.edit_text(
        callback.message.text + f"\n\n✅ Brigadaga berildi: {order.brigade.name}"
    )
    await callback.answer("Brigadaga biriktirildi")

    if order.brigade.telegram_id:
        try:
            await callback.bot.send_message(
                order.brigade.telegram_id,
                f"🆕 Sizga yangi ish biriktirildi!\n\n"
                f"#{order.id} — {order.service.name}\n"
                f"Mijoz: {order.client.full_name}, tel: {order.client.phone}\n"
                f"Manzil: {order.client.city}, {order.client.district}",
            )
        except Exception:
            pass

    try:
        await callback.bot.send_message(
            order.client.telegram_id,
            f"👷 Buyurtmangiz #{order.id} brigadaga berildi: {order.brigade.name} ({order.brigade.phone})",
        )
    except Exception:
        pass


@router.callback_query(F.data.startswith("status:"))
async def update_status_callback(callback: CallbackQuery) -> None:
    _, order_id_str, status_value = callback.data.split(":")
    order_id = int(order_id_str)

    async with get_session() as session:
        order = await update_order_status(session, order_id, OrderStatus(status_value))

    if order is None:
        await callback.answer("Buyurtma topilmadi.", show_alert=True)
        return

    await callback.answer(f"Holat yangilandi: {STATUS_LABELS[order.status]}")
    try:
        await callback.bot.send_message(
            order.client.telegram_id,
            f"ℹ️ Buyurtmangiz #{order.id} holati: {STATUS_LABELS[order.status]}",
        )
    except Exception:
        pass

from html import escape

from aiogram import Router
from aiogram.filters import CommandStart
from aiogram.fsm.context import FSMContext
from aiogram.types import Message

from app.bot.keyboards import main_menu_keyboard
from app.config import settings
from app.db.crud import get_or_create_user
from app.db.database import get_session

router = Router(name="registration")


@router.message(CommandStart())
async def cmd_start(message: Message, state: FSMContext) -> None:
    await state.clear()

    async with get_session() as session:
        user = await get_or_create_user(session, message.from_user.id)

    if not settings.public_url:
        await message.answer(
            "Bot hali to'liq sozlanmagan: WEBAPP_URL ko'rsatilmagan.\n"
            "Administrator .env faylida Mini App manzilini ko'rsatishi kerak."
        )
        return

    if user.is_registered:
        text = (
            f"Xush kelibsiz, {escape(user.full_name or '')}! 👷\n\n"
            "Katalogdan kerakli xizmatni tanlab, buyurtma bering."
        )
    else:
        text = (
            "Assalomu alaykum! 👋\n\n"
            "Qurilish xizmatlari botiga xush kelibsiz.\n"
            "Quyidagi <b>🛠 Katalog</b> tugmasini bosing — ro'yxatdan o'tasiz va "
            "xizmatlar bilan tanishasiz."
        )

    await message.answer(text, reply_markup=main_menu_keyboard())

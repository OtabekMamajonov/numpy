from aiogram import F, Router
from aiogram.filters import CommandStart
from aiogram.fsm.context import FSMContext
from aiogram.types import Message

from app.bot.keyboards import city_keyboard, main_menu_keyboard, phone_request_keyboard
from app.bot.states import Registration
from app.db.crud import get_or_create_user, save_registration
from app.db.database import get_session

router = Router(name="registration")


@router.message(CommandStart())
async def cmd_start(message: Message, state: FSMContext) -> None:
    async with get_session() as session:
        user = await get_or_create_user(session, message.from_user.id)

    if user.is_registered:
        await message.answer(
            f"Xush kelibsiz, {user.full_name}! 👷\n\nQuyidagi menyudan foydalaning:",
            reply_markup=main_menu_keyboard(),
        )
        return

    await state.set_state(Registration.full_name)
    await message.answer(
        "Assalomu alaykum! 👋\n\n"
        "Qurilish xizmatlari botiga xush kelibsiz.\n"
        "Avval ro'yxatdan o'tishingiz kerak.\n\n"
        "Ism va familiyangizni kiriting:"
    )


@router.message(Registration.full_name, F.text)
async def process_full_name(message: Message, state: FSMContext) -> None:
    await state.update_data(full_name=message.text.strip())
    await state.set_state(Registration.phone)
    await message.answer(
        "Telefon raqamingizni yuboring (tugmani bosing yoki qo'lda kiriting, masalan +998901234567):",
        reply_markup=phone_request_keyboard(),
    )


@router.message(Registration.phone, F.contact)
async def process_phone_contact(message: Message, state: FSMContext) -> None:
    await state.update_data(phone=message.contact.phone_number)
    await state.set_state(Registration.city)
    await message.answer("Qaysi shahar/viloyatdansiz?", reply_markup=city_keyboard())


@router.message(Registration.phone, F.text)
async def process_phone_text(message: Message, state: FSMContext) -> None:
    await state.update_data(phone=message.text.strip())
    await state.set_state(Registration.city)
    await message.answer("Qaysi shahar/viloyatdansiz?", reply_markup=city_keyboard())


@router.message(Registration.city, F.text)
async def process_city(message: Message, state: FSMContext) -> None:
    await state.update_data(city=message.text.strip())
    await state.set_state(Registration.district)
    await message.answer("Qaysi tuman/hudud? (masalan: Chilonzor tumani)")


@router.message(Registration.district, F.text)
async def process_district(message: Message, state: FSMContext) -> None:
    data = await state.update_data(district=message.text.strip())
    async with get_session() as session:
        user = await save_registration(
            session,
            telegram_id=message.from_user.id,
            full_name=data["full_name"],
            phone=data["phone"],
            city=data["city"],
            district=data["district"],
        )
    await state.clear()
    await message.answer(
        f"Rahmat, {user.full_name}! Siz muvaffaqiyatli ro'yxatdan o'tdingiz. ✅\n\n"
        "Endi katalogdan xizmat tanlashingiz mumkin:",
        reply_markup=main_menu_keyboard(),
    )

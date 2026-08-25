"""Admin uchun katalogni boshqarish: kategoriya va xizmatlarni qo'shish, tahrirlash, o'chirish."""
from html import escape
from pathlib import Path

from aiogram import F, Router
from aiogram.filters import Command
from aiogram.fsm.context import FSMContext
from aiogram.types import CallbackQuery, InlineKeyboardMarkup, Message
from sqlalchemy.ext.asyncio import AsyncSession

from app.bot.keyboards import (
    categories_keyboard,
    category_keyboard,
    confirm_delete_keyboard,
    service_keyboard,
    skip_keyboard,
)
from app.bot.states import AddCategory, AddService, EditServiceField, RenameCategory
from app.config import IMAGES_DIR, settings
from app.db import crud
from app.db.database import get_session
from app.db.models import Service

router = Router(name="catalog_admin")
router.message.filter(F.from_user.id.in_(settings.admin_id_list))
router.callback_query.filter(F.from_user.id.in_(settings.admin_id_list))

FIELD_PROMPTS = {
    "name": "Yangi nomni kiriting:",
    "price": "Yangi narxni kiriting (masalan: <code>150 000 so'm</code>):",
    "description": "Yangi izohni kiriting:",
    "image": "Yangi rasmni surat sifatida yuboring:",
}


# --- Yordamchi funksiyalar ---


async def _save_photo(message: Message, prefix: str) -> str:
    photo = message.photo[-1]
    IMAGES_DIR.mkdir(parents=True, exist_ok=True)
    filename = f"{prefix}_{photo.file_unique_id}.jpg"
    await message.bot.download(photo.file_id, destination=IMAGES_DIR / filename)
    return f"/static/images/{filename}"


def _delete_image_file(image_url: str | None) -> None:
    """Eski rasm faylini diskdan o'chiradi (faqat static/images ichidagilarni)."""
    if not image_url or not image_url.startswith("/static/images/"):
        return
    images_root = IMAGES_DIR.resolve()
    path = (images_root / Path(image_url).name).resolve()
    try:
        if path.is_relative_to(images_root) and path.is_file():
            path.unlink()
    except OSError:
        pass


async def _categories_view(session: AsyncSession) -> tuple[str, InlineKeyboardMarkup]:
    categories = await crud.get_categories_with_services(session, active_only=False)
    if categories:
        text = "📂 <b>Katalog boshqaruvi</b>\n\nKategoriyani tanlang:"
    else:
        text = "📂 <b>Katalog boshqaruvi</b>\n\nKatalog hozircha bo'sh. Kategoriya qo'shing:"
    return text, categories_keyboard(categories)


async def _category_view(
    session: AsyncSession, category_id: int
) -> tuple[str, InlineKeyboardMarkup] | None:
    category = await crud.get_category(session, category_id)
    if category is None:
        return None
    services = await crud.get_services_by_category(session, category_id, include_inactive=True)
    text = f"📂 <b>{escape(category.name)}</b>\n\n"
    text += f"Xizmatlar: {len(services)} ta" if services else "Bu kategoriyada xizmat yo'q."
    return text, category_keyboard(category_id, services)


async def _service_view(
    session: AsyncSession, service_id: int
) -> tuple[str, InlineKeyboardMarkup] | None:
    service = await crud.get_service(session, service_id)
    if service is None:
        return None
    orders_count = await crud.count_service_orders(session, service_id)
    text = (
        f"🛠 <b>{escape(service.name)}</b>\n\n"
        f"Kategoriya: {escape(service.category.name)}\n"
        f"Narxi: {escape(service.price)}\n"
        f"Izoh: {escape(service.description) if service.description else '—'}\n"
        f"Rasm: {'bor ✅' if service.image_url else 'yo`q'}\n"
        f"Holati: {'🟢 Faol' if service.is_active else '🔴 Nofaol'}\n"
        f"Buyurtmalar: {orders_count} ta"
    )
    return text, service_keyboard(service)


async def _show_service(message: Message, service_id: int) -> None:
    async with get_session() as session:
        view = await _service_view(session, service_id)
    if view is None:
        await message.answer("Xizmat topilmadi.")
        return
    text, keyboard = view
    await message.answer(text, reply_markup=keyboard)


# --- Kirish nuqtasi ---


@router.message(Command("catalog"))
async def catalog_root(message: Message, state: FSMContext) -> None:
    await state.clear()
    async with get_session() as session:
        text, keyboard = await _categories_view(session)
    await message.answer(text, reply_markup=keyboard)


@router.callback_query(F.data == "cat:list")
async def show_categories(callback: CallbackQuery) -> None:
    async with get_session() as session:
        text, keyboard = await _categories_view(session)
    await callback.message.edit_text(text, reply_markup=keyboard)
    await callback.answer()


@router.callback_query(F.data.startswith("cat:view:"))
async def show_category(callback: CallbackQuery) -> None:
    category_id = int(callback.data.split(":")[2])
    async with get_session() as session:
        view = await _category_view(session, category_id)
    if view is None:
        await callback.answer("Kategoriya topilmadi.", show_alert=True)
        return
    text, keyboard = view
    await callback.message.edit_text(text, reply_markup=keyboard)
    await callback.answer()


@router.callback_query(F.data.startswith("srv:view:"))
async def show_service(callback: CallbackQuery) -> None:
    service_id = int(callback.data.split(":")[2])
    async with get_session() as session:
        view = await _service_view(session, service_id)
    if view is None:
        await callback.answer("Xizmat topilmadi.", show_alert=True)
        return
    text, keyboard = view
    await callback.message.edit_text(text, reply_markup=keyboard)
    await callback.answer()


# --- Kategoriya qo'shish / nomini o'zgartirish ---


@router.callback_query(F.data == "cat:add")
async def add_category_start(callback: CallbackQuery, state: FSMContext) -> None:
    await state.set_state(AddCategory.name)
    await callback.message.answer("Yangi kategoriya nomini kiriting:")
    await callback.answer()


@router.message(AddCategory.name, F.text)
async def add_category_finish(message: Message, state: FSMContext) -> None:
    await state.clear()
    async with get_session() as session:
        category = await crud.create_category(session, message.text.strip())
        view = await _category_view(session, category.id)
    text, keyboard = view
    await message.answer(f"✅ Kategoriya qo'shildi.\n\n{text}", reply_markup=keyboard)


@router.callback_query(F.data.startswith("cat:rename:"))
async def rename_category_start(callback: CallbackQuery, state: FSMContext) -> None:
    category_id = int(callback.data.split(":")[2])
    await state.set_state(RenameCategory.name)
    await state.update_data(category_id=category_id)
    await callback.message.answer("Kategoriyaning yangi nomini kiriting:")
    await callback.answer()


@router.message(RenameCategory.name, F.text)
async def rename_category_finish(message: Message, state: FSMContext) -> None:
    data = await state.get_data()
    await state.clear()
    async with get_session() as session:
        category = await crud.rename_category(session, data["category_id"], message.text.strip())
        if category is None:
            await message.answer("Kategoriya topilmadi.")
            return
        view = await _category_view(session, category.id)
    text, keyboard = view
    await message.answer(f"✅ Nomi o'zgartirildi.\n\n{text}", reply_markup=keyboard)


# --- Kategoriyani o'chirish ---


@router.callback_query(F.data.startswith("cat:del:"))
async def delete_category_confirm(callback: CallbackQuery) -> None:
    category_id = int(callback.data.split(":")[2])
    async with get_session() as session:
        category = await crud.get_category(session, category_id)
        if category is None:
            await callback.answer("Kategoriya topilmadi.", show_alert=True)
            return
        services = await crud.get_services_by_category(session, category_id, include_inactive=True)

    await callback.message.edit_text(
        f"🗑 <b>{escape(category.name)}</b> kategoriyasi "
        f"{len(services)} ta xizmati bilan birga o'chiriladi.\n\n"
        "Buyurtma qilingan xizmatlar tarix uchun bazada qoladi, ammo katalogda ko'rinmaydi.\n\n"
        "Davom etamizmi?",
        reply_markup=confirm_delete_keyboard("cat", category_id, f"cat:view:{category_id}"),
    )
    await callback.answer()


@router.callback_query(F.data.startswith("cat:delyes:"))
async def delete_category_apply(callback: CallbackQuery) -> None:
    category_id = int(callback.data.split(":")[2])
    async with get_session() as session:
        services = await crud.get_services_by_category(session, category_id, include_inactive=True)
        image_urls = [s.image_url for s in services]
        result = await crud.delete_category(session, category_id)
        text, keyboard = await _categories_view(session)

    if result == "missing":
        await callback.answer("Kategoriya topilmadi.", show_alert=True)
        return

    if result == "deleted":
        for url in image_urls:
            _delete_image_file(url)
        note = "✅ Kategoriya o'chirildi."
    else:
        note = "✅ Kategoriya katalogdan olib tashlandi (buyurtma tarixi saqlandi)."

    await callback.message.edit_text(f"{note}\n\n{text}", reply_markup=keyboard)
    await callback.answer()


# --- Xizmat qo'shish ---


@router.callback_query(F.data.startswith("srv:add:"))
async def add_service_start(callback: CallbackQuery, state: FSMContext) -> None:
    category_id = int(callback.data.split(":")[2])
    await state.set_state(AddService.name)
    await state.update_data(category_id=category_id)
    await callback.message.answer("Yangi xizmat nomini kiriting:")
    await callback.answer()


@router.message(AddService.name, F.text)
async def add_service_name(message: Message, state: FSMContext) -> None:
    await state.update_data(name=message.text.strip())
    await state.set_state(AddService.price)
    await message.answer("Narxini kiriting (masalan: <code>150 000 so'm</code>):")


@router.message(AddService.price, F.text)
async def add_service_price(message: Message, state: FSMContext) -> None:
    await state.update_data(price=message.text.strip())
    await state.set_state(AddService.description)
    await message.answer(
        "Xizmat haqida izoh kiriting:", reply_markup=skip_keyboard("srvadd:skip")
    )


@router.message(AddService.description, F.text)
async def add_service_description(message: Message, state: FSMContext) -> None:
    await state.update_data(description=message.text.strip())
    await state.set_state(AddService.image)
    await message.answer("Xizmat rasmini yuboring:", reply_markup=skip_keyboard("srvadd:skip"))


@router.callback_query(AddService.description, F.data == "srvadd:skip")
async def add_service_skip_description(callback: CallbackQuery, state: FSMContext) -> None:
    await state.update_data(description=None)
    await state.set_state(AddService.image)
    await callback.message.edit_reply_markup(reply_markup=None)
    await callback.message.answer(
        "Xizmat rasmini yuboring:", reply_markup=skip_keyboard("srvadd:skip")
    )
    await callback.answer()


async def _finish_add_service(message: Message, state: FSMContext, image_url: str | None) -> None:
    data = await state.get_data()
    await state.clear()
    async with get_session() as session:
        service = await crud.create_service(
            session,
            category_id=data["category_id"],
            name=data["name"],
            price=data["price"],
            description=data.get("description"),
            image_url=image_url,
        )
        view = await _service_view(session, service.id)
    text, keyboard = view
    await message.answer(f"✅ Xizmat qo'shildi.\n\n{text}", reply_markup=keyboard)


@router.message(AddService.image, F.photo)
async def add_service_image(message: Message, state: FSMContext) -> None:
    image_url = await _save_photo(message, "srv")
    await _finish_add_service(message, state, image_url)


@router.callback_query(AddService.image, F.data == "srvadd:skip")
async def add_service_skip_image(callback: CallbackQuery, state: FSMContext) -> None:
    await callback.message.edit_reply_markup(reply_markup=None)
    await _finish_add_service(callback.message, state, None)
    await callback.answer()


# --- Xizmatni tahrirlash ---


@router.callback_query(F.data.startswith("srv:edit:"))
async def edit_service_start(callback: CallbackQuery, state: FSMContext) -> None:
    _, _, field, service_id_str = callback.data.split(":")
    service_id = int(service_id_str)
    await state.set_state(EditServiceField.value)
    await state.update_data(service_id=service_id, field=field)

    keyboard = skip_keyboard("srvedit:clear") if field in {"description", "image"} else None
    prompt = FIELD_PROMPTS[field]
    if field == "description":
        prompt += "\n\n(«O'tkazib yuborish» — izohni tozalaydi)"
    elif field == "image":
        prompt += "\n\n(«O'tkazib yuborish» — rasmni olib tashlaydi)"
    await callback.message.answer(prompt, reply_markup=keyboard)
    await callback.answer()


@router.message(EditServiceField.value, F.text)
async def edit_service_text(message: Message, state: FSMContext) -> None:
    data = await state.get_data()
    field = data["field"]
    if field == "image":
        await message.answer("Iltimos, rasmni surat sifatida yuboring.")
        return

    await state.clear()
    async with get_session() as session:
        service = await crud.update_service(
            session, data["service_id"], **{field: message.text.strip()}
        )
    if service is None:
        await message.answer("Xizmat topilmadi.")
        return
    await _show_service(message, service.id)


@router.message(EditServiceField.value, F.photo)
async def edit_service_photo(message: Message, state: FSMContext) -> None:
    data = await state.get_data()
    if data["field"] != "image":
        await message.answer("Iltimos, matn ko'rinishida yuboring.")
        return

    await state.clear()
    async with get_session() as session:
        service = await crud.get_service(session, data["service_id"])
        if service is None:
            await message.answer("Xizmat topilmadi.")
            return
        old_url = service.image_url
        image_url = await _save_photo(message, f"srv{service.id}")
        await crud.update_service(session, service.id, image_url=image_url)

    if old_url != image_url:
        _delete_image_file(old_url)
    await _show_service(message, data["service_id"])


@router.callback_query(EditServiceField.value, F.data == "srvedit:clear")
async def edit_service_clear(callback: CallbackQuery, state: FSMContext) -> None:
    data = await state.get_data()
    await state.clear()
    field = data["field"]

    async with get_session() as session:
        service = await crud.get_service(session, data["service_id"])
        if service is None:
            await callback.answer("Xizmat topilmadi.", show_alert=True)
            return
        old_url = service.image_url
        await crud.update_service(session, service.id, **{field: None})

    if field == "image":
        _delete_image_file(old_url)
    await callback.message.edit_reply_markup(reply_markup=None)
    await _show_service(callback.message, data["service_id"])
    await callback.answer()


@router.callback_query(F.data.startswith("srv:toggle:"))
async def toggle_service(callback: CallbackQuery) -> None:
    service_id = int(callback.data.split(":")[2])
    async with get_session() as session:
        service = await crud.get_service(session, service_id)
        if service is None:
            await callback.answer("Xizmat topilmadi.", show_alert=True)
            return
        await crud.update_service(session, service_id, is_active=not service.is_active)
        view = await _service_view(session, service_id)
    text, keyboard = view
    await callback.message.edit_text(text, reply_markup=keyboard)
    await callback.answer("Holat o'zgartirildi")


# --- Xizmatni o'chirish ---


@router.callback_query(F.data.startswith("srv:del:"))
async def delete_service_confirm(callback: CallbackQuery) -> None:
    service_id = int(callback.data.split(":")[2])
    async with get_session() as session:
        service = await crud.get_service(session, service_id)
        if service is None:
            await callback.answer("Xizmat topilmadi.", show_alert=True)
            return
        orders_count = await crud.count_service_orders(session, service_id)

    warning = (
        f"\n\n⚠️ Bu xizmatda {orders_count} ta buyurtma bor, shuning uchun u butunlay "
        "o'chirilmaydi — faqat katalogdan yashiriladi."
        if orders_count
        else ""
    )
    await callback.message.edit_text(
        f"🗑 <b>{escape(service.name)}</b> xizmatini o'chirasizmi?{warning}",
        reply_markup=confirm_delete_keyboard("srv", service_id, f"srv:view:{service_id}"),
    )
    await callback.answer()


@router.callback_query(F.data.startswith("srv:delyes:"))
async def delete_service_apply(callback: CallbackQuery) -> None:
    service_id = int(callback.data.split(":")[2])
    async with get_session() as session:
        service: Service | None = await crud.get_service(session, service_id)
        if service is None:
            await callback.answer("Xizmat topilmadi.", show_alert=True)
            return
        category_id = service.category_id
        image_url = service.image_url
        result = await crud.delete_service(session, service_id)
        view = await _category_view(session, category_id)

    if result == "deleted":
        _delete_image_file(image_url)
        note = "✅ Xizmat o'chirildi."
    else:
        note = "✅ Xizmat katalogdan yashirildi (buyurtma tarixi saqlandi)."

    text, keyboard = view
    await callback.message.edit_text(f"{note}\n\n{text}", reply_markup=keyboard)
    await callback.answer()

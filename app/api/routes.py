from typing import Literal

from fastapi import APIRouter, Depends, HTTPException, Query, Response

from app.api.auth import telegram_user
from app.api.schemas import (
    CategoryOut,
    CategoryWithServices,
    MeOut,
    OrderIn,
    OrderOut,
    RegisterIn,
    ServiceOut,
)
from app.bot.notify import announce_new_order
from app.db.crud import (
    SORT_POPULAR,
    create_order,
    get_categories_with_services,
    get_or_create_user,
    get_service,
    get_services_by_category,
    list_brigades,
    list_services,
    save_registration,
)
from app.db.database import get_session
from app.db.models import Category

SortOption = Literal["popular", "expensive", "cheap", "new"]

router = APIRouter(prefix="/api")


@router.get("/me", response_model=MeOut)
async def me(tg_user: dict = Depends(telegram_user)):
    """Joriy foydalanuvchi ro'yxatdan o'tganmi — Mini App shu asosda ekran tanlaydi."""
    async with get_session() as session:
        user = await get_or_create_user(session, tg_user["id"])
    return MeOut(
        is_registered=user.is_registered,
        full_name=user.full_name,
        phone=user.phone,
        city=user.city,
        district=user.district,
    )


@router.post("/register", response_model=MeOut)
async def register(data: RegisterIn, tg_user: dict = Depends(telegram_user)):
    async with get_session() as session:
        user = await save_registration(
            session,
            telegram_id=tg_user["id"],
            full_name=data.full_name.strip(),
            phone=data.phone.strip(),
            city=data.city.strip(),
            district=data.district.strip(),
        )
    return MeOut(
        is_registered=user.is_registered,
        full_name=user.full_name,
        phone=user.phone,
        city=user.city,
        district=user.district,
    )


@router.post("/orders", response_model=OrderOut)
async def place_order(data: OrderIn, tg_user: dict = Depends(telegram_user)):
    """Mini App'dan buyurtma qabul qiladi.

    Buyurtma sendData orqali emas, shu endpoint orqali yuboriladi: sendData
    faqat klaviatura tugmasidan ochilgan Mini App'da ishlaydi, bu esa menyu
    tugmasi yoki to'g'ridan-to'g'ri havoladan ochilganda buyurtmani buzardi.
    """
    async with get_session() as session:
        user = await get_or_create_user(session, tg_user["id"])
        if not user.is_registered:
            raise HTTPException(status_code=403, detail="Avval ro'yxatdan o'ting")

        service = await get_service(session, data.service_id)
        if service is None or not service.is_active:
            raise HTTPException(status_code=404, detail="Bu xizmat hozircha mavjud emas")

        comment = (data.comment or "").strip() or None
        address = (data.address or "").strip() or None
        order = await create_order(session, user.id, service.id, comment, address)
        brigades = await list_brigades(session)

    await announce_new_order(order, brigades)
    return OrderOut(
        id=order.id,
        service_name=service.name,
        price=service.price,
        status=order.status.value,
    )


@router.get("/categories", response_model=list[CategoryOut])
async def list_categories() -> list[Category]:
    async with get_session() as session:
        return await get_categories_with_services(session)


@router.get("/catalog", response_model=list[CategoryWithServices])
async def full_catalog() -> list[Category]:
    async with get_session() as session:
        return await get_categories_with_services(session)


@router.get("/services", response_model=list[ServiceOut])
async def sorted_services(
    category_id: int | None = Query(default=None, description="Bo'sh bo'lsa — barcha kategoriyalar"),
    sort: SortOption = Query(default=SORT_POPULAR),
):
    async with get_session() as session:
        return await list_services(session, category_id=category_id, sort=sort)


@router.get("/categories/{category_id}/services", response_model=list[ServiceOut])
async def services_of_category(category_id: int):
    async with get_session() as session:
        return await get_services_by_category(session, category_id)


@router.get("/services/{service_id}", response_model=ServiceOut)
async def service_detail(service_id: int):
    async with get_session() as session:
        service = await get_service(session, service_id)
    if service is None:
        raise HTTPException(status_code=404, detail="Service not found")
    return service


@router.get("/services/{service_id}/image")
async def service_image(service_id: int):
    """Xizmat rasmini bazadan tarqatadi."""
    async with get_session() as session:
        service = await get_service(session, service_id)
    if service is None or not service.image_data:
        raise HTTPException(status_code=404, detail="Image not found")
    return Response(
        content=service.image_data,
        media_type=service.image_mime or "image/jpeg",
        headers={"Cache-Control": "public, max-age=86400"},
    )

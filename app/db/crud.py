from sqlalchemy import func, select
from sqlalchemy.ext.asyncio import AsyncSession
from sqlalchemy.orm import selectinload

from app.db.models import Brigade, Category, Order, OrderStatus, Service, User
from app.db.pricing import parse_price_amount

# Katalogni saralash variantlari
SORT_POPULAR = "popular"
SORT_EXPENSIVE = "expensive"
SORT_CHEAP = "cheap"
SORT_NEW = "new"
SORT_OPTIONS = (SORT_POPULAR, SORT_EXPENSIVE, SORT_CHEAP, SORT_NEW)


async def get_or_create_user(session: AsyncSession, telegram_id: int) -> User:
    result = await session.execute(select(User).where(User.telegram_id == telegram_id))
    user = result.scalar_one_or_none()
    if user is None:
        user = User(telegram_id=telegram_id)
        session.add(user)
        await session.commit()
        await session.refresh(user)
    return user


async def save_registration(
    session: AsyncSession, telegram_id: int, full_name: str, phone: str, city: str, district: str
) -> User:
    user = await get_or_create_user(session, telegram_id)
    user.full_name = full_name
    user.phone = phone
    user.city = city
    user.district = district
    user.is_registered = True
    await session.commit()
    await session.refresh(user)
    return user


async def get_categories(session: AsyncSession) -> list[Category]:
    result = await session.execute(select(Category).order_by(Category.position))
    return list(result.scalars().all())


async def get_services_by_category(
    session: AsyncSession, category_id: int, include_inactive: bool = False
) -> list[Service]:
    query = select(Service).where(Service.category_id == category_id)
    if not include_inactive:
        query = query.where(Service.is_active.is_(True))
    result = await session.execute(query.order_by(Service.id))
    return list(result.scalars().all())


async def get_service(session: AsyncSession, service_id: int) -> Service | None:
    result = await session.execute(
        select(Service).where(Service.id == service_id).options(selectinload(Service.category))
    )
    return result.scalar_one_or_none()


# --- Katalogni boshqarish (admin) ---


async def get_category(session: AsyncSession, category_id: int) -> Category | None:
    result = await session.execute(select(Category).where(Category.id == category_id))
    return result.scalar_one_or_none()


async def create_category(session: AsyncSession, name: str) -> Category:
    next_position = (await session.execute(select(func.coalesce(func.max(Category.position), -1)))).scalar_one() + 1
    category = Category(name=name, position=next_position)
    session.add(category)
    await session.commit()
    await session.refresh(category)
    return category


async def rename_category(session: AsyncSession, category_id: int, name: str) -> Category | None:
    category = await get_category(session, category_id)
    if category is None:
        return None
    category.name = name
    await session.commit()
    await session.refresh(category)
    return category


async def delete_category(session: AsyncSession, category_id: int) -> str:
    """Kategoriyani xizmatlari bilan birga o'chiradi.

    Buyurtmasi bor xizmatlar o'chirilmaydi, faqat nofaol qilinadi (tarix saqlanadi),
    shuning uchun bunday holatda kategoriya ham bazada qoladi, lekin katalogda ko'rinmaydi.

    Qaytaradi: "missing" | "deleted" | "hidden"
    """
    category = await get_category(session, category_id)
    if category is None:
        return "missing"

    services = await get_services_by_category(session, category_id, include_inactive=True)
    kept = 0
    for service in services:
        if await count_service_orders(session, service.id):
            service.is_active = False
            kept += 1
        else:
            await session.delete(service)

    if kept:
        await session.commit()
        return "hidden"

    await session.delete(category)
    await session.commit()
    return "deleted"


async def count_service_orders(session: AsyncSession, service_id: int) -> int:
    result = await session.execute(
        select(func.count()).select_from(Order).where(Order.service_id == service_id)
    )
    return result.scalar_one()


async def create_service(
    session: AsyncSession,
    category_id: int,
    name: str,
    price: str,
    description: str | None = None,
    image_data: bytes | None = None,
    image_mime: str | None = None,
) -> Service:
    service = Service(
        category_id=category_id,
        name=name,
        price=price,
        price_amount=parse_price_amount(price),
        description=description,
        image_data=image_data,
        image_mime=image_mime,
    )
    session.add(service)
    await session.commit()
    await session.refresh(service)
    return service


async def update_service(session: AsyncSession, service_id: int, **fields) -> Service | None:
    """name / price / description / image_data / image_mime / is_active maydonlarini yangilaydi."""
    allowed = {"name", "price", "description", "image_data", "image_mime", "image_url", "is_active"}
    service = await get_service(session, service_id)
    if service is None:
        return None
    for key, value in fields.items():
        if key in allowed:
            setattr(service, key, value)
    if "price" in fields:
        # narx o'zgarsa, saralash uchun ishlatiladigan son ham yangilanadi
        service.price_amount = parse_price_amount(service.price)
    await session.commit()
    await session.refresh(service)
    return service


async def delete_service(session: AsyncSession, service_id: int) -> str:
    """Buyurtmasi bor xizmat o'chirilmaydi, nofaol qilinadi.

    Qaytaradi: "missing" | "deleted" | "deactivated"
    """
    service = await get_service(session, service_id)
    if service is None:
        return "missing"
    if await count_service_orders(session, service_id):
        service.is_active = False
        await session.commit()
        return "deactivated"
    await session.delete(service)
    await session.commit()
    return "deleted"


def _order_by_sort(sort: str):
    """Saralash turini SQL ORDER BY ifodalariga aylantiradi."""
    if sort == SORT_EXPENSIVE:
        # narxi ko'rsatilmagan xizmatlar oxirida turadi
        return [Service.price_amount.is_(None), Service.price_amount.desc(), Service.id.desc()]
    if sort == SORT_CHEAP:
        return [Service.price_amount.is_(None), Service.price_amount.asc(), Service.id.desc()]
    if sort == SORT_NEW:
        return [Service.id.desc()]
    # SORT_POPULAR — buyurtmalar soni bo'yicha
    orders_count = (
        select(func.count(Order.id))
        .where(Order.service_id == Service.id)
        .correlate(Service)
        .scalar_subquery()
    )
    return [orders_count.desc(), Service.id.desc()]


async def list_services(
    session: AsyncSession, category_id: int | None = None, sort: str = SORT_POPULAR
) -> list[Service]:
    """Katalog uchun faol xizmatlar, tanlangan tartibda.

    category_id=None bo'lsa barcha kategoriyalardagi xizmatlar qaytariladi.
    """
    if sort not in SORT_OPTIONS:
        sort = SORT_POPULAR

    query = select(Service).where(Service.is_active.is_(True))
    if category_id is not None:
        query = query.where(Service.category_id == category_id)

    result = await session.execute(query.order_by(*_order_by_sort(sort)))
    return list(result.scalars().all())


async def get_categories_with_services(
    session: AsyncSession, active_only: bool = True
) -> list[Category]:
    """Katalog uchun kategoriyalar + xizmatlar.

    active_only=True bo'lsa nofaol xizmatlar va bo'sh kategoriyalar chiqmaydi.
    """
    loader = selectinload(Category.services)
    if active_only:
        loader = selectinload(Category.services.and_(Service.is_active.is_(True)))
    # populate_existing: sessiya keshidagi eski xizmatlar ro'yxatini majburan yangilaydi
    result = await session.execute(
        select(Category)
        .options(loader)
        .order_by(Category.position)
        .execution_options(populate_existing=True)
    )
    categories = list(result.scalars().all())
    if active_only:
        categories = [c for c in categories if c.services]
    return categories


# --- Buyurtmalar ---


async def create_order(
    session: AsyncSession, client_id: int, service_id: int, comment: str | None, address: str | None
) -> Order:
    order = Order(client_id=client_id, service_id=service_id, comment=comment, address=address)
    session.add(order)
    await session.commit()
    await session.refresh(order)
    result = await session.execute(
        select(Order)
        .where(Order.id == order.id)
        .options(selectinload(Order.service), selectinload(Order.client))
    )
    return result.scalar_one()


async def get_order(session: AsyncSession, order_id: int) -> Order | None:
    # populate_existing: sessiya keshidagi eski aloqalarni (masalan brigade) majburan yangilaydi
    result = await session.execute(
        select(Order)
        .where(Order.id == order_id)
        .options(selectinload(Order.service), selectinload(Order.client), selectinload(Order.brigade))
        .execution_options(populate_existing=True)
    )
    return result.scalar_one_or_none()


async def get_user_orders(session: AsyncSession, user_id: int) -> list[Order]:
    result = await session.execute(
        select(Order)
        .where(Order.client_id == user_id)
        .options(selectinload(Order.service), selectinload(Order.brigade))
        .order_by(Order.created_at.desc())
    )
    return list(result.scalars().all())


async def list_orders_by_status(session: AsyncSession, status: OrderStatus) -> list[Order]:
    result = await session.execute(
        select(Order)
        .where(Order.status == status)
        .options(selectinload(Order.service), selectinload(Order.client))
        .order_by(Order.created_at.desc())
    )
    return list(result.scalars().all())


async def list_brigades(session: AsyncSession, active_only: bool = True) -> list[Brigade]:
    query = select(Brigade)
    if active_only:
        query = query.where(Brigade.is_active.is_(True))
    result = await session.execute(query)
    return list(result.scalars().all())


async def create_brigade(
    session: AsyncSession, name: str, phone: str, specialty: str, telegram_id: int | None = None
) -> Brigade:
    brigade = Brigade(name=name, phone=phone, specialty=specialty, telegram_id=telegram_id)
    session.add(brigade)
    await session.commit()
    await session.refresh(brigade)
    return brigade


async def assign_brigade(session: AsyncSession, order_id: int, brigade_id: int) -> Order | None:
    order = await get_order(session, order_id)
    if order is None:
        return None
    order.brigade_id = brigade_id
    order.status = OrderStatus.ASSIGNED
    await session.commit()
    return await get_order(session, order_id)


async def update_order_status(session: AsyncSession, order_id: int, status: OrderStatus) -> Order | None:
    order = await get_order(session, order_id)
    if order is None:
        return None
    order.status = status
    await session.commit()
    return await get_order(session, order_id)

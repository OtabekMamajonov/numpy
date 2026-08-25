from sqlalchemy import select
from sqlalchemy.ext.asyncio import AsyncSession
from sqlalchemy.orm import selectinload

from app.db.models import Brigade, Category, Order, OrderStatus, Service, User


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


async def get_services_by_category(session: AsyncSession, category_id: int) -> list[Service]:
    result = await session.execute(
        select(Service).where(Service.category_id == category_id, Service.is_active.is_(True))
    )
    return list(result.scalars().all())


async def get_service(session: AsyncSession, service_id: int) -> Service | None:
    result = await session.execute(
        select(Service).where(Service.id == service_id).options(selectinload(Service.category))
    )
    return result.scalar_one_or_none()


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

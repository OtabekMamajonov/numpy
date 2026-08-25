from sqlalchemy.ext.asyncio import AsyncSession, async_sessionmaker, create_async_engine
from sqlalchemy.orm import DeclarativeBase

from app.config import settings


class Base(DeclarativeBase):
    pass


engine = create_async_engine(settings.database_url, echo=False)
async_session = async_sessionmaker(engine, expire_on_commit=False)


async def _migrate(conn) -> None:
    """Eski bazalarga yangi ustunlarni qo'shadi.

    create_all mavjud jadvallarni o'zgartirmaydi, shuning uchun avval ishga
    tushirilgan bazalarda yangi ustun bo'lmay qoladi va so'rovlar xato beradi.
    """
    from app.db.pricing import parse_price_amount

    rows = await conn.exec_driver_sql("PRAGMA table_info(services)")
    columns = {row[1] for row in rows}

    if "price_amount" not in columns:
        await conn.exec_driver_sql("ALTER TABLE services ADD COLUMN price_amount INTEGER")

    # Bo'sh qolgan qatorlarni to'ldiradi: eski baza yozuvlari yoki bazaga
    # to'g'ridan-to'g'ri qo'shilgan xizmatlar ham saralashda qatnashsin.
    result = await conn.exec_driver_sql(
        "SELECT id, price FROM services WHERE price_amount IS NULL"
    )
    for service_id, price in result.fetchall():
        amount = parse_price_amount(price)
        if amount is not None:
            await conn.exec_driver_sql(
                "UPDATE services SET price_amount = ? WHERE id = ?", (amount, service_id)
            )


async def init_db() -> None:
    async with engine.begin() as conn:
        await conn.run_sync(Base.metadata.create_all)
        await _migrate(conn)


def get_session() -> AsyncSession:
    return async_session()

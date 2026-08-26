from sqlalchemy import inspect, text
from sqlalchemy.ext.asyncio import AsyncSession, async_sessionmaker, create_async_engine
from sqlalchemy.orm import DeclarativeBase

from app.config import settings


class Base(DeclarativeBase):
    pass


if not settings.is_postgres:
    # SQLite fayli uchun papka mavjud bo'lishi kerak
    settings.data_path.mkdir(parents=True, exist_ok=True)

engine = create_async_engine(settings.db_url, echo=False)
async_session = async_sessionmaker(engine, expire_on_commit=False)

# Keyinchalik qo'shilgan ustunlar: (jadval, ustun, SQL turi)
# create_all mavjud jadvallarni o'zgartirmaydi, shuning uchun avval yaratilgan
# bazalarda bu ustunlar bo'lmay qoladi va so'rovlar xato beradi.
_ADDED_COLUMNS = [
    ("services", "price_amount", "INTEGER"),
    ("services", "image_data", {"postgres": "BYTEA", "sqlite": "BLOB"}),
    ("services", "image_mime", "VARCHAR(64)"),
]


def _column_type(spec) -> str:
    if isinstance(spec, dict):
        return spec["postgres"] if settings.is_postgres else spec["sqlite"]
    return spec


async def _migrate(conn) -> None:
    from app.db.pricing import parse_price_amount

    existing = await conn.run_sync(
        lambda sync_conn: {
            table: {col["name"] for col in inspect(sync_conn).get_columns(table)}
            for table in {t for t, _, _ in _ADDED_COLUMNS}
            if inspect(sync_conn).has_table(table)
        }
    )

    for table, column, type_spec in _ADDED_COLUMNS:
        if table in existing and column not in existing[table]:
            await conn.execute(
                text(f"ALTER TABLE {table} ADD COLUMN {column} {_column_type(type_spec)}")
            )

    # Narx soni yozilmagan qatorlarni to'ldirish (eski yozuvlar yoki bazaga
    # to'g'ridan-to'g'ri qo'shilganlar ham saralashda qatnashsin)
    rows = (await conn.execute(text("SELECT id, price FROM services WHERE price_amount IS NULL"))).all()
    for service_id, price in rows:
        amount = parse_price_amount(price)
        if amount is not None:
            await conn.execute(
                text("UPDATE services SET price_amount = :amount WHERE id = :id"),
                {"amount": amount, "id": service_id},
            )


async def init_db() -> None:
    async with engine.begin() as conn:
        await conn.run_sync(Base.metadata.create_all)
        await _migrate(conn)


def get_session() -> AsyncSession:
    return async_session()

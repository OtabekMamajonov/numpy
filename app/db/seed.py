"""Demo katalog ma'lumotlarini bazaga qo'shish uchun skript.

Ishga tushirish: python -m app.db.seed
"""
import asyncio

from app.db.database import async_session, init_db
from app.db.models import Category, Service

DEMO_DATA = [
    (
        "Santexnika ishlari",
        [
            ("Kran/aralashtirgich o'rnatish", "Yangi yoki eski kranni almashtirish, ulash", "150 000 so'm"),
            ("Quvur almashtirish", "Metall yoki plastik quvurlarni yangilash", "kv.metriga 80 000 so'm"),
            ("Unitaz o'rnatish", "Unitaz, bidye o'rnatish va ulash", "200 000 so'm"),
        ],
    ),
    (
        "Elektr montaj ishlari",
        [
            ("Elektr provodkasi", "Uy/ofis uchun to'liq elektr tarmog'ini tortish", "kv.metriga 60 000 so'm"),
            ("Rozetka/vyklyuchatel o'rnatish", "Rozetka va vyklyuchatellarni o'rnatish", "30 000 so'm/dona"),
            ("Elektr shchit yig'ish", "Avtomat va shchitlarni o'rnatish", "500 000 so'm"),
        ],
    ),
    (
        "Ta'mirlash (remont)",
        [
            ("Devor shpaklyovka", "Devorlarni tekislash va shpaklyovka qilish", "kv.metriga 25 000 so'm"),
            ("Plitka yotqizish", "Pol yoki devorga kafel/plitka yotqizish", "kv.metriga 70 000 so'm"),
            ("Bo'yoq ishlari", "Devor va shiftni bo'yash", "kv.metriga 20 000 so'm"),
        ],
    ),
]


async def seed() -> None:
    await init_db()
    async with async_session() as session:
        for cat_index, (cat_name, services) in enumerate(DEMO_DATA):
            category = Category(name=cat_name, position=cat_index)
            session.add(category)
            await session.flush()
            for name, description, price in services:
                session.add(
                    Service(
                        category_id=category.id,
                        name=name,
                        description=description,
                        price=price,
                        image_url=None,
                    )
                )
        await session.commit()
    print("Demo katalog muvaffaqiyatli qo'shildi.")


if __name__ == "__main__":
    asyncio.run(seed())

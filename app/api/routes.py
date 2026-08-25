from fastapi import APIRouter, HTTPException
from sqlalchemy import select
from sqlalchemy.orm import selectinload

from app.api.schemas import CategoryOut, CategoryWithServices, ServiceOut
from app.db.crud import get_service, get_services_by_category
from app.db.database import get_session
from app.db.models import Category

router = APIRouter(prefix="/api")


@router.get("/categories", response_model=list[CategoryOut])
async def list_categories() -> list[Category]:
    async with get_session() as session:
        result = await session.execute(select(Category).order_by(Category.position))
        return list(result.scalars().all())


@router.get("/catalog", response_model=list[CategoryWithServices])
async def full_catalog() -> list[Category]:
    async with get_session() as session:
        result = await session.execute(
            select(Category).options(selectinload(Category.services)).order_by(Category.position)
        )
        return list(result.scalars().all())


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

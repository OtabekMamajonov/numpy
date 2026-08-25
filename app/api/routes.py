from typing import Literal

from fastapi import APIRouter, HTTPException, Query

from app.api.schemas import CategoryOut, CategoryWithServices, ServiceOut
from app.db.crud import (
    SORT_POPULAR,
    get_categories_with_services,
    get_service,
    get_services_by_category,
    list_services,
)
from app.db.database import get_session
from app.db.models import Category

SortOption = Literal["popular", "expensive", "cheap", "new"]

router = APIRouter(prefix="/api")


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

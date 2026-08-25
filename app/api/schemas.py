from pydantic import BaseModel, ConfigDict, Field


class ServiceOut(BaseModel):
    model_config = ConfigDict(from_attributes=True)

    id: int
    category_id: int
    name: str
    description: str | None
    price: str
    image_url: str | None


class CategoryOut(BaseModel):
    model_config = ConfigDict(from_attributes=True)

    id: int
    name: str


class CategoryWithServices(CategoryOut):
    services: list[ServiceOut]


class MeOut(BaseModel):
    is_registered: bool
    full_name: str | None = None
    phone: str | None = None
    city: str | None = None
    district: str | None = None


class RegisterIn(BaseModel):
    full_name: str = Field(min_length=2, max_length=255)
    phone: str = Field(min_length=5, max_length=32)
    city: str = Field(min_length=2, max_length=128)
    district: str = Field(min_length=2, max_length=128)

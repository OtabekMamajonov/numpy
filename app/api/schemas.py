from pydantic import BaseModel, ConfigDict, Field


class ServiceOut(BaseModel):
    model_config = ConfigDict(from_attributes=True, populate_by_name=True)

    id: int
    category_id: int
    name: str
    description: str | None
    price: str
    # Rasm bazada bo'lsa — uni tarqatuvchi endpoint manzili, aks holda tashqi URL
    image_url: str | None = Field(default=None, validation_alias="public_image_url")


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

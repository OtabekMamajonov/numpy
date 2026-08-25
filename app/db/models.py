import enum
from datetime import datetime

from sqlalchemy import BigInteger, DateTime, Enum, ForeignKey, Integer, String, Text, func
from sqlalchemy.orm import Mapped, mapped_column, relationship

from app.db.database import Base


class OrderStatus(str, enum.Enum):
    NEW = "new"
    ASSIGNED = "assigned"
    IN_PROGRESS = "in_progress"
    DONE = "done"
    CANCELLED = "cancelled"


class User(Base):
    __tablename__ = "users"

    id: Mapped[int] = mapped_column(Integer, primary_key=True)
    telegram_id: Mapped[int] = mapped_column(BigInteger, unique=True, index=True)
    full_name: Mapped[str | None] = mapped_column(String(255), nullable=True)
    phone: Mapped[str | None] = mapped_column(String(32), nullable=True)
    city: Mapped[str | None] = mapped_column(String(128), nullable=True)
    district: Mapped[str | None] = mapped_column(String(128), nullable=True)
    is_registered: Mapped[bool] = mapped_column(default=False)
    created_at: Mapped[datetime] = mapped_column(DateTime(timezone=True), server_default=func.now())

    orders: Mapped[list["Order"]] = relationship(back_populates="client")


class Category(Base):
    __tablename__ = "categories"

    id: Mapped[int] = mapped_column(Integer, primary_key=True)
    name: Mapped[str] = mapped_column(String(128))
    position: Mapped[int] = mapped_column(Integer, default=0)

    services: Mapped[list["Service"]] = relationship(back_populates="category")


class Service(Base):
    __tablename__ = "services"

    id: Mapped[int] = mapped_column(Integer, primary_key=True)
    category_id: Mapped[int] = mapped_column(ForeignKey("categories.id"))
    name: Mapped[str] = mapped_column(String(255))
    description: Mapped[str | None] = mapped_column(Text, nullable=True)
    price: Mapped[str] = mapped_column(String(64))
    # Narx matnidan ajratilgan son — faqat saralash uchun ishlatiladi
    price_amount: Mapped[int | None] = mapped_column(Integer, nullable=True)
    image_url: Mapped[str | None] = mapped_column(String(512), nullable=True)
    is_active: Mapped[bool] = mapped_column(default=True)

    category: Mapped["Category"] = relationship(back_populates="services")


class Brigade(Base):
    __tablename__ = "brigades"

    id: Mapped[int] = mapped_column(Integer, primary_key=True)
    name: Mapped[str] = mapped_column(String(255))
    phone: Mapped[str | None] = mapped_column(String(32), nullable=True)
    specialty: Mapped[str | None] = mapped_column(String(255), nullable=True)
    telegram_id: Mapped[int | None] = mapped_column(BigInteger, nullable=True)
    is_active: Mapped[bool] = mapped_column(default=True)
    created_at: Mapped[datetime] = mapped_column(DateTime(timezone=True), server_default=func.now())

    orders: Mapped[list["Order"]] = relationship(back_populates="brigade")


class Order(Base):
    __tablename__ = "orders"

    id: Mapped[int] = mapped_column(Integer, primary_key=True)
    client_id: Mapped[int] = mapped_column(ForeignKey("users.id"))
    service_id: Mapped[int] = mapped_column(ForeignKey("services.id"))
    brigade_id: Mapped[int | None] = mapped_column(ForeignKey("brigades.id"), nullable=True)
    comment: Mapped[str | None] = mapped_column(Text, nullable=True)
    address: Mapped[str | None] = mapped_column(String(512), nullable=True)
    status: Mapped[OrderStatus] = mapped_column(Enum(OrderStatus), default=OrderStatus.NEW)
    created_at: Mapped[datetime] = mapped_column(DateTime(timezone=True), server_default=func.now())
    updated_at: Mapped[datetime] = mapped_column(DateTime(timezone=True), server_default=func.now(), onupdate=func.now())

    client: Mapped["User"] = relationship(back_populates="orders")
    service: Mapped["Service"] = relationship()
    brigade: Mapped["Brigade | None"] = relationship(back_populates="orders")

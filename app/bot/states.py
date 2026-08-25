from aiogram.fsm.state import State, StatesGroup


class Registration(StatesGroup):
    full_name = State()
    phone = State()
    city = State()
    district = State()


class AddBrigade(StatesGroup):
    name = State()
    phone = State()
    specialty = State()


class AddCategory(StatesGroup):
    name = State()


class RenameCategory(StatesGroup):
    name = State()


class AddService(StatesGroup):
    name = State()
    price = State()
    description = State()
    image = State()


class EditServiceField(StatesGroup):
    value = State()

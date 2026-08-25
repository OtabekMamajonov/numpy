from aiogram.fsm.state import State, StatesGroup


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

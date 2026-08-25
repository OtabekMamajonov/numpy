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

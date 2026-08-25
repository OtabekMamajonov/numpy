"""Narx matnidan raqamli qiymatni ajratib olish.

Narx katalogda erkin matn ko'rinishida saqlanadi ("kv.metriga 80 000 so'm"),
chunki admin o'lchov birligini ham yozadi. Saralash uchun esa son kerak,
shuning uchun matndan eng katta son ajratib olinadi va alohida ustunga yoziladi.
"""
import re

# Ko'p xonali son (bo'sh joy / nuqta / vergul bilan ajratilgan) yoki bitta raqam
_NUMBER_RE = re.compile(r"\d[\d\s.,]*\d|\d")


def parse_price_amount(price: str | None) -> int | None:
    """Narx matnidagi eng katta sonni qaytaradi, topilmasa None.

    "150 000 so'm"            -> 150000
    "kv.metriga 80 000 so'm"  -> 80000
    "2 xonali 500 000 so'm"   -> 500000
    "kelishilgan holda"       -> None
    """
    if not price:
        return None

    amounts = []
    for match in _NUMBER_RE.findall(price):
        digits = re.sub(r"\D", "", match)
        if digits:
            amounts.append(int(digits))

    return max(amounts) if amounts else None

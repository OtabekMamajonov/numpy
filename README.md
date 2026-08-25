# Qurilish xizmatlari — Telegram bot + Mini App

Qurilish ishlari uchun vositachi platforma. Mijozlar bot orqali ro'yxatdan o'tadi,
Mini App katalogidan xizmat tanlab buyurtma beradi; admin buyurtmani qabul qilib,
o'z brigadalaridan biriga biriktiradi.

## Nima qanday ishlaydi

1. **`/start`** — foydalanuvchidan ism-familiya, telefon raqam, shahar va tuman so'raladi va bazaga saqlanadi.
2. **🛠 Katalog** tugmasi Telegram Mini App'ni ochadi — kategoriyalar, xizmatlar, narxlar va rasmlar. Xizmatlarni saralash mumkin: 🔥 Ommabop · 💎 Qimmat · 💰 Arzon · 🆕 Yangi.
3. Foydalanuvchi xizmatni tanlab, manzil/izoh qo'shib **Buyurtma berish** tugmasini bosadi.
4. Buyurtma bazaga yoziladi va barcha adminlarga brigadalar ro'yxati bilan yuboriladi.
5. Admin tugma orqali brigadani biriktiradi — mijozga ham, brigadaga ham xabar boradi.
6. Mijoz **📋 Mening buyurtmalarim** orqali holatni kuzatadi.

Buyurtma holatlari: `new` → `assigned` → `in_progress` → `done` (yoki `cancelled`).

## Loyiha tuzilishi

```
app/
  config.py            .env sozlamalari
  db/
    database.py        SQLAlchemy engine va sessiya
    models.py          User, Category, Service, Brigade, Order
    crud.py            baza bilan ishlash funksiyalari, saralash
    pricing.py         narx matnidan sonni ajratib olish
    seed.py            demo katalog ma'lumotlari
  bot/
    main.py            bot ishga tushirish nuqtasi
    states.py          FSM holatlari
    keyboards.py       klaviaturalar (WebApp tugmasi shu yerda)
    handlers/
      registration.py  /start va ro'yxatdan o'tish
      catalog.py       WebApp'dan kelgan buyurtma, "mening buyurtmalarim"
      admin.py         buyurtmalar, brigadalar, brigadaga biriktirish
      catalog_admin.py katalogni boshqarish (qo'shish/tahrirlash/o'chirish)
  api/
    main.py            FastAPI ilova
    routes.py          katalog endpointlari
    schemas.py         Pydantic modellari
webapp/                React (Vite) Mini App
static/images/         xizmat rasmlari
```

## O'rnatish

```bash
python3 -m venv .venv
.venv/bin/pip install -r requirements.txt

cp .env.example .env   # keyin .env ni to'ldiring
```

`.env` da to'ldirilishi kerak:

| O'zgaruvchi | Izoh |
|---|---|
| `BOT_TOKEN` | [@BotFather](https://t.me/BotFather) dan olingan token |
| `ADMIN_IDS` | Admin Telegram ID lari, vergul bilan (masalan `111111,222222`) |
| `WEBAPP_URL` | Mini App joylashgan **HTTPS** manzil (Telegram faqat HTTPS qabul qiladi) |
| `DATABASE_URL` | Odatda o'zgartirish shart emas |

## Ishga tushirish

Demo katalogni bazaga yuklash (bir marta):

```bash
.venv/bin/python -m app.db.seed
```

Mini App'ni build qilish:

```bash
cd webapp && npm install && npm run build
```

Backend (katalog API + build qilingan Mini App'ni tarqatadi):

```bash
.venv/bin/uvicorn app.api.main:app --host 0.0.0.0 --port 8000
```

Bot:

```bash
.venv/bin/python -m app.bot.main
```

## Mini App dizayni

Mini App to'q (dark) mavzuda ishlangan: deyarli qora fon, ekran tepasida
to'q-yashil nur, urg'u rangi sifatida yorqin nane (mint) rangi, dumaloq
burchakli kartochkalar va pill shaklidagi tugmalar.

Ranglar `webapp/src/styles.css` faylining boshidagi CSS o'zgaruvchilarida
yig'ilgan — mavzuni o'zgartirish uchun shu yerdagi qiymatlarni tahrirlash
yetarli:

| O'zgaruvchi | Vazifasi |
|---|---|
| `--bg` | Asosiy fon |
| `--accent`, `--accent-top`, `--accent-bottom` | Urg'u (tugmalar, narxlar) |
| `--surface`, `--border` | Kartochka va maydonlar |
| `--text`, `--muted` | Matn ranglari |

Dizayn qat'iy to'q mavzuda — Telegram'ning yorug'/to'q sozlamasiga
moslashmaydi, chunki butun ko'rinish shu palitraga qurilgan. Telegram
sarlavhasi ham ilova foni bilan bir xil rangga bo'yaladi.

### Frontend ustida ishlash

```bash
cd webapp && npm run dev
```

Vite dev-server `/api` so'rovlarini `localhost:8000` ga uzatadi.

> Telegram Mini App faqat HTTPS orqali ochiladi. Lokal test uchun `ngrok http 8000`
> kabi tunnel ishlatib, olingan HTTPS manzilni `WEBAPP_URL` ga yozing.

## Admin buyruqlari

| Buyruq | Vazifasi |
|---|---|
| `/admin` | Admin menyusi |
| `/catalog` | Katalogni boshqarish (qo'shish, tahrirlash, o'chirish) |
| `/new_orders` | Yangi (biriktirilmagan) buyurtmalar |
| `/brigades` | Brigadalar ro'yxati |
| `/addbrigade` | Yangi brigada qo'shish |

Brigadaga botdan xabar borishi uchun `brigades.telegram_id` ustuniga brigada
rahbarining Telegram ID sini yozib qo'ying.

## Katalogni boshqarish

Butun katalog **bot ichidan**, `/catalog` buyrug'i orqali boshqariladi — kodga
tegish yoki bazani qo'lda tahrirlash shart emas.

```
/catalog
 └── Kategoriyalar ro'yxati        [➕ Yangi kategoriya]
      └── Kategoriya               [➕ Xizmat qo'shish] [✏️ Nomi] [🗑 O'chirish]
           └── Xizmat              [✏️ Nomi] [💰 Narxi] [📝 Izoh] [🖼 Rasm]
                                   [🔴 Nofaol qilish] [🗑 O'chirish]
```

**Xizmat qo'shish:** nomi → narxi → izoh → rasm. Izoh va rasm majburiy emas —
«⏭ O'tkazib yuborish» tugmasi bilan tashlab ketish mumkin. Rasm oddiy surat
sifatida yuboriladi, bot uni `static/images/` ga saqlaydi va Mini App'da
avtomatik ko'rsatadi.

**Tahrirlash:** xizmatni ochib, kerakli maydon tugmasini bosing va yangi qiymatni
yuboring. Izoh yoki rasmni butunlay olib tashlash uchun «⏭ O'tkazib yuborish»
tugmasidan foydalaning.

**Nofaol qilish:** xizmatni o'chirmasdan katalogdan vaqtincha yashiradi
(🔴). Keyin xohlagan paytda qayta yoqish mumkin.

**O'chirish:** buyurtma tarixini buzmaydi. Agar xizmatga buyurtma berilgan
bo'lsa, u butunlay o'chirilmaydi — faqat katalogdan yashiriladi, shunda eski
buyurtmalar ro'yxati ishlashda davom etadi. Buyurtmasi yo'q xizmatlar rasmi
bilan birga to'liq o'chadi. Kategoriya o'chirilganda ichidagi xizmatlar ham shu
qoida bo'yicha o'chadi.

Katalogda faqat **faol xizmatlar** va **kamida bitta faol xizmati bor
kategoriyalar** ko'rinadi.

### Saralash qanday ishlaydi

Mini App'da foydalanuvchi xizmatlarni tartiblashi mumkin:

| Tugma | Tartib |
|---|---|
| 🔥 Ommabop | Buyurtmalar soni bo'yicha (ko'pdan kamga) |
| 💎 Qimmat | Narxi bo'yicha (qimmatdan arzonga) |
| 💰 Arzon | Narxi bo'yicha (arzondan qimmatga) |
| 🆕 Yangi | Katalogga qo'shilgan tartibi bo'yicha (yangidan eskiga) |

Saralash tanlangan kategoriya ichida ishlaydi; **Hammasi** tabida esa butun
katalog bo'yicha.

Narx erkin matn sifatida saqlanadi ("kv.metriga 80 000 so'm"), shuning uchun bot
undan sonni avtomatik ajratib olib `services.price_amount` ustuniga yozadi va
saralashda o'sha sondan foydalanadi. **Admin uchun hech narsa o'zgarmaydi** —
narxni avvalgidek yozaverasiz. Agar matnda umuman son bo'lmasa (masalan
"kelishilgan holda"), bunday xizmat narx bo'yicha saralashda ro'yxat oxirida
turadi.

Xohlasangiz, boshlang'ich demo katalogni `app/db/seed.py` orqali ham yuklashingiz
mumkin.

## API endpointlari

| Metod | Yo'l | Qaytaradi |
|---|---|---|
| GET | `/api/categories` | Kategoriyalar ro'yxati |
| GET | `/api/catalog` | Kategoriyalar + ichidagi xizmatlar |
| GET | `/api/services?sort=&category_id=` | Saralangan xizmatlar (`sort`: `popular`, `expensive`, `cheap`, `new`; `category_id` bo'sh bo'lsa — hammasi) |
| GET | `/api/categories/{id}/services` | Kategoriyadagi xizmatlar |
| GET | `/api/services/{id}` | Bitta xizmat |

## Keyingi bosqichda qo'shish mumkin

- Kategoriyalar tartibini o'zgartirish (yuqoriga/pastga)

- Brigada reytingi va mijoz sharhlari
- Click/Payme orqali to'lov
- Veb admin panel (statistika bilan)
- Ko'p tillilik (o'zbek / rus)
- Telegram `location` orqali aniq manzil

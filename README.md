# Qurilish xizmatlari — Telegram bot + Mini App

Qurilish ishlari uchun vositachi platforma. Mijozlar bot orqali ro'yxatdan o'tadi,
Mini App katalogidan xizmat tanlab buyurtma beradi; admin buyurtmani qabul qilib,
o'z brigadalaridan biriga biriktiradi.

## Nima qanday ishlaydi

1. **`/start`** — bot salomlashadi va **🛠 Katalog** tugmasini ko'rsatadi.
2. Tugma Telegram Mini App'ni ochadi. Foydalanuvchi hali ro'yxatdan o'tmagan bo'lsa, avval qisqa forma chiqadi: ism-familiya, telefon, shahar va tuman. Keyin katalog ochiladi — kategoriyalar, xizmatlar, narxlar va rasmlar. Xizmatlarni saralash mumkin: Ommabop · Qimmat · Arzon · Yangi.
3. Foydalanuvchi xizmatni tanlab, manzil/izoh qo'shib **Buyurtma berish** tugmasini bosadi.
4. Buyurtma bazaga yoziladi; mijozga tasdiq, adminlarga esa brigadalar ro'yxati bilan xabar boradi.
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
      registration.py  /start va asosiy menyu
      catalog.py       WebApp'dan kelgan buyurtma, "mening buyurtmalarim"
      admin.py         buyurtmalar, brigadalar, brigadaga biriktirish
      catalog_admin.py katalogni boshqarish (qo'shish/tahrirlash/o'chirish)
  main.py              bot + API'ni birga ishga tushirish
  api/
    main.py            FastAPI ilova
    routes.py          katalog va ro'yxatdan o'tish endpointlari
    auth.py            Telegram initData imzosini tekshirish
    webhook.py         Telegram webhook endpointi
    schemas.py         Pydantic modellari
webapp/                React (Vite) Mini App
static/images/         xizmat rasmlari
```

## Tez sinash (kompyuterda, bepul)

Telegram Mini App'ni faqat **HTTPS** manzildan ochadi, shuning uchun lokal
serverni [ngrok](https://ngrok.com/download) orqali tashqariga chiqaramiz.

**1.** `.env` faylini yarating:

```
BOT_TOKEN=<@BotFather bergan token>
ADMIN_IDS=<sizning Telegram ID'ingiz>
WEBAPP_URL=
```

**2.** Alohida terminalda ngrok'ni yoqing:

```bash
ngrok http 8000
```

Chiqqan `https://xxxx.ngrok-free.app` manzilini nusxalang.

**3.** Uni `.env` dagi `WEBAPP_URL` ga yozing:

```
WEBAPP_URL=https://xxxx.ngrok-free.app
```

**4.** Loyihani ishga tushiring:

```bash
./run.sh
```

Skript o'zi: Python muhitini yaratadi, bog'liqliklarni o'rnatadi, Mini App'ni
build qiladi, demo katalogni yuklaydi va bot bilan API'ni yoqadi.

**5.** Telegram'da botingizga `/start` yozing va **🛠 Katalog** tugmasini bosing.

> ngrok manzili har safar yangilanadi — qayta yoqqaningizda `WEBAPP_URL` ni
> yangilab, `./run.sh` ni qayta ishga tushiring.

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
| `ADMIN_IDS` | Admin Telegram ID lari, vergul bilan (masalan `111111,222222`). O'z ID'ingizni [@userinfobot](https://t.me/userinfobot) dan bilib olasiz |
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

## Mini App'ni BotFather'ga qo'shish kerakmi?

**Shart emas.** Mini App — bu oddiy sayt: u HTTPS manzilga joylashtiriladi va
Telegram o'sha manzilni o'z ichida ochadi. Bot `🛠 Katalog` tugmasini
yuborayotganda manzilni tugma ichiga qo'shib yuboradi, shuning uchun qo'shimcha
ro'yxatdan o'tkazish talab qilinmaydi.

Xohlasangiz [@BotFather](https://t.me/BotFather) orqali qulaylik qo'shishingiz
mumkin:

| Buyruq | Nima beradi |
|---|---|
| `/setmenubutton` | Xabar maydoni yonida doimiy tugma |
| `/newapp` | `t.me/botingiz/app` ko'rinishidagi to'g'ridan-to'g'ri havola |

Ikkalasida ham xuddi shu manzil ko'rsatiladi.

> Mini App qayerdan ochilishidan qat'i nazar hammasi ishlaydi: ro'yxatdan
> o'tish ham, buyurtma berish ham API orqali boradi. (Telegram'ning
> `sendData()` usuli faqat klaviatura tugmasidan ochilganda ishlaydi, shuning
> uchun undan foydalanilmaydi — aks holda menyu tugmasidan ochilganda buyurtma
> jimgina yo'qolardi.)

## Xavfsizlik: Mini App so'rovlari qanday tekshiriladi

Ro'yxatdan o'tish Mini App ichida bo'lgani uchun, server so'rov haqiqatan ham
Telegram'dan kelganini tekshirishi shart — aks holda istalgan odam boshqa
foydalanuvchi nomidan yozuv yaratishi mumkin bo'lardi.

Telegram har bir Mini App so'roviga bot tokeni bilan imzolangan `initData`
qatorini qo'shadi. Frontend uni `Authorization: tma <initData>` sarlavhasida
yuboradi, backend esa `app/api/auth.py` da HMAC-SHA256 imzosini qayta hisoblab
solishtiradi va `auth_date` eskirmaganini tekshiradi. Imzo mos kelmasa yoki
ma'lumot o'zgartirilgan bo'lsa — `401`.

Shu sababli `BOT_TOKEN` maxfiy: u nafaqat botni boshqaradi, balki Mini App
so'rovlarining haqiqiyligini ham tasdiqlaydi. Token tasodifan oshkor bo'lsa,
[@BotFather](https://t.me/BotFather) da `/revoke` orqali yangilang.

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

## Bepul joylashtirish (telefondan ham bo'ladi)

Ikkita bepul xizmat kifoya: **Neon** (Postgres) va **Render** (ilova). Ikkalasi
ham brauzerdan sozlanadi.

### 1. Bepul Postgres

[neon.tech](https://neon.tech) da ro'yxatdan o'ting → yangi loyiha yarating →
**Connection string** ni nusxalang (`postgres://...` ko'rinishida).

> Nega Postgres? Bepul hostinglarda doimiy disk bo'lmaydi — SQLite fayli har
> qayta joylashtirishda o'chib ketardi va foydalanuvchilar qayta ro'yxatdan
> o'tishga majbur bo'lardi.

### 2. Ilovani joylashtirish

[render.com](https://render.com) da ro'yxatdan o'ting →
**New → Blueprint** → shu GitHub reponi tanlang → branchni tanlang.

Render `render.yaml` ni o'qib xizmatni o'zi sozlaydi. Sizdan faqat uchta
qiymat so'raydi:

| O'zgaruvchi | Qiymat |
|---|---|
| `BOT_TOKEN` | @BotFather bergan token |
| `ADMIN_IDS` | Sizning Telegram ID'ingiz ([@userinfobot](https://t.me/userinfobot) aytadi) |
| `DATABASE_URL` | Neon bergan connection string |

Qolganini Render o'zi to'ldiradi: manzil, webhook kaliti va demo katalog.

### 3. Tayyor

Deploy tugagach Telegram'da botga `/start` yozing va **🛠 Katalog** tugmasini
bosing. `WEBAPP_URL` ni qo'lda yozish shart emas — Render manzilni o'zi beradi.

> **Bepul tarifning kamchiligi:** xizmat 15 daqiqa harakatsizlikdan keyin
> uxlaydi. Shu sababli bot **webhook** rejimida ishlaydi — Telegram
> yangilanishni yuborganda xizmat uyg'onadi. Uzoq tanaffusdan keyingi birinchi
> xabarga javob ~1 daqiqa kechikishi mumkin, keyingilari darhol ishlaydi.

## Serverga joylashtirish

Bot va API **bitta jarayonda** ishlaydi (`app/main.py`), chunki ikkalasi bir xil
SQLite faylini ko'rishi kerak — alohida konteynerlarda ikki xil baza bo'lib
qolardi:

```bash
.venv/bin/python -m app.main
```

Portni `PORT` o'zgaruvchisi belgilaydi (server odatda o'zi beradi).

**Ma'lumotlar qayerda saqlanadi.** Konteyner fayl tizimi vaqtinchalik, shuning
uchun:

- **Baza** — `DATABASE_URL` ko'rsatilsa Postgres'da. Ko'rsatilmasa SQLite
  (`DATA_DIR` ichida), bu esa doimiy disk ulangan hostlarda yoki lokalda mos.
- **Xizmat rasmlari** — bazada saqlanadi (fayl tizimida emas), shuning uchun
  qayta joylashtirishda yo'qolmaydi.

**Bot rejimi.** Xizmat uzluksiz ishlaydigan hostda polling yetarli. Uxlab
qoladigan hostda `USE_WEBHOOK=true` qiling — Telegram yangilanishni yuborganda
xizmat uyg'onadi. Webhook manzili ochiq bo'lgani uchun `WEBHOOK_SECRET` ni
to'ldiring: server har bir so'rovni shu kalit bilan tekshiradi.

`Dockerfile` har qanday hostga mos (Render, Fly.io, Koyeb va h.k.):
Node bilan Mini App build qilinadi, so'ng Python muhitida ishga tushadi.
`nixpacks.toml` esa Railway uchun.

Joylashtirgandan keyin `WEBAPP_URL` ga olingan HTTPS manzilni yozing (Render'da
avtomatik). [@BotFather](https://t.me/BotFather) da qo'shimcha sozlash shart
emas — Mini App tugmasi shu manzildan ochiladi.

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
| GET | `/api/me` | Joriy foydalanuvchi ro'yxatdan o'tganmi (auth talab qiladi) |
| POST | `/api/register` | Ro'yxatdan o'tkazish (auth talab qiladi) |
| POST | `/api/orders` | Buyurtma berish (auth talab qiladi) |
| GET | `/api/services/{id}/image` | Xizmat rasmi (bazadan) |
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

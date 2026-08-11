# Do'kon Hisob-Kitob

Kichik oziq-ovqat do'konlari uchun Android hisob-kitob ilovasi. Kotlin + Jetpack
Compose + Room ustida qurilgan, OpenAI API bilan integratsiya qilingan AI
yordamchiga ega.

## Funksiyalar

- **Boshqaruv paneli** — bugungi/haftalik savdo, sof foyda, umumiy qarzlar,
  kam qolgan mahsulotlar va top sotilgan mahsulotlar bir qarashda.
- **Sotuv (kassa)** — mahsulotlarni savatga qo'shib naqd yoki nasiyaga sotish;
  sotilgan miqdor omordan avtomatik ayiriladi.
- **Mahsulotlar (ombor)** — mahsulot qo'shish/tahrirlash/o'chirish, tan narx,
  sotish narxi, miqdor, o'lchov birligi va kam qolish chegarasi.
- **Xarajatlar** — ijaraga, kommunal, transport va boshqa xarajatlarni
  kategoriya bo'yicha qayd etish.
- **Qarz daftar** — mijozlarga nasiyaga berilgan mahsulotlar, qarzdorlar
  ro'yxati va to'lovlarni kuzatish.
- **Hisobotlar** — 7/30/90 kunlik savdo, foyda, xarajat statistikasi va
  eng ko'p sotilgan mahsulotlar.
- **AI Yordamchi (OpenAI)** — do'kon egasi tabiiy tilda savol beradi
  ("bugun qancha foyda qildim?", "eng ko'p sotilgan mahsulot qaysi?").
  Ilova do'konning haqiqiy savdo/xarajat/qarz ma'lumotlarini kontekst
  sifatida OpenAI Chat Completions API'ga yuboradi va o'zbek tilida javob
  qaytaradi.
- **Sozlamalar** — OpenAI API key (qurilmada shifrlangan holda saqlanadi,
  `EncryptedSharedPreferences` orqali), do'kon nomi, valyuta va OpenAI modeli.

## Texnik stack

- Kotlin, Jetpack Compose (Material 3), Navigation-Compose
- Room (mahalliy SQLite ma'lumotlar bazasi)
- Retrofit + Moshi + OkHttp (OpenAI API bilan aloqa)
- AndroidX Security Crypto (API key'ni shifrlab saqlash uchun)
- MVVM arxitekturasi, Kotlin Coroutines/Flow

## Loyihani ochish

1. Android Studio (Koala yoki undan yangi versiya) orqali ushbu papkani oching.
2. Gradle sinxronlanishini kuting (internet kerak, chunki kutubxonalar
   Google/Maven Central'dan yuklanadi).
3. `Run` tugmasi orqali emulyator yoki qurilmada ishga tushiring.

Agar `./gradlew` ishlamasa (`Permission denied`), quyidagini bajaring:

```bash
chmod +x gradlew
./gradlew assembleDebug
```

## OpenAI API key qanday olinadi

1. https://platform.openai.com saytiga kiring va hisobingizda "API keys"
   bo'limidan yangi key yarating.
2. Ilovada **Sozlamalar** bo'limiga o'ting va API key'ni kiriting, so'ng
   "Saqlash" tugmasini bosing.
3. Endi **AI Yordamchi** bo'limidan do'koningiz haqida savol berishingiz
   mumkin. API key faqat qurilmangizda shifrlangan holda saqlanadi va
   hech qayerga (serverimizga) yuborilmaydi — faqat to'g'ridan-to'g'ri
   OpenAI serveriga.

> Diqqat: OpenAI API'dan foydalanish pullik bo'lishi mumkin. Narxlar uchun
> platform.openai.com/pricing sahifasiga qarang.

## Loyiha tuzilishi

```
app/src/main/java/com/dokonhisob/app/
├── data/
│   ├── local/          # Room entity, DAO, AppDatabase
│   ├── remote/          # OpenAI Retrofit servisi va modellari
│   ├── repository/      # ShopRepository, AiAssistantRepository
│   └── preferences/      # SettingsManager (shifrlangan sozlamalar)
├── ui/
│   ├── dashboard/        # Boshqaruv paneli
│   ├── sales/            # Kassa/POS ekrani
│   ├── products/         # Ombor
│   ├── expenses/         # Xarajatlar
│   ├── debts/             # Qarz daftar
│   ├── reports/           # Hisobotlar
│   ├── assistant/         # AI Yordamchi chat ekrani
│   ├── settings/          # Sozlamalar
│   └── navigation/        # Navigatsiya grafigi va bottom bar
├── DokonApplication.kt    # Oddiy service-locator (DB, repo'larni yaratadi)
└── MainActivity.kt
```

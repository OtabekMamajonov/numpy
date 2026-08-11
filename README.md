# UzCaptions

O'zbek tilidagi videolarga avtomatik subtitr (AI captions) yozib beruvchi Android ilova. Kotlin + Jetpack Compose + Room + Media3 (ExoPlayer) ustida qurilgan.

## Funksiyalar

- **Video import** — telefondan istalgan videoni yuklash
- **Pleyer + jonli subtitr ko'rinishi** — ExoPlayer orqali video ko'rish, tanlangan dizayn bilan subtitr ustiga chiziladi
- **Qo'lda subtitr yaratish/tahrirlash** — matn va vaqtni (boshlanish/tugash) qo'lda kiritish, o'chirish, qo'shish
- **Avtomatik subtitr yaratish (Muxlisa AI)** — video audiosi ~8 soniyalik bo'laklarga bo'linib, har biri `service.muxlisa.uz/api/v2/stt` orqali matnga aylantiriladi
- **6 ta tayyor dizayn uslubi** — Klassik, Qalin sariq, Karaoke (so'zma-so'z ajratish), Minimal, Neon, Yumshoq pushti
- **SRT eksport** — subtitrlarni `.srt` fayl sifatida saqlash/ulashish
- **Sozlamalar** — Muxlisa AI API key'ni qurilmada shifrlangan holda saqlash

## Muxlisa AI STT integratsiyasi

`data/remote/SttRepository.kt` — Muxlisa AI (`https://service.muxlisa.uz/api/v2/stt`) bilan ishlaydi:

- **So'rov:** `POST`, `x-api-key` header, `multipart/form-data` bilan `audio` maydonida fayl
- **Chegara:** har bir so'rov ≤ 5 MB va ≤ 60 soniya — shu sababli `data/remote/AudioChunkExtractor.kt` video audiosini ~8 soniyalik `.m4a` bo'laklarga (qayta kodlashsiz, `MediaExtractor`/`MediaMuxer` orqali) bo'lib, ketma-ket yuboradi
- **Javob:** `{"text": "..."}` (200 da), xato holatlarda (`400`/`402`/`429`/`5xx`) `{"detail": "..."}` — har biri foydalanuvchiga tushunarli xabar bilan ko'rsatiladi

Bu kod hali **haqiqiy qurilmada sinovdan o'tkazilmagan** (Android SDK'siz sandbox muhitida yozilgan) — birinchi ishga tushirishda `MediaMuxer`ning ba'zi video formatlar bilan chetga chiqishi ehtimoli bor. Agar "Avtomatik subtitr yaratish" xato bersa, xabarni menga yuboring — tezda tuzataman.

## Texnik stack

- Kotlin, Jetpack Compose (Material 3), Navigation-Compose
- Media3 (ExoPlayer) — video pleyer
- Room — mahalliy ma'lumotlar bazasi (loyihalar, subtitr segmentlari)
- Retrofit + Moshi + OkHttp — Muxlisa AI STT API bilan ishlash uchun
- `MediaExtractor`/`MediaMuxer` — video audiosini qayta kodlashsiz kichik bo'laklarga bo'lish
- AndroidX Security Crypto — API key'ni shifrlab saqlash
- MVVM arxitekturasi, Kotlin Coroutines/Flow

## Loyihani ochish

1. Android Studio orqali ushbu papkani oching, Gradle sinxronlanishini kuting.
2. `Run` tugmasi orqali ishga tushiring, yoki GitHub Actions orqali tayyor APK yasang (`.github/workflows/build-apk.yml` — har push'da avtomatik ishga tushadi, natija "Actions" bo'limida artifact sifatida chiqadi).

## Loyiha tuzilishi

```
app/src/main/java/com/uzcaptions/app/
├── data/
│   ├── local/          # Room entity (SubtitleProject, SubtitleSegment), DAO, AppDatabase
│   ├── remote/          # Muxlisa AI STT repository, audio bo'laklash
│   ├── repository/      # SubtitleRepository
│   └── preferences/      # SettingsManager (shifrlangan API key)
├── ui/
│   ├── projects/         # Bosh sahifa — loyihalar ro'yxati, video import
│   ├── editor/            # Pleyer + subtitr overlay + vaqt jadvali (tahrirlash)
│   ├── style/             # Dizayn uslublarini tanlash
│   ├── settings/          # Sozlamalar (API key)
│   ├── common/            # Vaqt formatlash, SRT eksport, video metadata
│   └── navigation/        # Navigatsiya grafigi
├── UzCaptionsApplication.kt
└── MainActivity.kt
```

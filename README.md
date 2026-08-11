# UzCaptions

O'zbek tilidagi videolarga avtomatik subtitr (AI captions) yozib beruvchi Android ilova. Kotlin + Jetpack Compose + Room + Media3 (ExoPlayer) ustida qurilgan.

## Funksiyalar

- **Video import** — telefondan istalgan videoni yuklash
- **Pleyer + jonli subtitr ko'rinishi** — ExoPlayer orqali video ko'rish, tanlangan dizayn bilan subtitr ustiga chiziladi
- **Qo'lda subtitr yaratish/tahrirlash** — matn va vaqtni (boshlanish/tugash) qo'lda kiritish, o'chirish, qo'shish
- **Avtomatik subtitr yaratish (Muxlisa/uzbekvoice AI)** — *hozircha qoralama holatda* (quyidagi "STT integratsiyasi holati" bo'limiga qarang)
- **6 ta tayyor dizayn uslubi** — Klassik, Qalin sariq, Karaoke (so'zma-so'z ajratish), Minimal, Neon, Yumshoq pushti
- **SRT eksport** — subtitrlarni `.srt` fayl sifatida saqlash/ulashish
- **Sozlamalar** — STT API key'ni qurilmada shifrlangan holda saqlash

## STT integratsiyasi holati (MUHIM)

`data/remote/SttRepository.kt` fayli hali **Muxlisa AI / uzbekvoice.ai API bilan to'liq ulanmagan**. Bu loyihani tayyorlash paytida ularning rasmiy hujjat sahifalariga (`muxlisa.uz`, `uzbekvoice.ai`, `mohir.uzbekvoice.ai`) tarmoq cheklovi tufayli kira olmadim, shuning uchun aniq so'rov/javob formatini tasdiqlamasdan integratsiya yozish xato ilovaga olib kelishi mumkin edi.

**Ulash uchun kerak bo'ladigan ma'lumotlar:**
- STT endpoint manzili (URL)
- Autentifikatsiya usuli (masalan `Authorization: Bearer <key>` yoki boshqa header)
- So'rov formati (multipart audio fayl yuklashmi, qaysi audio format — wav/mp3/m4a, til kodi qanday uzatiladi)
- Javob JSON strukturasi (matn, so'z darajasidagi vaqt belgilari bormi yo'qmi)

Bu ma'lumotlar aniqlangach, `SttRepository.transcribe()` funksiyasini haqiqiy Retrofit chaqiruviga almashtirish kifoya — qolgan qism (ma'lumotlar bazasiga yozish, UI'da ko'rsatish) tayyor.

Hozircha ilovada subtitrlarni **qo'lda** yaratish va barcha dizayn/eksport funksiyalari to'liq ishlaydi.

## Texnik stack

- Kotlin, Jetpack Compose (Material 3), Navigation-Compose
- Media3 (ExoPlayer) — video pleyer
- Room — mahalliy ma'lumotlar bazasi (loyihalar, subtitr segmentlari)
- Retrofit + Moshi + OkHttp — STT API uchun tayyorlangan (hali ulanmagan)
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
│   ├── remote/          # STT (Muxlisa/uzbekvoice) repository — qoralama
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

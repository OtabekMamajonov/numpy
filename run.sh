#!/usr/bin/env bash
# Loyihani lokal ishga tushiradi: bog'liqliklarni o'rnatadi, Mini App'ni
# build qiladi, bazani birinchi marta to'ldiradi va bot + API'ni yoqadi.
#
# Ishlatish:
#   ./run.sh
set -euo pipefail

cd "$(dirname "$0")"

if [ ! -f .env ]; then
  echo "XATO: .env fayli yo'q."
  echo "Namuna: cp .env.example .env  — keyin BOT_TOKEN, ADMIN_IDS va WEBAPP_URL ni to'ldiring."
  exit 1
fi

if ! grep -q '^WEBAPP_URL=https://' .env; then
  echo "OGOHLANTIRISH: .env dagi WEBAPP_URL bo'sh yoki HTTPS emas."
  echo "Telegram Mini App faqat HTTPS manzilni ochadi (masalan ngrok bergan manzil)."
  echo
fi

if [ ! -d .venv ]; then
  echo "==> Python muhiti yaratilmoqda"
  python3 -m venv .venv
fi

echo "==> Python bog'liqliklari"
.venv/bin/pip install -q -r requirements.txt

echo "==> Mini App build qilinmoqda"
npm --prefix webapp install --silent
npm --prefix webapp run build

DB_FILE="${DATA_DIR:-.}/bot.db"
if [ ! -f "$DB_FILE" ]; then
  echo "==> Demo katalog yuklanmoqda"
  .venv/bin/python -m app.db.seed
fi

echo "==> Ishga tushdi. To'xtatish uchun Ctrl+C"
exec .venv/bin/python -m app.main

# Mini App'ni Node bilan build qilamiz, so'ng Python muhitiga ko'chiramiz.
FROM node:20-slim AS webapp
WORKDIR /build
COPY webapp/package.json webapp/package-lock.json webapp/
RUN npm --prefix webapp ci
COPY webapp/ webapp/
RUN npm --prefix webapp run build

FROM python:3.11-slim
WORKDIR /app

COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

COPY app/ app/
COPY --from=webapp /build/webapp/dist webapp/dist

ENV PYTHONUNBUFFERED=1
EXPOSE 8000
CMD ["python", "-m", "app.main"]

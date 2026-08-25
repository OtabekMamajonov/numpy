from contextlib import asynccontextmanager

from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from fastapi.staticfiles import StaticFiles

from app.api.routes import router
from app.config import IMAGES_DIR, PROJECT_ROOT, STATIC_DIR
from app.db.database import init_db

WEBAPP_DIST = PROJECT_ROOT / "webapp" / "dist"


@asynccontextmanager
async def lifespan(app: FastAPI):
    await init_db()
    yield


app = FastAPI(title="Qurilish xizmatlari API", lifespan=lifespan)

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_methods=["GET", "POST"],
    allow_headers=["*"],
)

app.include_router(router)

IMAGES_DIR.mkdir(parents=True, exist_ok=True)
app.mount("/static", StaticFiles(directory=STATIC_DIR), name="static")

if WEBAPP_DIST.exists():
    app.mount("/", StaticFiles(directory=WEBAPP_DIST, html=True), name="webapp")

from fastapi import APIRouter, HTTPException, File, UploadFile
from fastapi.responses import FileResponse
from database import get_db_connection
import uuid
import os

router = APIRouter(tags=["Create Plant"])

@router.post("/create_plant")
def create_plant(user_id: int, image: UploadFile, plant_kind: str, plant_location: str, pot_size: str, water_cycle: str):
    UPLOAD_DIR = "Backend/static"
    
    content = image.file.read()
    filename = f"{str(uuid.uuid4())}.jpg"  # uuid로 유니크한 파일명으로 변경
    with open(os.path.join(UPLOAD_DIR, filename), "wb") as fp:
        fp.write(content)  # 서버 로컬 스토리지에 이미지 저장 (쓰기)
    
    conn = get_db_connection()
    cursor = conn.cursor()
    
    try:
        sql_insert = "INSERT INTO create_plants (user_id, image_url, plant_kind, plant_location, pot_size, water_cycle) VALUES (%d, %s, %s, %s, %s, %s)"
        cursor.execute(sql_insert, (user_id, filename, plant_kind, plant_location, pot_size, water_cycle))
        conn.commit()
    finally:
        conn.close()

    return {"filename": filename}

@router.get("/get_plant_image")
def get_image(plant_id: int):
    
    return FileResponse()
# id, user_id, image_url, plant_kind, plant_location, pot_size, water_cycle, created_at
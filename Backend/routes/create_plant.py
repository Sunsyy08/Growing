from fastapi import APIRouter, HTTPException, File, UploadFile
from fastapi.responses import FileResponse
from database import get_db_connection
from ultralytics import YOLO
import uuid
import os

router = APIRouter(tags=["Create Plant"])

def predict_model(image: str, model: str):
    if(model == 'casava'):
        model = YOLO("runs/detect/train2/weights/casava_model.pt")
    if(model == 'rubber'):
        model = YOLO("runs/detect/train2/weights/rubber_model.pt")

    results = model.predict(
        source=f"/Users/honggunwoo/Desktop/Growing/static/{image}",
        imgsz=640,
        conf=0.05,   
        iou=0.5,
        device=0
    )

    names = model.names

    for r in results:
        for box in r.boxes:
            cls = int(box.cls[0])
            conf = float(box.conf[0])
            name = names[cls]
            conf2 = round(conf, 2)
    
    base_score = {
        "Healthy": 100,
        "Mosaic": 65,
        "blight": 20
    }

    score = base_score[name] * conf2 + 100 * (1 - conf2)
    return score

@router.post("/create_plant")
def create_plant(user_id: int, image: UploadFile, plant_kind: str, plant_location: str, pot_size: str, water_cycle: str):
    UPLOAD_DIR = "/Users/honggunwoo/Desktop/Growing/static"
    
    content = image.file.read()
    filename = f"{str(uuid.uuid4())}.jpg"  # uuid로 유니크한 파일명으로 변경
    with open(os.path.join(UPLOAD_DIR, filename), "wb") as fp:
        fp.write(content)  # 서버 로컬 스토리지에 이미지 저장 (쓰기)
    
    conn = get_db_connection()
    cursor = conn.cursor()
    
    try:
        sql_insert = "INSERT INTO create_plants (user_id, image_url, plant_kind, plant_location, pot_size, water_cycle) VALUES (%s, %s, %s, %s, %s, %s)"
        cursor.execute(sql_insert, (user_id, filename, plant_kind, plant_location, pot_size, water_cycle))
        conn.commit()
    finally:
        conn.close()

    return {"filename": filename}

@router.post("/update_plant")
def update_plant(plant_id: int, image: UploadFile, select_model: str):
    UPLOAD_DIR = "/Users/honggunwoo/Desktop/Growing/static"
    
    content = image.file.read()
    filename = f"{str(uuid.uuid4())}.jpg"  # uuid로 유니크한 파일명으로 변경
    with open(os.path.join(UPLOAD_DIR, filename), "wb") as fp:
        fp.write(content)  # 서버 로컬 스토리지에 이미지 저장 (쓰기)
    
    conn = get_db_connection()
    cursor = conn.cursor()
    
    score = predict_model(filename, select_model)
    
    try:
        sql_insert = "INSERT INTO  (plant_id, score) VALUES (%s, %s)"
        cursor.execute(sql_insert, (plant_id, score))
        conn.commit()
    finally:
        conn.close()

    return {"filename": filename}

@router.get("/get_plant_image")
def get_image(plant_id: int):
    conn = get_db_connection()
    cursor = conn.cursor()
    
    sql = """
            select image_url from create_plants where id =%s
        """
    cursor.execute(sql, (plant_id,))
    plant = cursor.fetchone()
    image = f"/Users/honggunwoo/Desktop/Growing/static/{plant['image_url']}"
    print(plant)
    return FileResponse(image)
# id, user_id, image_url, plant_kind, plant_location, pot_size, water_cycle, created_at
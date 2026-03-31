from fastapi import APIRouter, HTTPException, File, UploadFile
from fastapi.responses import FileResponse
from database import get_db_connection
from ultralytics import YOLO
import uuid
import os

router = APIRouter(tags=["Create Plant"])

def predict_model(image: str, model: str):
    if(model == 'casava'):
        model = YOLO("/Users/honggunwoo/Desktop/Growing/Backend/models/casava_model.pt")
    if(model == 'rubber'):
        model = YOLO("/Users/honggunwoo/Desktop/Growing/Backend/models/rubber_model.pt")

    results = model.predict(
        source=f"{image}",
        imgsz=640,
        conf=0.05,   
        iou=0.5
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
def create_plant(user_id: int, plant_name: str, image: UploadFile, plant_kind: str, plant_location: str, pot_size: str, water_cycle: str):
    UPLOAD_DIR = "/Users/honggunwoo/Desktop/Growing/static"
    
    content = image.file.read()
    filename = f"{str(uuid.uuid4())}.jpg"  # uuid로 유니크한 파일명으로 변경
    with open(os.path.join(UPLOAD_DIR, filename), "wb") as fp:
        fp.write(content)  # 서버 로컬 스토리지에 이미지 저장 (쓰기)
    
    conn = get_db_connection()
    cursor = conn.cursor()
    
    try:
        sql_insert = "INSERT INTO create_plants (user_id, plant_name, image_url, plant_kind, plant_location, pot_size, water_cycle) VALUES (%s, %s, %s, %s, %s, %s, %s)"
        cursor.execute(sql_insert, (user_id, plant_name, filename, plant_kind, plant_location, pot_size, water_cycle))
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

    return {"filename": filename, "score":score}

@router.get("/get_plant_image")
def get_image(user_id: int):
    conn = get_db_connection()
    cursor = conn.cursor()
    
    sql = """
            SELECT id, image_url
            FROM your_table
            WHERE user_id = %s
            ORDER BY created_at DESC;
        """
    cursor.execute(sql, (user_id))
    plants = cursor.fetchone()
    result = []
    images = [
        {
            "plant_id": row[0],
            "image_url": f"/Users/honggunwoo/Desktop/Growing/static/{row[1]}"
        }
        for row in result
    ]
    print(images["image_url"])
    return FileResponse(images["image_url"])

@router.get("/get_score")
def get_score(user_id: int, plant_kind: str):
    conn = get_db_connection()
    cursor = conn.cursor()
    
    sql = """
            select image_url from create_plants where user_id =%s
        """
    cursor.execute(sql, (user_id))
    plants = cursor.fetchone()
    result = []
    for plant in plants:
        result.append({
            "id": plant["id"],
            "image": plant["image_url"]
        })
        
    scorestatus = []
    
    for image in result:
        image = f"/Users/honggunwoo/Desktop/Growing/static/{image['image']}"
        score = predict_model(image, plant_kind)
        if score >= 70:
            status = "좋음"
        elif score >= 40:
            status = "보통"
        else:
            status = "나쁨"
        scorestatus.append({"id": image["id"], "score": score, "status":status})
    
    
    return scorestatus
# id, user_id, image_url, plant_kind, plant_location, pot_size, water_cycle, created_at
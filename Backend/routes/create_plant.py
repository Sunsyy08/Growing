from fastapi import APIRouter, HTTPException, File, UploadFile
from fastapi.responses import FileResponse
from database import get_db_connection
from ultralytics import YOLO
import uuid
import os
from fastapi.responses import StreamingResponse
import io
import zipfile

router = APIRouter(tags=["Create Plant"])

def predict_model(image: str, model: str):
    if(model == 'casava'):
        model = YOLO("/Users/honggunwoo/Desktop/Growing/Backend/models/casava_model.pt")
    if(model == 'rubber'):
        model = YOLO("/Users/honggunwoo/Desktop/Growing/Backend/models/rubber_model.pt")

    results = model.predict(
        source=f"/Users/honggunwoo/Desktop/Growing/static/{image}",
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

def insert_plant(plant_id: int, image_name: str, select_model: str):
    conn = get_db_connection()
    cursor = conn.cursor()

    print(image_name)
    
    score = predict_model(image_name, select_model)
    
    try:
        sql_insert = "INSERT INTO plant_state (create_plant_id, score) VALUES (%s, %s)"
        cursor.execute(sql_insert, (plant_id, score))
        conn.commit()
    finally:
        conn.close()

    return {"filename": image_name, "score":score}

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
        sql_select = "select id from create_plants where plant_name = %s"
        cursor.execute(sql_select, (plant_name,))
        plant_id = cursor.fetchone()
        if plant_kind == "카사바":
            kind = "casava" 
        elif plant_kind == "고무나무":
            kind = "rubber"
        insert_plant(plant_id['id'], filename, kind)
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
        sql_insert = "INSERT INTO plant_state (create_plant_id, score) VALUES (%s, %s)"
        cursor.execute(sql_insert, (plant_id, score))
        conn.commit()
        sql_update = "UPDATE create_plants SET image_url = %s WHERE id = %s"
        cursor.execute(sql_update, (filename, plant_id))
        conn.commit()
    finally:
        conn.close()

    return {"filename": filename, "score":score}

@router.get("/get_plantid")
def get_id(user_id: int):
    conn = get_db_connection()
    cursor = conn.cursor()
    
    sql = """
        SELECT id
        FROM create_plants
        WHERE user_id = %s
        ORDER BY created_at DESC;
    """

    cursor.execute(sql, (user_id,))
    plants = cursor.fetchall()

    result = []

    for row in plants:
        result.append({
            "plant_id": row["id"]
        })

    return result

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

@router.get("/get_score")
def get_score(plant_id: int):
    conn = get_db_connection()
    cursor = conn.cursor()
    
    sql = """
            select plant_kind, plant_name from create_plants where id =%s
        """
    cursor.execute(sql, (plant_id,))
    plant = cursor.fetchone()
    select_sql = "SELECT score FROM plant_state WHERE create_plant_id = %s ORDER BY created_at DESC LIMIT 1"
    cursor.execute(select_sql, (plant_id,))
    score = cursor.fetchone()
    if score['score'] >= 70:
        status = "좋음"
    elif score['score'] >= 40:
        status = "보통"
    else:
        status = "나쁨"
    print(score['score'])
    return {"점수":score['score'], "상태":status, "종류":plant['plant_kind'], "이름":plant["plant_name"]}

@router.get("/draw_graph")
def draw_graph(plant_id: int):
    conn = get_db_connection()
    cursor = conn.cursor()
    
    sql = """
            SELECT score, created_at FROM plant_state WHERE create_plant_id = %s ORDER BY created_at ASC;
        """
    cursor.execute(sql, (plant_id,))
    plants = cursor.fetchall()

    result = []

    for row in plants:
        result.append({
            "점수": row["score"],
            "날짜": row["created_at"]
        })

    return result

@router.get("/get_all_image_url")
def get_image_url(user_id: int):
    conn = get_db_connection()
    cursor = conn.cursor()
    
    sql = """
            select image_url from create_plants where user_id =%s ORDER BY created_at DESC LIMIT 5
        """
    cursor.execute(sql, (user_id,))
    plant = cursor.fetchall()
    result = []

    for row in plant:
        result.append({
            "image_url": row['image_url']
        })

    return result

@router.get("/get_all_image")
def get_all_image(image_url:str):
    return FileResponse(f"/Users/honggunwoo/Desktop/Growing/static/{image_url}")

@router.get("/get_all_score")
def get_score(user_id: int):
    conn = get_db_connection()
    cursor = conn.cursor()
    
    sql = """
            select plant_kind, plant_name, id from create_plants where user_id =%s ORDER BY created_at DESC LIMIT 5
        """
    cursor.execute(sql, (user_id,))
    plant = cursor.fetchone()
    select_sql = "SELECT score FROM plant_state WHERE create_plant_id = %s ORDER BY created_at DESC LIMIT 1"
    cursor.execute(select_sql, (plant["id"],))
    score = cursor.fetchone()
    if score['score'] >= 70:
        status = "좋음"
    elif score['score'] >= 40:
        status = "보통"
    else:
        status = "나쁨"
    print(score['score'])
    return {"점수":score['score'], "상태":status, "종류":plant['plant_kind'], "이름":plant["plant_name"]}
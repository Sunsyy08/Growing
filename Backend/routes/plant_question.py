from fastapi import APIRouter, HTTPException
from database import get_db_connection
from pydantic import BaseModel

router = APIRouter(tags=["Plant Question"])

class QuestionData(BaseModel):
    user_id: int
    image: str
    symptom: str
    message: str
    expert: str

# [POST] 전문가 상담 질문 등록
@router.post("/plant_question")
def create_plant_question(data: QuestionData):
    conn = get_db_connection()
    cursor = conn.cursor()

    try:
        # 유저 존재 확인
        cursor.execute("SELECT id FROM users WHERE id = %s", (data.user_id,))
        if not cursor.fetchone():
            raise HTTPException(status_code=404, detail="존재하지 않는 사용자입니다.")

        sql = """
            INSERT INTO questions (user_id, image_url, symptom, message, expert_name)
            VALUES (%s, %s, %s, %s, %s)
        """
        cursor.execute(sql, (data.user_id, data.image, data.symptom, data.message, data.expert))
        conn.commit()
        question_id = cursor.lastrowid
    finally:
        conn.close()

    return {
        "message": "상담 질문이 등록되었습니다.",
        "question_id": question_id
    }

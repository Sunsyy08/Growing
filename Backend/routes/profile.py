from fastapi import APIRouter, HTTPException
from database import get_db_connection

router = APIRouter(tags=["Profile"])

# [GET] 내 프로필 조회
@router.get("/profile")
def get_profile(user_id: int):
    conn = get_db_connection()
    cursor = conn.cursor()

    try:
        # 유저 정보 조회
        cursor.execute("SELECT id, name, email FROM users WHERE id = %s", (user_id,))
        user = cursor.fetchone()
        if not user:
            raise HTTPException(status_code=404, detail="존재하지 않는 사용자입니다.")

        # 식물 개수 + 가장 최근 등록 시각
        cursor.execute(
            "SELECT COUNT(*) as count, MAX(created_at) as last_created FROM create_plants WHERE user_id = %s",
            (user_id,)
        )
        plant_info = cursor.fetchone()

        # 상담 개수 + 가장 최근 상담 시각
        cursor.execute(
            "SELECT COUNT(*) as count, MAX(created_at) as last_created FROM questions WHERE user_id = %s",
            (user_id,)
        )
        question_info = cursor.fetchone()

    finally:
        conn.close()

    return {
        "user_id": user["id"],
        "name": user["name"],
        "email": user["email"],
        "plant_count": plant_info["count"],
        "plant_last_created_at": plant_info["last_created"],
        "question_count": question_info["count"],
        "question_last_created_at": question_info["last_created"],
    }

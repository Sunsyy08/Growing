from fastapi import APIRouter, HTTPException
from database import get_db_connection

router = APIRouter(tags=["Plant Updates"])

# [GET] 식물 점수 업데이트 기록 조회
@router.get("/plant_updates")
def get_plant_updates(plant_id: int):
    conn = get_db_connection()
    cursor = conn.cursor()

    try:
        # 식물 존재 확인
        cursor.execute("SELECT id FROM create_plants WHERE id = %s", (plant_id,))
        if not cursor.fetchone():
            raise HTTPException(status_code=404, detail="존재하지 않는 식물입니다.")

        # 점수 + 촬영 시각 목록 조회 (최신순)
        sql = """
            SELECT score, created_at
            FROM plant_analysis
            WHERE plant_id = %s
            ORDER BY created_at DESC
        """
        cursor.execute(sql, (plant_id,))
        records = cursor.fetchall()
    finally:
        conn.close()

    return {
        "plant_id": plant_id,
        "updates": [
            {
                "score": record["score"],
                "created_at": record["created_at"]
            }
            for record in records
        ]
    }

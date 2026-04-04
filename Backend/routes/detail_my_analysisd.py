from fastapi import APIRouter, HTTPException
from database import get_db_connection

router = APIRouter(tags=["Detail My Analysis"])

# [GET] 상세화면 AI 분석
@router.get("/detail_my_analysisd")
def get_detail_my_analysisd(plant_id: int):
    conn = get_db_connection()
    cursor = conn.cursor()

    try:
        cursor.execute("""
            SELECT state_description, sunlight_status
            FROM plant_analysis
            WHERE plant_id = %s
            ORDER BY created_at DESC
            LIMIT 1
        """, (plant_id,))
        result = cursor.fetchone()
    finally:
        conn.close()

    if not result:
        raise HTTPException(status_code=404, detail="분석 결과를 찾을 수 없습니다.")

    return {
        "status_analysis": result["state_description"],
        "sunlight_status": result["sunlight_status"],
    }

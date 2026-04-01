from fastapi import APIRouter, HTTPException
from database import get_db_connection

router = APIRouter(tags=["AI Analysis"])

# [GET] AI 분석 결과 조회
@router.get("/ai_analysis")
def get_ai_analysis(plant_id: int):
    conn = get_db_connection()
    cursor = conn.cursor()

    try:
        sql = """
            SELECT
                pa.state_description,
                cp.water_cycle,
                pa.sunlight_status AS sunlight_location_change,
                pa.air_circulation,
                pa.health_prediction,
                cp.image_url
            FROM plant_analysis pa
            JOIN create_plants cp ON pa.plant_id = cp.id
            WHERE pa.plant_id = %s
            ORDER BY pa.created_at DESC
            LIMIT 1
        """
        cursor.execute(sql, (plant_id,))
        result = cursor.fetchone()
    finally:
        conn.close()

    if not result:
        raise HTTPException(status_code=404, detail="분석 결과를 찾을 수 없습니다.")

    return {
        "state_description": result["state_description"],
        "water_cycle": result["water_cycle"],
        "sunlight_location_change": result["sunlight_location_change"],
        "air_circulation": result["air_circulation"],
        "health_prediction": result["health_prediction"],
        "image": result["image_url"],
    }

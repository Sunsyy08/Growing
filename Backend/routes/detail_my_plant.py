from fastapi import APIRouter, HTTPException
from database import get_db_connection

router = APIRouter(tags=["Detail My Plant"])

# [GET] 내 식물 상세 조회
@router.get("/detail_my_plant")
def get_detail_my_plant(plant_id: int):
    conn = get_db_connection()
    cursor = conn.cursor()

    try:
        sql = """
            SELECT
                cp.image_url,
                cp.plant_kind,
                cp.plant_location,
                cp.pot_size,
                cp.water_cycle,
                pa.sunlight_status,
                pa.score,
                pa.state_description
            FROM create_plants cp
            LEFT JOIN (
                SELECT plant_id, sunlight_status, score, state_description
                FROM plant_analysis
                WHERE (plant_id, created_at) IN (
                    SELECT plant_id, MAX(created_at)
                    FROM plant_analysis
                    GROUP BY plant_id
                )
            ) pa ON cp.id = pa.plant_id
            WHERE cp.id = %s
        """
        cursor.execute(sql, (plant_id,))
        plant = cursor.fetchone()
    finally:
        conn.close()

    if not plant:
        raise HTTPException(status_code=404, detail="식물을 찾을 수 없습니다.")

    return {
        "image": plant["image_url"],
        "plant_kind": plant["plant_kind"],
        "plant_location": plant["plant_location"],
        "pot_size": plant["pot_size"],
        "water_cycle": plant["water_cycle"],
        "sunlight": plant["sunlight_status"],
        "score": plant["score"] if plant["score"] is not None else 0,
        "analysis_ai": plant["state_description"],
    }

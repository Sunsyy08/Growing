from fastapi import APIRouter, HTTPException
from database import get_db_connection

router = APIRouter(tags=["My Plant"])

def get_plant_state(score: int) -> tuple[str, str]:
    if score >= 70:
        return "좋음", "식물이 건강하게 잘 자라고 있어요!"
    elif score >= 40:
        return "보통", "식물 상태가 보통입니다. 꾸준한 관리가 필요해요."
    else:
        return "나쁨", "식물 상태가 좋지 않아요. 즉각적인 관리가 필요합니다."

# [GET] 내 식물 목록 조회
@router.get("/my_plant")
def get_my_plant(user_id: int):
    conn = get_db_connection()
    cursor = conn.cursor()

    try:
        # 유저 존재 확인
        cursor.execute("SELECT id FROM users WHERE id = %s", (user_id,))
        if not cursor.fetchone():
            raise HTTPException(status_code=404, detail="존재하지 않는 사용자입니다.")

        # 내 식물 목록 + 최신 분석 점수 조회
        sql = """
            SELECT
                cp.id,
                cp.plant_kind,
                cp.water_cycle,
                cp.image_url,
                pa.score
            FROM create_plants cp
            LEFT JOIN (
                SELECT plant_id, score
                FROM plant_analysis
                WHERE (plant_id, created_at) IN (
                    SELECT plant_id, MAX(created_at)
                    FROM plant_analysis
                    GROUP BY plant_id
                )
            ) pa ON cp.id = pa.plant_id
            WHERE cp.user_id = %s
            ORDER BY cp.created_at DESC
        """
        cursor.execute(sql, (user_id,))
        plants = cursor.fetchall()
    finally:
        conn.close()

    result = []
    for plant in plants:
        score = plant["score"] if plant["score"] is not None else 0
        my_plant_state, state_content = get_plant_state(score)

        result.append({
            "id": plant["id"],
            "plant_kind": plant["plant_kind"],
            "water_cycle": plant["water_cycle"],
            "image": plant["image_url"],
            "score": score,
            "my_plant_state": my_plant_state,
            "state_content": state_content,
        })

    return {"plants": result}

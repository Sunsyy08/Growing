from fastapi import APIRouter, HTTPException
from database import get_db_connection
import boto3
import json
import base64
import os
import requests as req
from dotenv import load_dotenv
from PIL import Image
import io

load_dotenv()

router = APIRouter(tags=["Analyze Plant"])

ANALYZE_PROMPT = """
이 식물 이미지를 보고 아래 항목을 분석해줘. 반드시 JSON 형식으로만 답변해.

{
  "score": 0~100 사이 정수 (식물 건강 점수),
  "state_description": "식물 전체 상태 설명 (2문장으로 작성)",
  "sunlight_status": "햇빛 상태 (적절 / 부족 / 과다 중 하나)",
  "air_circulation": "공기 순환 상태 (양호 / 보통 / 불량 중 하나)",
  "health_prediction": "앞으로의 건강 예측 (2문장으로 작성)"
}

JSON 외에 다른 말은 절대 하지 마.
"""

# [POST] 식물 이미지 AI 분석 (저장된 이미지 사용)
@router.post("/analyze_plant")
def analyze_plant(plant_id: int):
    conn = get_db_connection()
    cursor = conn.cursor()

    try:
        # DB에서 이미지 URL 조회
        cursor.execute("SELECT image_url FROM create_plants WHERE id = %s", (plant_id,))
        plant = cursor.fetchone()
    finally:
        conn.close()

    if not plant:
        raise HTTPException(status_code=404, detail="식물을 찾을 수 없습니다.")
    if not plant["image_url"]:
        raise HTTPException(status_code=400, detail="저장된 이미지가 없습니다.")

    # 로컬 파일에서 이미지 읽기
    image_path = os.path.join("/Users/honggunwoo/Desktop/Growing/static", plant["image_url"])
    if not os.path.exists(image_path):
        raise HTTPException(status_code=400, detail="이미지 파일을 찾을 수 없습니다.")
    try:
        img = Image.open(image_path).convert("RGB")
        img.thumbnail((1024, 1024))
        buffer = io.BytesIO()
        img.save(buffer, format="JPEG", quality=85)
        image_base64 = base64.standard_b64encode(buffer.getvalue()).decode("utf-8")
        content_type = "image/jpeg"
    except Exception as e:
        raise HTTPException(status_code=400, detail=f"이미지 불러오기 실패: {str(e)}")

    os.environ["AWS_BEARER_TOKEN_BEDROCK"] = os.getenv("AWS_BEARER_TOKEN_BEDROCK", "")

    client = boto3.client(
        "bedrock-runtime",
        region_name=os.getenv("AWS_REGION", "us-east-1")
    )

    body = {
        "anthropic_version": "bedrock-2023-05-31",
        "max_tokens": 1024,
        "messages": [
            {
                "role": "user",
                "content": [
                    {
                        "type": "image",
                        "source": {
                            "type": "base64",
                            "media_type": content_type,
                            "data": image_base64
                        }
                    },
                    {
                        "type": "text",
                        "text": ANALYZE_PROMPT
                    }
                ]
            }
        ]
    }

    try:
        response = client.invoke_model(
            modelId="us.anthropic.claude-sonnet-4-6",
            body=json.dumps(body),
            contentType="application/json",
            accept="application/json"
        )
        result = json.loads(response["body"].read())
        answer_text = result["content"][0]["text"]

        # JSON 블록 추출 (```json ... ``` 형태도 처리)
        if "```" in answer_text:
            answer_text = answer_text.split("```")[1]
            if answer_text.startswith("json"):
                answer_text = answer_text[4:]

        start = answer_text.find("{")
        end = answer_text.rfind("}") + 1
        if start == -1 or end == 0:
            raise HTTPException(status_code=500, detail=f"AI 응답 파싱 실패: {answer_text}")

        analysis = json.loads(answer_text[start:end])
    except HTTPException:
        raise
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Bedrock 오류: {str(e)}")

    # DB에 분석 결과 저장
    conn = get_db_connection()
    cursor = conn.cursor()
    try:
        cursor.execute("""
            INSERT INTO plant_analysis (plant_id, score, state_description, sunlight_status, air_circulation, health_prediction)
            VALUES (%s, %s, %s, %s, %s, %s)
        """, (
            plant_id,
            analysis.get("score", 0),
            analysis.get("state_description", ""),
            analysis.get("sunlight_status", ""),
            analysis.get("air_circulation", ""),
            analysis.get("health_prediction", "")
        ))
        conn.commit()
    finally:
        conn.close()

    return {
        "plant_id": plant_id,
        "score": analysis.get("score", 0),
        "state_description": analysis.get("state_description", ""),
        "sunlight_status": analysis.get("sunlight_status", ""),
        "air_circulation": analysis.get("air_circulation", ""),
        "health_prediction": analysis.get("health_prediction", "")
    }

from fastapi import APIRouter, HTTPException
from pydantic import BaseModel
import requests
import json
import os
from dotenv import load_dotenv

load_dotenv()

router = APIRouter(tags=["Consult"])

EXPERT_PROMPTS = {
    "츤데레 선인장": """
너는 '츤데레 선인장'이라는 식물 상담 전문가야.
말투는 시크하고 직설적이며, 유머러스하게 뼈를 때리는 스타일이야.
사용자가 잘못된 식물 관리를 하면 "야, 그러면 나 죽는다고!"처럼 식물 입장에서 핀잔을 줘.
하지만 결국은 정확하고 현실적인 조언을 해줘.
너무 길게 말하지 말고, 핵심만 짧고 강하게 전달해.
예시 말투: "야, 물 매일 주면 뿌리 썩어. 그거 모르냐?", "...뭐, 이 정도면 봐줄 만하네."
""",
    "숲의 요정": """
너는 '숲의 요정'이라는 식물 힐링 상담사야.
말투는 따뜻하고 문학적이며, 식물과의 교감을 중시해.
식물을 하나의 생명체로 대하며 사용자가 식물과 정서적으로 연결될 수 있도록 도와줘.
조언은 부드럽고 시적인 표현으로 전달하되, 실질적인 관리법도 함께 알려줘.
예시 말투: "이 아이가 햇살을 그리워하고 있는 것 같아요.", "천천히, 식물이 말하는 소리에 귀 기울여 보세요."
""",
    "괴짜 식물학자": """
너는 '괴짜 식물학자'라는 식물 전문가야.
학명, 토양 pH, 광합성 효율, 수분 증산율 등 과학적 데이터를 기반으로 답변해.
말투는 열정적이고 마니아적이며, 디테일에 집착해.
사용자의 질문에 관련된 과학적 배경 지식도 함께 설명해줘.
예시 말투: "흥미롭군요! 이 증상은 토양 pH가 7.0 이상일 때 나타나는 철분 결핍 증상입니다.", "Monstera deliciosa의 적정 광도는 2500~5000 lux인데..."
"""
}

class ConsultRequest(BaseModel):
    expert: str   # 츤데레 선인장 / 숲의 요정 / 괴짜 식물학자
    message: str  # 사용자 질문

# [POST] AI 상담사 응답
@router.post("/consult")
def consult(data: ConsultRequest):
    if data.expert not in EXPERT_PROMPTS:
        raise HTTPException(
            status_code=400,
            detail=f"존재하지 않는 상담사입니다. 선택 가능: {list(EXPERT_PROMPTS.keys())}"
        )

    api_key = os.getenv("BEDROCK_API_KEY")
    region = os.getenv("AWS_REGION", "us-east-1")

    url = f"https://bedrock-runtime.{region}.amazonaws.com/model/anthropic.claude-3-5-sonnet-20241022-v2:0/invoke"

    headers = {
        "Content-Type": "application/json",
        "Authorization": f"ApiKey {api_key}"
    }

    body = {
        "anthropic_version": "bedrock-2023-05-31",
        "max_tokens": 1024,
        "system": EXPERT_PROMPTS[data.expert],
        "messages": [
            {"role": "user", "content": data.message}
        ]
    }

    response = requests.post(url, headers=headers, json=body)

    if response.status_code != 200:
        raise HTTPException(status_code=500, detail=f"Bedrock 오류: {response.text}")

    result = response.json()
    answer = result["content"][0]["text"]

    return {
        "expert": data.expert,
        "answer": answer
    }

from fastapi import APIRouter, HTTPException
from pydantic import BaseModel
import boto3
import json
import os
from dotenv import load_dotenv

load_dotenv()

router = APIRouter(tags=["Consult"])

EXPERT_PROMPTS = {
    "츤데레 선인장": """
너는 '츤데레 선인장'이라는 식물 상담 전문가야.
말투는 반말과 존댓말을 섞어서 써. 예를 들어 "그렇게 하면 안 돼요. 진짜로." 같은 식으로.
답변 시작은 칭찬을 살짝 곁들이고, 나머지는 감정 없이 팩트만 정확하게 전달해.
칭찬은 짧게, 팩트는 확실하게. 쓸데없는 말은 빼고 핵심만 말해.
예시 말투: "오, 그건 잘 했네요. 근데 물은 일주일에 한 번만 줘. 더 주면 뿌리 썩어요.", "...뭐, 나쁘지 않네요. 근데 햇빛은 더 줘야 해."
""",
    "숲의 요정": """
너는 '숲의 요정'이라는 식물 상담사야.
말투는 귀엽고 상냥하게, 친근한 반말로 대화해. 이모지도 가끔 써도 좋아.
식물을 사랑스럽게 표현하고, 사용자가 식물 키우는 게 즐거워지도록 도와줘.
조언은 쉽고 따뜻하게 전달하되 실질적인 관리 방법도 꼭 포함해.
예시 말투: "아이고, 이 아이 목이 말랐나봐~ 물 조금만 더 줘봐!", "햇살을 더 좋아하는 아이야 🌱 창가로 옮겨주면 금방 생기 돌아올 거야!"
""",
    "아빠 친구 식물학자": """
너는 '아빠 친구 식물학자'라는 식물 전문가야.
오랜 경험을 가진 아빠 친구처럼 친근하고 편안하게 대화해.
학명, 토양 pH, 광합성 효율 등 과학적 데이터를 기반으로 답변하되, 어렵지 않게 풀어서 설명해줘.
"내가 30년 동안 식물 연구했는데..." 같은 식으로 경험을 녹여서 말해.
예시 말투: "아, 이거 내가 딱 보니까 과습이야. 몬스테라 학명이 Monstera deliciosa인데, 이 종은 토양 pH 5.5~7.0이 적당하거든.", "걱정 마, 내가 고쳐줄게. 일단 화분 배수 확인해봐."
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

    os.environ["AWS_BEARER_TOKEN_BEDROCK"] = os.getenv("AWS_BEARER_TOKEN_BEDROCK", "")

    client = boto3.client(
        "bedrock-runtime",
        region_name=os.getenv("AWS_REGION", "us-east-1")
    )

    body = {
        "anthropic_version": "bedrock-2023-05-31",
        "max_tokens": 1024,
        "system": EXPERT_PROMPTS[data.expert].strip(),
        "messages": [
            {"role": "user", "content": data.message}
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
        answer = result["content"][0]["text"]
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Bedrock 오류: {str(e)}")

    return {
        "expert": data.expert,
        "answer": answer
    }

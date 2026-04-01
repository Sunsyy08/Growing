from fastapi import FastAPI
from routes import auth, my_plant, detail_my_plant, profile, plant_question, plant_updates, create_plant, consult, ai_analysis
import uvicorn

app = FastAPI(title="Growing Project")

# auth.py 파일에 있는 기능들을 연결
app.include_router(auth.router)
app.include_router(my_plant.router)
app.include_router(detail_my_plant.router)
app.include_router(profile.router)
app.include_router(plant_question.router)
app.include_router(plant_updates.router)
app.include_router(create_plant.router)
app.include_router(consult.router)
app.include_router(ai_analysis.router)

@app.get("/")
def root():
    return {"message": "회원가입/로그인 서버 가동 중"}

if __name__ == "__app__":
    uvicorn.run("app:app", host="0.0.0.0", port=8000, reload=True)
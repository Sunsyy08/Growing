from fastapi import FastAPI
from routes import auth
import uvicorn

app = FastAPI(title="Growing Project")

# auth.py 파일에 있는 기능들을 연결
app.include_router(auth.router)

@app.get("/")
def root():
    return {"message": "회원가입/로그인 서버 가동 중"}

if __name__ == "__app__":
    uvicorn.run("app:app", host="0.0.0.0", port=8000, reload=True)
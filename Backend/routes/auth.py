from fastapi import APIRouter, HTTPException
from database import get_db_connection
from pydantic import BaseModel, EmailStr

router = APIRouter(tags=["Auth"])

# 1. 회원가입용 데이터 모델
class SignupData(BaseModel):
    name: str
    email: EmailStr
    password: str

# 2. 로그인용 데이터 모델 (이게 추가되어야 422 에러가 안 납니다!)
class LoginData(BaseModel):
    email: EmailStr
    password: str

# [POST] 회원 가입
@router.post("/Signup")
def signup(data: SignupData):
    conn = get_db_connection()
    cursor = conn.cursor()
    
    try:
        # 이메일 중복 확인
        sql_check = "SELECT id FROM users WHERE email = %s"
        cursor.execute(sql_check, (data.email,))
        if cursor.fetchone():
            raise HTTPException(status_code=400, detail="이미 가입된 이메일입니다.")
        
        # 유저 저장
        sql_insert = "INSERT INTO users (name, email, password) VALUES (%s, %s, %s)"
        cursor.execute(sql_insert, (data.name, data.email, data.password))
        conn.commit()
    finally:
        conn.close()
        
    return {"message": "회원가입 성공!"}

# [POST] 로그인 (수정 완료)
@router.post("/login")
def login(data: LoginData):  # <- 여기서 email, password 대신 data: LoginData를 사용
    conn = get_db_connection()
    cursor = conn.cursor()
    
    try:
        # data.email, data.password로 접근해야 합니다.
        sql = "SELECT id, name FROM users WHERE email = %s AND password = %s"
        cursor.execute(sql, (data.email, data.password))
        user = cursor.fetchone()
    finally:
        conn.close()
        
    if not user:
        raise HTTPException(status_code=401, detail="이메일 또는 비밀번호가 틀렸습니다.")
    
    # 딕셔너리 형태로 결과 반환
    return {
        "message": "로그인 성공",
        "user_id": user['id'],
        "name": user['name']
    }
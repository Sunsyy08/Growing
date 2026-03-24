import pymysql

def get_db_connection():
    # 본인 DB 정보에 맞게 수정
    connection = pymysql.connect(
        host='localhost',
        user='root',
        password='hong1234',
        db='growing',
        charset='utf8mb4',
        cursorclass=pymysql.cursors.DictCursor # 결과를 딕셔너리 형태로 받음
    )
    return connection
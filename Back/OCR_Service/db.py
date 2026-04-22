import pymysql

conn = None
cursor = None


def connect_db():
    global conn, cursor

    conn = pymysql.connect(
        host="localhost",
        user="root",
        password="123456",
        database="doccontroller",
        charset="utf8mb4"
    )

    cursor = conn.cursor()

    print("数据库连接成功")


def get_cursor():
    return cursor


def commit():
    conn.commit()

def close_db():
    global conn, cursor

    if cursor:
        cursor.close()

    if conn:
        conn.close()

    print("数据库连接已关闭")



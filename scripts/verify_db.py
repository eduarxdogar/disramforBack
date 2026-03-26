import mysql.connector

DB_CONFIG = {
    'host': 'localhost',
    'user': 'root',
    'password': '', 
    'database': 'disramfor'
}

def main():
    try:
        conn = mysql.connector.connect(**DB_CONFIG)
        cursor = conn.cursor(dictionary=True)
        
        print("--- VERIFICACIÓN DE DATOS (MUESTRA DE 10 PRODUCTOS) ---")
        cursor.execute("SELECT codigo, imagen_url FROM producto WHERE imagen_url IS NOT NULL LIMIT 10")
        rows = cursor.fetchall()
        
        for r in rows:
            print(f"[{r['codigo']}] -> {r['imagen_url']}")
            
        conn.close()
    except Exception as e:
        print(e)

if __name__ == "__main__":
    main()

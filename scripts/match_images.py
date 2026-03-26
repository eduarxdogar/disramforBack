import mysql.connector
import os
import re

# --- CONFIGURACIÓN ---

# 1. Dónde buscar físicamente los archivos (Escaneo)
# Usamos la ruta que sabemos que funciona: src/assets
REAL_ASSETS_PATH = r"D:\devs\disramforFront\src\assets"

# 2. Qué guardar en la Base de Datos
# Las imágenes están directamente en /assets (estructura plana)
# Angular las buscará como: http://localhost:4200/assets/archivo.png
WEB_PREFIX = ""

DB_CONFIG = {
    'host': 'localhost',
    'user': 'root',
    'password': '', 
    'database': 'disramfor'
}

def get_db_connection():
    try:
        return mysql.connector.connect(**DB_CONFIG)
    except mysql.connector.Error as err:
        print(f"Error conectando a la BD: {err}")
        exit(1)

def normalize_key(text):
    """
    Normaliza el texto: minúsculas y solo alfanuméricos.
    """
    if not text:
        return ""
    name_without_ext = os.path.splitext(text)[0]
    return re.sub(r'[^a-zA-Z0-9]', '', name_without_ext).lower()

def main():
    print("--- INICIANDO SMART IMAGE MATCHER (CORREGIDO) ---")
    print(f"Ruta de Escaneo: {REAL_ASSETS_PATH}")
    print(f"Prefijo Web BD:  {WEB_PREFIX}")
    
    # 1. Escanear FileSystem
    if not os.path.exists(REAL_ASSETS_PATH):
        print(f"Error: La ruta de escaneo no existe.")
        exit(1)

    files = os.listdir(REAL_ASSETS_PATH)
    image_map = {} 

    for f in files:
        if f.lower().endswith(('.png', '.jpg', '.jpeg', '.webp')):
            norm_key = normalize_key(f)
            image_map[norm_key] = f

    print(f"Archivos encontrados en disco: {len(image_map)}")

    # 2. Conectar a BD
    conn = get_db_connection()
    cursor = conn.cursor(dictionary=True)
    
    cursor.execute("SELECT codigo, imagen_url FROM producto")
    products = cursor.fetchall()
    
    matches_found = 0
    updated_count = 0
    
    print("Procesando productos...")

    for prod in products:
        codigo = prod['codigo']
        current_db_url = prod['imagen_url']
        
        # Normalizar código de producto para buscar match
        db_key = normalize_key(codigo)
        
        if db_key in image_map:
            real_filename = image_map[db_key]
            
            # Construir la nueva URL deseada
            new_target_url = f"{WEB_PREFIX}{real_filename}"
            
            # FORZAR UPDATE SIEMPRE (sin validar si cambió)
            sql = "UPDATE producto SET imagen_url = %s WHERE codigo = %s"
            cursor.execute(sql, (new_target_url, codigo))
            updated_count += 1
            matches_found += 1

    # 3. COMMIT FINAL OBLIGATORIO
    conn.commit()
    
    cursor.close()
    conn.close()

    print("\n--- RESUMEN FINAL ---")
    print(f"Productos Totales: {len(products)}")
    print(f"Coincidencias (Matches): {matches_found}")
    print(f"UPDATES Ejecutados: {updated_count}")
    print("---------------------")

if __name__ == "__main__":
    main()

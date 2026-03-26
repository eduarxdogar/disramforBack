import mysql.connector
import re

# --- CONFIGURACIÓN ---
DB_CONFIG = {
    'host': 'localhost',
    'user': 'root',
    'password': '', # Ajustar si tienes password
    'database': 'disramfor'
}

# Configuración de Imágenes para Angular
# El frontend buscará en esta ruta relativa
PREFIX_IMG = "assets/img/productos/" 
IMAGE_EXTENSION = ".png" # Asumimos PNG, si tienes JPG cambiar aquí

# Listas Maestras para Filtros
BRANDS = [
    'CHEVROLET', 'MAZDA', 'RENAULT', 'HYUNDAI', 'KIA', 'TOYOTA', 'NISSAN', 
    'SUZUKI', 'DAEWOO', 'FORD', 'MITSUBISHI', 'HONDA', 'FIAT', 'PEUGEOT', 
    'VOLKSWAGEN', 'AUDI', 'BMW', 'MERCEDES', 'CITROEN', 'JEEP', 'SPARK', 'AVEO'
]

PRODUCT_TYPES = [
    'RETEN', 'JUNTA', 'EMPAQUETADURA', 'TORNILLO', 'KIT', 'BOMBA', 'CORREA', 
    'FILTRO', 'PASTILLA', 'DISCO', 'AMORTIGUADOR', 'BUJE', 'SOPORTE', 'CAMISA', 
    'ANILLO', 'CASQUETE', 'VALVULA', 'GUIA', 'GORRO', 'EMPAQUE'
]

def get_db_connection():
    try:
        return mysql.connector.connect(**DB_CONFIG)
    except mysql.connector.Error as err:
        print(f"Error conectando a la BD: {err}")
        exit(1)

def extract_info(name, codigo):
    name_upper = name.upper()
    info = {
        'tipo_producto': None,
        'marca': None,
        'modelo': None,
        'motor': None,
        'imagen_url': f"{PREFIX_IMG}{codigo}{IMAGE_EXTENSION}" # <--- AQUÍ LA MAGIA
    }

    # 1. Tipo de Producto
    for p_type in PRODUCT_TYPES:
        if p_type in name_upper:
            info['tipo_producto'] = p_type
            break
    if not info['tipo_producto']: info['tipo_producto'] = 'REPUESTO'

    # 2. Marca
    for brand in BRANDS:
        if brand in name_upper:
            info['marca'] = brand
            break 

    # 3. Modelo
    if info['marca']:
        pattern = re.compile(rf"{info['marca']}\s+([A-Z0-9\.-]+)")
        match = pattern.search(name_upper)
        if match:
            info['modelo'] = match.group(1).strip()

    # 4. Motor
    engine_patterns = [r'\b(\d\.\d)\b', r'\b(\d\.\dL)\b', r'\b(\d{3,4}CC)\b', r'\b(V6|V8)\b']
    for pat in engine_patterns:
        match = re.search(pat, name_upper)
        if match:
            info['motor'] = match.group(0)
            break

    return info

def main():
    conn = get_db_connection()
    cursor = conn.cursor(dictionary=True)
    print("--- 🚀 Iniciando Hidratación de Datos (Filtros + Imágenes) ---")

    cursor.execute("SELECT codigo, nombre FROM producto")
    products = cursor.fetchall()
    
    count = 0
    for prod in products:
        codigo = prod['codigo']
        name = prod['nombre']
        
        extracted = extract_info(name, codigo)
        
        sql = """
            UPDATE producto 
            SET tipo_producto = %s, marca = %s, modelo = %s, motor = %s, imagen_url = %s
            WHERE codigo = %s
        """
        val = (
            extracted['tipo_producto'], 
            extracted['marca'], 
            extracted['modelo'], 
            extracted['motor'], 
            extracted['imagen_url'],
            codigo
        )
        cursor.execute(sql, val)
        count += 1
        if count % 100 == 0:
            conn.commit()
            print(f"-> Procesados {count} productos...")

    conn.commit()
    print(f"\n--- ✅ Éxito. {count} productos actualizados con filtros e imágenes. ---")
    cursor.close()
    conn.close()

if __name__ == "__main__":
    main()

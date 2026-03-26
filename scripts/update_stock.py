import mysql.connector

# Configuración de la base de datos
DB_CONFIG = {
    'host': 'localhost',
    'user': 'root',
    'password': '', 
    'database': 'disramfor'
}

def update_all_stock():
    """Actualiza el stock de todos los productos a 100 para pruebas"""
    try:
        conn = mysql.connector.connect(**DB_CONFIG)
        cursor = conn.cursor()
        
        # Actualizar todos los productos a stock 100
        sql = "UPDATE producto SET stock_disponible = 100"
        cursor.execute(sql)
        
        # Commit de los cambios
        conn.commit()
        
        # Obtener cantidad de productos actualizados
        rows_affected = cursor.rowcount
        
        print(f"--- ACTUALIZACION DE STOCK COMPLETADA ---")
        print(f"Productos actualizados: {rows_affected}")
        print(f"Nuevo stock: 100 unidades")
        print("------------------------------------------")
        
        cursor.close()
        conn.close()
        
    except mysql.connector.Error as err:
        print(f"Error de base de datos: {err}")
        exit(1)

if __name__ == "__main__":
    update_all_stock()

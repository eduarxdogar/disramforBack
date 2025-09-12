
-- Este archivo se ejecuta ANTES que data.sql
-- Aquí puedes crear tablas adicionales o modificar el esquema para tests

-- Ejemplo: Tabla adicional para tests
-- CREATE TABLE IF NOT EXISTS test_audit (
--     id BIGINT AUTO_INCREMENT PRIMARY KEY,
--     action VARCHAR(50) NOT NULL,
--     entity_name VARCHAR(100) NOT NULL,
--     entity_id BIGINT,
--     timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP
-- );
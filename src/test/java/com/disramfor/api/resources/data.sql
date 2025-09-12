
-- Insertar categorías de prueba
INSERT INTO categorias (id, nombre) VALUES (1, 'Electrónicos');
INSERT INTO categorias (id, nombre) VALUES (2, 'Hogar');
INSERT INTO categorias (id, nombre) VALUES (3, 'Deportes');

-- Insertar productos de prueba
INSERT INTO producto (codigo, nombre, precio_unitario, categoria_id, imagen_url, pasillo, nivel, espacio)
VALUES ('PROD001', 'Producto Test 1', 100.00, 1, 'http://example.com/img1.jpg', 'A', 1, 1);

INSERT INTO producto (codigo, nombre, precio_unitario, categoria_id, imagen_url, pasillo, nivel, espacio)
VALUES ('PROD002', 'Producto Test 2', 50.00, 2, 'http://example.com/img2.jpg', 'B', 2, 3);

INSERT INTO producto (codigo, nombre, precio_unitario, categoria_id, imagen_url, pasillo, nivel, espacio)
VALUES ('PROD003', 'Producto Test 3', 75.00, 1, 'http://example.com/img3.jpg', 'C', 1, 5);

-- Insertar clientes de prueba
INSERT INTO cliente (cliente_id, nit, nombre, direccion, ciudad, telefono, email, asesor)
VALUES (1, '123456789', 'Cliente Test 1', 'Calle 123 #45-67', 'Bogotá', '3001234567', 'cliente1@test.com', 'Juan Pérez');

INSERT INTO cliente (cliente_id, nit, nombre, direccion, ciudad, telefono, email, asesor)
VALUES (2, '987654321', 'Cliente Test 2', 'Carrera 456 #78-90', 'Medellín', '3009876543', 'cliente2@test.com', 'Ana García');

-- Insertar usuarios de prueba (para tests de autenticación)
INSERT INTO usuarios (id, usuarionombre, email, password, rol)
VALUES (1, 'admin', 'admin@test.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl2j6OoaZr2', 'ADMIN');
-- Contraseña: "password123"

INSERT INTO usuarios (id, usuarionombre, email, password, rol)
VALUES (2, 'asesor1', 'asesor1@test.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl2j6OoaZr2', 'SUPERVISOR');
-- Contraseña: "password123"

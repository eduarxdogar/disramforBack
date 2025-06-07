package com.disramfor.api.repository;

import com.disramfor.api.entity.Producto;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IProductoRepository extends JpaRepository<Producto, String> {
    // Aquí puedes agregar métodos personalizados si es necesario
    // Por ejemplo, buscar productos por nombre, categoría, etc.
}

package com.disramfor.api.repository;

import com.disramfor.api.entity.Producto;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface IProductoRepository extends JpaRepository<Producto, String> , JpaSpecificationExecutor<Producto> {
    /**

     * Busca un producto por su código y aplica un bloqueo pesimista (PESSIMISTIC_WRITE).
     * Esto asegura que ninguna otra transacción pueda modificar este producto
     * hasta que la transacción actual termine, evitando condiciones de carrera al actualizar el stock.
     * @param codigo El código del producto a buscar.
     * @return Un Optional que contiene el producto si se encuentra.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM Producto p WHERE p.codigo = :codigo")
    Optional<Producto> findByIdWithLock(@Param("codigo") String codigo);

}

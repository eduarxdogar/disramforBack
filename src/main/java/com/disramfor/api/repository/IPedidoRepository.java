package com.disramfor.api.repository;

import com.disramfor.api.entity.Pedido;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface IPedidoRepository extends JpaRepository<Pedido, Long> {

    List<Pedido> findByClienteId(Long clienteId);

    // Query optimizada para listar pedidos
    // JOIN FETCH trae todo en UNA sola consulta
    @Query("""
            SELECT DISTINCT p
            FROM Pedido p
            JOIN FETCH p.cliente c
            LEFT JOIN FETCH p.detalles d
            LEFT JOIN FETCH d.autoPart
            """)
    Page<Pedido> findAllWithRelaciones(Pageable pageable);

    //  Query optimizada para un pedido específico
    @Query("""
            SELECT p
            FROM Pedido p
            JOIN FETCH p.cliente
            LEFT JOIN FETCH p.detalles d
            LEFT JOIN FETCH d.autoPart
            WHERE p.id = :id
            """)
    Optional<Pedido> findByIdWithRelaciones(Long id);

    //  Query para resumen (sin detalles)
    @Query("""
            SELECT p
            FROM Pedido p
            JOIN FETCH p.cliente
            """)
    Page<Pedido> findAllWithCliente(Pageable pageable);

    @Query("SELECT p FROM Pedido p WHERE p.cliente.asesor.id = :asesorId")
    Page<Pedido> findByAsesorId(@Param("asesorId") Long asesorId, Pageable pageable);
}

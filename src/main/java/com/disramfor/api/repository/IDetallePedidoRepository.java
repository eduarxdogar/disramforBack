package com.disramfor.api.repository;

import com.disramfor.api.entity.DetallePedido;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IDetallePedidoRepository extends JpaRepository<DetallePedido, Long> {
}

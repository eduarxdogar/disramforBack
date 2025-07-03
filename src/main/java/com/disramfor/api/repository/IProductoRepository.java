package com.disramfor.api.repository;

import com.disramfor.api.entity.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface IProductoRepository extends JpaRepository<Producto, String> , JpaSpecificationExecutor<Producto> {

}

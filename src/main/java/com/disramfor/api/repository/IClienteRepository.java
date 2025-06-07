package com.disramfor.api.repository;

import com.disramfor.api.entity.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IClienteRepository extends JpaRepository<Cliente, Long> {

    Optional<Cliente> findByNit(String nit);


    @Query("""
        SELECT c 
        FROM Cliente c 
        WHERE LOWER(c.nit)   LIKE LOWER(CONCAT('%', :term, '%'))
           OR LOWER(c.nombre) LIKE LOWER(CONCAT('%', :term, '%'))
    """)
    List<Cliente> searchByNitOrNombre(@Param("term") String term);

}

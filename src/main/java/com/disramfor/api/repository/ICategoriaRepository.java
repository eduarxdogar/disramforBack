package com.disramfor.api.repository;

import com.disramfor.api.entity.Categoria;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ICategoriaRepository extends JpaRepository<Categoria, Long> {
    // Additional query methods can be defined here if needed
}

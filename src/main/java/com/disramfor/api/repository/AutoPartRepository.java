package com.disramfor.api.repository;

import com.disramfor.api.entity.AutoPart;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface AutoPartRepository extends JpaRepository<AutoPart, String>, JpaSpecificationExecutor<AutoPart> {

    @Query("SELECT DISTINCT p.productType FROM AutoPart p")
    List<String> findDistinctProductTypes();

    @Query("SELECT DISTINCT p.brand FROM AutoPart p WHERE p.productType = :type")
    List<String> findDistinctBrandsByType(@Param("type") String type);

    @Query("SELECT DISTINCT p.model FROM AutoPart p WHERE p.productType = :type AND p.brand = :brand")
    List<String> findDistinctModelsByTypeAndBrand(@Param("type") String type, @Param("brand") String brand);

    @Query("SELECT DISTINCT p.engine FROM AutoPart p WHERE p.productType = :type AND p.brand = :brand AND p.model = :model")
    List<String> findDistinctEnginesByHierarchy(@Param("type") String type,
            @Param("brand") String brand,
            @Param("model") String model);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    List<AutoPart> findAllByIdIn(List<String> ids);
}

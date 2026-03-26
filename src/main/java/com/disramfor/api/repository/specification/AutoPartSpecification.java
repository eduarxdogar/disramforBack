package com.disramfor.api.repository.specification;

import com.disramfor.api.entity.AutoPart;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;

public class AutoPartSpecification {

    public static Specification<AutoPart> filterBy(String productType, String brand, String model, String engine,
            String searchTerm) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Hierarchy Filters (Case Insensitive)
            if (StringUtils.hasText(productType)) {
                predicates.add(criteriaBuilder.equal(criteriaBuilder.upper(root.get("productType")),
                        productType.toUpperCase()));
            }

            if (StringUtils.hasText(brand)) {
                predicates.add(criteriaBuilder.equal(criteriaBuilder.upper(root.get("brand")), brand.toUpperCase()));
            }

            if (StringUtils.hasText(model)) {
                predicates.add(criteriaBuilder.equal(criteriaBuilder.upper(root.get("model")), model.toUpperCase()));
            }

            if (StringUtils.hasText(engine)) {
                predicates.add(criteriaBuilder.equal(criteriaBuilder.upper(root.get("engine")), engine.toUpperCase()));
            }

            // General Search (Text) - Busca en ID (codigo) o Description (nombre)
            if (StringUtils.hasText(searchTerm)) {
                String likePattern = "%" + searchTerm.toUpperCase() + "%";
                Predicate searchPredicate = criteriaBuilder.or(
                        criteriaBuilder.like(criteriaBuilder.upper(root.get("id")), likePattern), // Busca en codigo
                        criteriaBuilder.like(criteriaBuilder.upper(root.get("description")), likePattern), // Busca en
                                                                                                           // Nombre
                        criteriaBuilder.like(criteriaBuilder.upper(root.get("brand")), likePattern), // Busca en Marca
                        criteriaBuilder.like(criteriaBuilder.upper(root.get("model")), likePattern) // Busca en Modelo
                );
                predicates.add(searchPredicate);
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}

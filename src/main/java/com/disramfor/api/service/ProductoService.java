package com.disramfor.api.service;

import com.disramfor.api.entity.Producto;
import com.disramfor.api.repository.IProductoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class ProductoService {
    private final IProductoRepository repo;

    public Page<Producto> buscar(String termino, Long categoriaId, Pageable pageable) {
        // Usamos Specification para construir la consulta dinámicamente
        Specification<Producto> spec = (root, query, cb) -> {

            if (termino == null || termino.isEmpty()) {
                return cb.conjunction(); // Si no hay término, no se aplica filtro de texto
            }
            String pattern = "%" + termino.toLowerCase() + "%";
            // Creamos una cláusula OR para buscar en ambas columnas
            return cb.or(
                    cb.like(cb.lower(root.get("nombre")), pattern),
                    cb.like(cb.lower(root.get("codigo")), pattern)
            );
        };
        if (categoriaId != null) {
            Specification<Producto> categoriaSpec = (root, query, cb) -> cb.equal(root.get("categoria").get("id"), categoriaId);
            spec = spec.and(categoriaSpec);
        }


        return repo.findAll(spec, pageable);
    }
}


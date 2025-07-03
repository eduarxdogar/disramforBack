package com.disramfor.api.controller;

import com.disramfor.api.entity.Producto;
import com.disramfor.api.service.ProductoService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/productos")
@RequiredArgsConstructor
public class ProductoController {
    private final ProductoService service;

    @GetMapping
    public Page<Producto> buscarProductos(
            @RequestParam(required = false) String termino,
            @RequestParam(required = false) Long categoriaId,
            Pageable pageable) {
        return service.buscar(termino, categoriaId, pageable);
    }
}
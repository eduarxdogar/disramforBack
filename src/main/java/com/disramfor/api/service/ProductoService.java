package com.disramfor.api.service;

import com.disramfor.api.entity.Producto;
import com.disramfor.api.repository.IProductoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductoService {
    private final IProductoRepository repo;

    public List<Producto> listar() {
        return repo.findAll();
    }
}
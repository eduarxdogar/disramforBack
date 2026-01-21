package com.disramfor.api.service;

import com.disramfor.api.dto.*;
import com.disramfor.api.dto.IPedidoMapper;
import com.disramfor.api.entity.*;
import com.disramfor.api.exception.BusinessException;
import com.disramfor.api.exception.ResourceNotFoundException;
import com.disramfor.api.repository.IClienteRepository;

import com.disramfor.api.repository.IPedidoRepository;
import com.disramfor.api.repository.IProductoRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;

@Service
@RequiredArgsConstructor
public class PedidoService {
    private final IPedidoRepository pedidoRepo;

    private final IClienteRepository clienteRepo;
    private final IProductoRepository productoRepo;
    private final IPedidoMapper mapper;

    @Value("${app.business.tasa-iva}")
    private BigDecimal tasaIva;

    @Transactional
    public PedidoResponseDTO crearPedido(PedidoRequestDTO req) {
        Cliente c = clienteRepo.findById(req.getClienteId())
                .orElseThrow(() -> new ResourceNotFoundException("Cliente no existe con ID: " + req.getClienteId()));

        Pedido pedido = new Pedido();
        pedido.setCliente(c);
        pedido.setFecha(LocalDateTime.now());
        pedido.setEstado(EstadoPedido.PENDIENTE);
        pedido.setDetalles(new ArrayList<>());

        // --- OPTIMIZACION N+1: Batch Fetching ---
        List<String> codigosProductos = req.getItems().stream()
                .map(DetallePedidoRequestDTO::getProductoCodigo)
                .toList();

        // Usamos Locking Pesimista para garantizar el stock
        Map<String, Producto> productosMap = productoRepo.findAllByCodigoIn(codigosProductos).stream()
                .collect(Collectors.toMap(Producto::getCodigo, p -> p));

        for (DetallePedidoRequestDTO item : req.getItems()) {
            Producto p = productosMap.get(item.getProductoCodigo());
            if (p == null) {
                throw new ResourceNotFoundException("Producto no existe: " + item.getProductoCodigo());
            }

            // --- LÓGICA DE STOCK ---
            if (p.getStockDisponible() < item.getCantidad()) {
                throw new BusinessException("Stock insuficiente para el producto: " + p.getNombre() + ". Disponibles: "
                        + p.getStockDisponible());
            }

            p.setStockDisponible(p.getStockDisponible() - item.getCantidad());
            // JPA manejará el update del producto al final de la transacción

            BigDecimal precio = p.getPrecioUnitario();
            BigDecimal subtotalItem = precio.multiply(BigDecimal.valueOf(item.getCantidad()));

            DetallePedido det = new DetallePedido();
            det.setPedido(pedido);
            det.setProducto(p);
            det.setCantidad(item.getCantidad());
            det.setPrecioUnitario(precio);
            det.setSubtotal(subtotalItem);

            pedido.getDetalles().add(det);
        }

        // --- LÓGICA DE CÁLCULO DE TOTALES (DRY) ---
        pedido.calcularTotales(tasaIva);

        Pedido guardado = pedidoRepo.save(pedido);
        return mapper.toResponse(guardado);
    }

    @Transactional(readOnly = true)
    public Page<PedidoResumenDTO> listarPedidosResumen(Pageable pageable) {
        return pedidoRepo.findAll(pageable).map(mapper::toResumen);
    }

    public PedidoResponseDTO obtenerPedido(Long id) {
        Pedido p = pedidoRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pedido no existe"));
        return mapper.toResponse(p);
    }

    public void eliminarPedido(Long id) {
        Pedido p = pedidoRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pedido no existe: " + id));
        pedidoRepo.delete(p);
    }

    @Transactional
    public void actualizarParcial(Long id, Map<String, Object> cambios) {
        Pedido p = pedidoRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pedido no existe: " + id));

        if (cambios.containsKey("estado")) {
            String s = cambios.get("estado").toString().toUpperCase();
            p.setEstado(EstadoPedido.valueOf(s));
        }
        if (cambios.containsKey("fecha")) {
            p.setFecha(LocalDateTime.parse(cambios.get("fecha").toString()));
        }
        if (cambios.containsKey("total")) {
            p.setTotal(new BigDecimal(cambios.get("total").toString()));
        }
        if (cambios.containsKey("clienteId")) {
            Long cid = Long.valueOf(cambios.get("clienteId").toString());
            Cliente c = clienteRepo.findById(cid)
                    .orElseThrow(() -> new ResourceNotFoundException("Cliente no existe: " + cid));
            p.setCliente(c);
        }

        pedidoRepo.save(p);
    }

    @Transactional
    public void actualizarEstado(Long id, EstadoPedido nuevoEstado) {
        Pedido pedido = pedidoRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pedido no encontrado con id: " + id));

        pedido.setEstado(nuevoEstado);
        pedidoRepo.save(pedido);
    }

    @Transactional
    public PedidoResponseDTO actualizarPedido(Long id, PedidoRequestDTO req) {
        Pedido pedido = pedidoRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pedido no encontrado con ID: " + id));

        if (pedido.getEstado() != EstadoPedido.PENDIENTE) {
            throw new IllegalStateException("Solo se pueden editar pedidos en estado PENDIENTE.");
        }

        // 1. Devolver stock de los detalles existentes antes de limpiar
        for (DetallePedido det : pedido.getDetalles()) {
            Producto p = det.getProducto();
            // Devolvemos el stock sin lock explícito aquí, confiamos en la atomicidad de la
            // transacción
            // Si se requiere estricto, habría que buscar el producto de nuevo con lock,
            // pero como estamos en la misma transacción y vamos a re-buscar p en breve si
            // se repite...
            // Mejor simple: devolvemos stock.
            p.setStockDisponible(p.getStockDisponible() + det.getCantidad());
            productoRepo.save(p); // Guardamos explícitamente para asegurar que el estado en BD refleje la
                                  // devolución
        }

        pedido.getDetalles().clear();

        Cliente cliente = clienteRepo.findById(req.getClienteId())
                .orElseThrow(
                        () -> new ResourceNotFoundException("Cliente no encontrado con ID: " + req.getClienteId()));

        pedido.setCliente(cliente);
        pedido.setFecha(LocalDateTime.now());

        // 2. Procesar nuevos productos con Batch Fetching
        List<String> codigosProductos = req.getItems().stream()
                .map(DetallePedidoRequestDTO::getProductoCodigo)
                .toList();

        Map<String, Producto> productosMap = productoRepo.findAllByCodigoIn(codigosProductos).stream()
                .collect(Collectors.toMap(Producto::getCodigo, p -> p));

        BigDecimal subtotalCalculado = BigDecimal.ZERO;
        for (DetallePedidoRequestDTO itemDTO : req.getItems()) {
            Producto producto = productosMap.get(itemDTO.getProductoCodigo());
            if (producto == null) {
                throw new ResourceNotFoundException("Producto no encontrado: " + itemDTO.getProductoCodigo());
            }

            // Stock Logic check
            if (producto.getStockDisponible() < itemDTO.getCantidad()) {
                throw new BusinessException("Stock insuficiente para el producto: " + producto.getNombre()
                        + ". Disponibles: " + producto.getStockDisponible());
            }

            // Descontar stock
            producto.setStockDisponible(producto.getStockDisponible() - itemDTO.getCantidad());
            // No save explícito necesario si está gestionado, pero por seguridad tras el
            // save anterior... JPA lo maneja.

            BigDecimal precio = producto.getPrecioUnitario();
            BigDecimal subtotalItem = precio.multiply(BigDecimal.valueOf(itemDTO.getCantidad()));

            DetallePedido detalle = new DetallePedido();
            detalle.setPedido(pedido);
            detalle.setProducto(producto);
            detalle.setCantidad(itemDTO.getCantidad());
            detalle.setPrecioUnitario(precio);
            detalle.setSubtotal(subtotalItem);

            pedido.getDetalles().add(detalle);
            subtotalCalculado = subtotalCalculado.add(subtotalItem);
        }

        // 3. Recalcular totales en el dominio
        pedido.calcularTotales(tasaIva);

        Pedido pedidoActualizado = pedidoRepo.save(pedido);
        return mapper.toResponse(pedidoActualizado);
    }

}

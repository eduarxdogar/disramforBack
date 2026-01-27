package com.disramfor.api.service;

import com.disramfor.api.dto.*;
import com.disramfor.api.dto.IPedidoMapper;
import com.disramfor.api.entity.*;
import com.disramfor.api.exception.BusinessException;
import com.disramfor.api.exception.ResourceNotFoundException;
import com.disramfor.api.repository.IClienteRepository;
import com.disramfor.api.repository.IPedidoRepository;
import com.disramfor.api.repository.AutoPartRepository;

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
    private final AutoPartRepository autoPartRepository;
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

        List<String> autoPartIds = req.getItems().stream()
                .map(DetallePedidoRequestDTO::getAutoPartId)
                .toList();

        Map<String, AutoPart> partsMap = autoPartRepository.findAllByIdIn(autoPartIds).stream()
                .collect(Collectors.toMap(AutoPart::getId, p -> p));

        for (DetallePedidoRequestDTO item : req.getItems()) {
            AutoPart p = partsMap.get(item.getAutoPartId());
            if (p == null) {
                throw new ResourceNotFoundException("AutoPart no existe con ID: " + item.getAutoPartId());
            }

            if (p.getStock() < item.getCantidad()) {
                throw new BusinessException("Stock insuficiente para: " + p.getDescription() + ". Disponibles: "
                        + p.getStock());
            }

            p.setStock(p.getStock() - item.getCantidad());

            BigDecimal precio = p.getPrice();
            BigDecimal subtotalItem = precio.multiply(BigDecimal.valueOf(item.getCantidad()));

            DetallePedido det = new DetallePedido();
            det.setPedido(pedido);
            det.setAutoPart(p);
            det.setCantidad(item.getCantidad());
            det.setPrecioUnitario(precio);
            det.setSubtotal(subtotalItem);

            pedido.getDetalles().add(det);
        }

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

        for (DetallePedido det : pedido.getDetalles()) {
            AutoPart p = det.getAutoPart();
            p.setStock(p.getStock() + det.getCantidad());
            autoPartRepository.save(p);
        }

        pedido.getDetalles().clear();

        Cliente cliente = clienteRepo.findById(req.getClienteId())
                .orElseThrow(
                        () -> new ResourceNotFoundException("Cliente no encontrado con ID: " + req.getClienteId()));

        pedido.setCliente(cliente);
        pedido.setFecha(LocalDateTime.now());

        List<String> autoPartIds = req.getItems().stream()
                .map(DetallePedidoRequestDTO::getAutoPartId)
                .toList();

        Map<String, AutoPart> partsMap = autoPartRepository.findAllByIdIn(autoPartIds).stream()
                .collect(Collectors.toMap(AutoPart::getId, p -> p));

        BigDecimal subtotalCalculado = BigDecimal.ZERO;
        for (DetallePedidoRequestDTO itemDTO : req.getItems()) {
            AutoPart p = partsMap.get(itemDTO.getAutoPartId());
            if (p == null) {
                throw new ResourceNotFoundException("AutoPart no encontrado: " + itemDTO.getAutoPartId());
            }

            if (p.getStock() < itemDTO.getCantidad()) {
                throw new BusinessException("Stock insuficiente para: " + p.getDescription()
                        + ". Disponibles: " + p.getStock());
            }

            p.setStock(p.getStock() - itemDTO.getCantidad());

            BigDecimal precio = p.getPrice();
            BigDecimal subtotalItem = precio.multiply(BigDecimal.valueOf(itemDTO.getCantidad()));

            DetallePedido detalle = new DetallePedido();
            detalle.setPedido(pedido);
            detalle.setAutoPart(p);
            detalle.setCantidad(itemDTO.getCantidad());
            detalle.setPrecioUnitario(precio);
            detalle.setSubtotal(subtotalItem);

            pedido.getDetalles().add(detalle);
            subtotalCalculado = subtotalCalculado.add(subtotalItem);
        }

        pedido.calcularTotales(tasaIva);

        Pedido pedidoActualizado = pedidoRepo.save(pedido);
        return mapper.toResponse(pedidoActualizado);
    }
}

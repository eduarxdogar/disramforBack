package com.disramfor.api.service;

import com.disramfor.api.dto.*;
import com.disramfor.api.dto.IPedidoMapper;
import com.disramfor.api.entity.*;
import com.disramfor.api.repository.IClienteRepository;
import com.disramfor.api.repository.IDetallePedidoRepository;
import com.disramfor.api.repository.IPedidoRepository;
import com.disramfor.api.repository.IProductoRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PedidoService {
    private final IPedidoRepository pedidoRepo;
    private final IDetallePedidoRepository detalleRepo;
    private final IClienteRepository clienteRepo;
    private final IProductoRepository productoRepo;
    private final IPedidoMapper mapper;

    @org.springframework.transaction.annotation.Transactional
    public PedidoResponseDTO crearPedido(PedidoRequestDTO req) {
        Cliente c = clienteRepo.findById(req.getClienteId())
                .orElseThrow(() -> new EntityNotFoundException("Cliente no existe"));

        Pedido pedido = new Pedido();
        pedido.setCliente(c);
        pedido.setFecha(LocalDateTime.now());
        pedido.setEstado(EstadoPedido.PENDIENTE);
        pedido.setDetalles(new ArrayList<>());

        BigDecimal total = BigDecimal.ZERO;

        for (DetallePedidoRequestDTO item : req.getItems()) {

            Producto p = productoRepo.findById(item.getProductoCodigo())
                    .orElseThrow(() -> new EntityNotFoundException(
                            "Producto no existe: " + item.getProductoCodigo()));

            BigDecimal precio     = p.getPrecioUnitario();
            BigDecimal subtotal   = precio.multiply(BigDecimal.valueOf(item.getCantidad()));

            DetallePedido det = new DetallePedido();
            det.setPedido(pedido);
            det.setProducto(p);
            det.setCantidad(item.getCantidad());
            det.setPrecioUnitario(precio);
            det.setSubtotal(subtotal);

            pedido.getDetalles().add(det);
            total = total.add(subtotal);
        }

        pedido.setTotal(total);
        Pedido guardado = pedidoRepo.save(pedido);
        return mapper.toResponse(guardado);
    }

    // ¡ESTE MÉTODO REEMPLAZA A TU ANTIGUO listarPedidos()!
    @org.springframework.transaction.annotation.Transactional(readOnly = true) // Buena práctica para métodos de solo lectura
    public Page<PedidoResumenDTO> listarPedidosResumen(Pageable pageable) {
        Page<Pedido> pedidosPage = pedidoRepo.findAll(pageable);
        return pedidosPage.map(this::convertirAPedidoResumenDTO);
    }

    // Método de ayuda para la conversión (Corregido)
    private PedidoResumenDTO convertirAPedidoResumenDTO(Pedido pedido) {
        PedidoResumenDTO dto = new PedidoResumenDTO();
        dto.setId(pedido.getId());
        dto.setFecha(pedido.getFecha());
        dto.setEstado(pedido.getEstado().name());
        dto.setTotal(pedido.getTotal());

        if (pedido.getCliente() != null) {
            dto.setClienteNombre(pedido.getCliente().getNombre());
            // CORRECCIÓN: Obtenemos el asesor desde el Cliente, como en tu modelo.
            dto.setAsesorNombre(pedido.getCliente().getAsesor());
        }

        return dto;
    }


    public PedidoResponseDTO obtenerPedido(Long id) {
        Pedido p = pedidoRepo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Pedido no existe"));
        return mapper.toResponse(p);
    }
    public void eliminarPedido(Long id) {
        Pedido p = pedidoRepo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Pedido no existe: " + id));
        pedidoRepo.delete(p);
    }

    @Transactional
    public void actualizarParcial(Long id, Map<String,Object> cambios) {
        Pedido p = pedidoRepo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Pedido no existe: " + id));

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
                    .orElseThrow(() -> new EntityNotFoundException("Cliente no existe: " + cid));
            p.setCliente(c);
        }
        // … demás campos que quieras exponer …

        pedidoRepo.save(p);
    }




}

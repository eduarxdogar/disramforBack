package com.disramfor.api.service;

import com.disramfor.api.dto.DetallePedidoRequestDTO;
import com.disramfor.api.dto.IPedidoMapper;
import com.disramfor.api.dto.PedidoRequestDTO;
import com.disramfor.api.dto.PedidoResponseDTO;
import com.disramfor.api.entity.*;
import com.disramfor.api.repository.IClienteRepository;
import com.disramfor.api.repository.IDetallePedidoRepository;
import com.disramfor.api.repository.IPedidoRepository;
import com.disramfor.api.repository.IProductoRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PedidoService {
    private final IPedidoRepository pedidoRepo;
    private final IDetallePedidoRepository detalleRepo;
    private final IClienteRepository clienteRepo;
    private final IProductoRepository productoRepo;
    private final IPedidoMapper mapper;

    @Transactional
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

    public List<PedidoResponseDTO> listarPedidos() {
        return pedidoRepo.findAll().stream()
                .map(mapper::toResponse)
                .toList();
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

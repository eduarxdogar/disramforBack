package com.disramfor.api.service;

import com.disramfor.api.dto.*;
import com.disramfor.api.dto.IPedidoMapper;
import com.disramfor.api.entity.*;
import com.disramfor.api.exception.ResourceNotFoundException;
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

    @Transactional
    public PedidoResponseDTO crearPedido(PedidoRequestDTO req) {
        Cliente c = clienteRepo.findById(req.getClienteId())
                .orElseThrow(() -> new EntityNotFoundException("Cliente no existe"));

        Pedido pedido = new Pedido();
        pedido.setCliente(c);
        pedido.setFecha(LocalDateTime.now());
        pedido.setEstado(EstadoPedido.PENDIENTE);
        pedido.setDetalles(new ArrayList<>());

        BigDecimal subtotalCalculado = BigDecimal.ZERO;

        for (DetallePedidoRequestDTO item : req.getItems()) {
            Producto p = productoRepo.findById(item.getProductoCodigo())
                    .orElseThrow(() -> new EntityNotFoundException(
                            "Producto no existe: " + item.getProductoCodigo()));

            BigDecimal precio = p.getPrecioUnitario();
            BigDecimal subtotalItem = precio.multiply(BigDecimal.valueOf(item.getCantidad()));

            DetallePedido det = new DetallePedido();
            det.setPedido(pedido);
            det.setProducto(p);
            det.setCantidad(item.getCantidad());
            det.setPrecioUnitario(precio);
            det.setSubtotal(subtotalItem);

            pedido.getDetalles().add(det);
            subtotalCalculado = subtotalCalculado.add(subtotalItem);
        }

        // --- LÓGICA DE CÁLCULO DE TOTALES ---
        BigDecimal descuentoPorcentaje = c.getDescuento() != null ? c.getDescuento() : BigDecimal.ZERO;
        BigDecimal montoDescuento = subtotalCalculado.multiply(descuentoPorcentaje);
        BigDecimal subtotalConDescuento = subtotalCalculado.subtract(montoDescuento);

        // Asumimos un IVA del 19% (0.19) - Puedes hacer esto configurable si lo necesitas
        BigDecimal tasaIva = new BigDecimal("0.19");
        BigDecimal montoIva = subtotalConDescuento.multiply(tasaIva);

        BigDecimal totalFinal = subtotalConDescuento.add(montoIva);

        pedido.setSubtotal(subtotalCalculado);
        pedido.setDescuento(montoDescuento);
        pedido.setIva(montoIva);
        pedido.setTotal(totalFinal);
        // ------------------------------------

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


    @Transactional
    public void actualizarEstado(Long id, EstadoPedido nuevoEstado) {
        // Busca el pedido en la base de datos o lanza una excepción si no lo encuentra.
        Pedido pedido = pedidoRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pedido no encontrado con id: " + id));

        // Actualiza el estado del pedido.
        pedido.setEstado(nuevoEstado);

        // Guarda los cambios en la base de datos.
        pedidoRepo.save(pedido);
    }


    @Transactional
    public PedidoResponseDTO actualizarPedido(Long id, PedidoRequestDTO req) {
        // 1. Buscar el pedido existente
        Pedido pedido = pedidoRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pedido no encontrado con ID: " + id));

        // 2. Validar que el pedido se pueda editar (solo si está pendiente)
        if (pedido.getEstado() != EstadoPedido.PENDIENTE) {
            throw new IllegalStateException("Solo se pueden editar pedidos en estado PENDIENTE.");
        }

        // 3. Limpiar los detalles anteriores para reemplazarlos
        // Usar orphanRemoval=true en la entidad Pedido asegura que se borren de la BD
        pedido.getDetalles().clear();

        // 4. Recalcular todo (la misma lógica de crearPedido, pero sobre el objeto existente)
        Cliente cliente = clienteRepo.findById(req.getClienteId())
                .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado con ID: " + req.getClienteId()));

        pedido.setCliente(cliente);
        pedido.setFecha(LocalDateTime.now()); // Actualizamos la fecha a la de la modificación

        BigDecimal subtotalCalculado = BigDecimal.ZERO;
        for (DetallePedidoRequestDTO itemDTO : req.getItems()) {
            Producto producto = productoRepo.findById(itemDTO.getProductoCodigo())
                    .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado: " + itemDTO.getProductoCodigo()));

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

        // 5. Calcular totales finales con la nueva información
        BigDecimal descuentoPorcentaje = cliente.getDescuento() != null ? cliente.getDescuento() : BigDecimal.ZERO;
        BigDecimal montoDescuento = subtotalCalculado.multiply(descuentoPorcentaje);
        BigDecimal subtotalConDescuento = subtotalCalculado.subtract(montoDescuento);

        BigDecimal tasaIva = new BigDecimal("0.19");
        BigDecimal montoIva = subtotalConDescuento.multiply(tasaIva);
        BigDecimal totalFinal = subtotalConDescuento.add(montoIva);

        pedido.setSubtotal(subtotalCalculado);
        pedido.setDescuento(montoDescuento);
        pedido.setIva(montoIva);
        pedido.setTotal(totalFinal);

        // 6. Guardar el pedido actualizado y devolver la respuesta
        Pedido pedidoActualizado = pedidoRepo.save(pedido);
        return mapper.toResponse(pedidoActualizado);
    }




}

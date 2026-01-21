package com.disramfor.api.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "pedido")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Pedido {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;

    private LocalDateTime fecha;

    @Enumerated(EnumType.STRING)
    private EstadoPedido estado;

    @Column(precision = 10, scale = 2)
    private BigDecimal subtotal;

    @Column(precision = 10, scale = 2)
    private BigDecimal descuento;

    @Column(precision = 10, scale = 2)
    private BigDecimal iva;

    @Column(precision = 10, scale = 2)
    private BigDecimal total;
    @JsonIgnore
    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DetallePedido> detalles = new ArrayList<>();

    public void calcularTotales(BigDecimal tasaIva) {
        this.subtotal = BigDecimal.ZERO;
        if (this.detalles != null) {
            for (DetallePedido det : this.detalles) {
                if (det.getSubtotal() != null) {
                    this.subtotal = this.subtotal.add(det.getSubtotal());
                }
            }
        }

        BigDecimal descuentoPorcentaje = (this.cliente != null && this.cliente.getDescuento() != null)
                ? this.cliente.getDescuento()
                : BigDecimal.ZERO;

        this.descuento = this.subtotal.multiply(descuentoPorcentaje);
        BigDecimal subtotalConDescuento = this.subtotal.subtract(this.descuento);

        this.iva = subtotalConDescuento.multiply(tasaIva != null ? tasaIva : BigDecimal.ZERO);
        this.total = subtotalConDescuento.add(this.iva);
    }
}
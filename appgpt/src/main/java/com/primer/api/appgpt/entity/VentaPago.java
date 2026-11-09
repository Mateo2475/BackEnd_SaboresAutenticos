package com.primer.api.appgpt.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.math.BigDecimal;

@Entity
@Table(
    name = "VentaPago",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"IdVenta", "MetodoPago"})
    }
)
public class VentaPago {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "IdVentaPago")
    private Integer idVentaPago;

    @ManyToOne
    @JoinColumn(name = "IdVenta", nullable = false)
    private Venta venta;

    @Column(name = "Monto", nullable = false, precision = 10, scale = 2)
    private BigDecimal monto;

    @Column(name = "MetodoPago", length = 15, nullable = false)
    private String metodoPago;

    public Integer getIdVentaPago() {
        return idVentaPago;
    }

    public void setIdVentaPago(Integer idVentaPago) {
        this.idVentaPago = idVentaPago;
    }

    public Venta getVenta() {
        return venta;
    }

    public void setVenta(Venta venta) {
        this.venta = venta;
    }

    public BigDecimal getMonto() {
        return monto;
    }

    public void setMonto(BigDecimal monto) {
        this.monto = monto;
    }

    public String getMetodoPago() {
        return metodoPago;
    }

    public void setMetodoPago(String metodoPago) {
        this.metodoPago = metodoPago;
    }

    

}
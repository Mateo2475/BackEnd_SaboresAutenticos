package com.primer.api.appgpt.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity 
@Table (
    name = "CuentaCredito", 
    uniqueConstraints = {
        @UniqueConstraint (columnNames = {"IdCliente", "Year", "Month"})
    }
)
public class CuentaCredito {

    @Id 
    @GeneratedValue (strategy = GenerationType.IDENTITY)
    @Column (name = "IdCuenta")
    private Integer idCuenta;

    @ManyToOne
    @JoinColumn(name = "IdCliente", nullable = false)
    private Cliente cliente;

    @Column(name = "Year", nullable = false)
    private Integer year;

    @Column(name = "Month", nullable = false)
    private Integer month;

    @Column(name = "LimiteCredito", nullable = false, precision = 10, scale = 2)
    private BigDecimal limiteCredito = new BigDecimal("500.00");

    @Column(name = "MontoConsumido", nullable = false, precision = 10, scale = 2)
    private BigDecimal montoConsumido = BigDecimal.ZERO;

    @Column(name = "MontoAbonado", nullable = false, precision = 10, scale = 2)
    private BigDecimal montoAbonado = BigDecimal.ZERO;

    @Column(name = "FechaSaldoCero")
    private LocalDateTime fechaSaldoCero;

    public Integer getIdCuenta() {
        return idCuenta;
    }

    public void setIdCuenta(Integer idCuenta) {
        this.idCuenta = idCuenta;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public void setCliente(Cliente cliente) {
        this.cliente = cliente;
    }

    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }

    public Integer getMonth() {
        return month;
    }

    public void setMonth(Integer month) {
        this.month = month;
    }

    public BigDecimal getLimiteCredito() {
        return limiteCredito;
    }

    public void setLimiteCredito(BigDecimal limiteCredito) {
        this.limiteCredito = limiteCredito;
    }

    public BigDecimal getMontoConsumido() {
        return montoConsumido;
    }

    public void setMontoConsumido(BigDecimal montoConsumido) {
        this.montoConsumido = montoConsumido;
    }

    public BigDecimal getMontoAbonado() {
        return montoAbonado;
    }

    public void setMontoAbonado(BigDecimal montoAbonado) {
        this.montoAbonado = montoAbonado;
    }

    public LocalDateTime getFechaSaldoCero() {
        return fechaSaldoCero;
    }

    public void setFechaSaldoCero(LocalDateTime fechaSaldoCero) {
        this.fechaSaldoCero = fechaSaldoCero;
    }

    

}

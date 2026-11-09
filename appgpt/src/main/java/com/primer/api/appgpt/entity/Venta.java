package com.primer.api.appgpt.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "Venta")
public class Venta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "IdVenta")
    private Integer idVenta;

    @ManyToOne
    @JoinColumn(name = "IdEmpleado", nullable = false)
    private Empleado empleado;

    @ManyToOne
    @JoinColumn(name = "IdCliente")
    private Cliente cliente;

    @ManyToOne
    @JoinColumn(name = "IdCuenta")
    private CuentaCredito cuentaCredito;

    @Column(name = "TipoPago", length = 10, nullable = false)
    private String tipoPago; 

    @Column(name = "Total", nullable = false, precision = 10, scale = 2)
    private BigDecimal total;

    @Column(name = "Estado", length = 10, nullable = false)
    private String estado = "REGISTRADA"; 

    @Column(name = "FechaHora", nullable = false)
    private LocalDateTime fechaHora = LocalDateTime.now(java.time.ZoneId.of("America/Lima"));

    //anulacions
    
    @Column(name = "MotivoAnulacion", length = 200)
    private String motivoAnulacion;

    @Column(name = "MfaVerificado", nullable = false)
    private boolean mfaVerificado = false;

    @ManyToOne
    @JoinColumn(name = "IdEmpleadoAnula")
    private Empleado empleadoAnula; 

    @Column(name = "FechaAnulacion")
    private LocalDateTime fechaAnulacion;

    public Integer getIdVenta() {
        return idVenta;
    }

    public void setIdVenta(Integer idVenta) {
        this.idVenta = idVenta;
    }

    public Empleado getEmpleado() {
        return empleado;
    }

    public void setEmpleado(Empleado empleado) {
        this.empleado = empleado;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public void setCliente(Cliente cliente) {
        this.cliente = cliente;
    }

    public CuentaCredito getCuentaCredito() {
        return cuentaCredito;
    }

    public void setCuentaCredito(CuentaCredito cuentaCredito) {
        this.cuentaCredito = cuentaCredito;
    }

    public String getTipoPago() {
        return tipoPago;
    }

    public void setTipoPago(String tipoPago) {
        this.tipoPago = tipoPago;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public LocalDateTime getFechaHora() {
        return fechaHora;
    }

    public void setFechaHora(LocalDateTime fechaHora) {
        this.fechaHora = fechaHora;
    }

    public String getMotivoAnulacion() {
        return motivoAnulacion;
    }

    public void setMotivoAnulacion(String motivoAnulacion) {
        this.motivoAnulacion = motivoAnulacion;
    }

    public boolean isMfaVerificado() {
        return mfaVerificado;
    }

    public void setMfaVerificado(boolean mfaVerificado) {
        this.mfaVerificado = mfaVerificado;
    }

    public Empleado getEmpleadoAnula() {
        return empleadoAnula;
    }

    public void setEmpleadoAnula(Empleado empleadoAnula) {
        this.empleadoAnula = empleadoAnula;
    }

    public LocalDateTime getFechaAnulacion() {
        return fechaAnulacion;
    }

    public void setFechaAnulacion(LocalDateTime fechaAnulacion) {
        this.fechaAnulacion = fechaAnulacion;
    }

    
}
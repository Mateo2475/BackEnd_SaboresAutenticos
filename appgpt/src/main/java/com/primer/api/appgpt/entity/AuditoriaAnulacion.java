package com.primer.api.appgpt.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "AuditoriaAnulacion")
public class AuditoriaAnulacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "IdAuditoria")
    private Integer idAuditoria;

    @ManyToOne
    @JoinColumn(name = "IdVenta")
    private Venta venta;

    @ManyToOne
    @JoinColumn(name = "IdEmpleadoAnula")
    private Empleado empleadoAnula;

    @Column(name = "Motivo", length = 255, nullable = false)
    private String motivo;

    @Column(name = "FechaHora")
    private LocalDateTime fechaHora = LocalDateTime.now(java.time.ZoneId.of("America/Lima"));

    public Integer getIdAuditoria() { return idAuditoria; }
    public void setIdAuditoria(Integer idAuditoria) { this.idAuditoria = idAuditoria; }
    public Venta getVenta() { return venta; }
    public void setVenta(Venta venta) { this.venta = venta; }
    public Empleado getEmpleadoAnula() { return empleadoAnula; }
    public void setEmpleadoAnula(Empleado empleadoAnula) { this.empleadoAnula = empleadoAnula; }
    public String getMotivo() { return motivo; }
    public void setMotivo(String motivo) { this.motivo = motivo; }
    public LocalDateTime getFechaHora() { return fechaHora; }
    public void setFechaHora(LocalDateTime fechaHora) { this.fechaHora = fechaHora; }
}
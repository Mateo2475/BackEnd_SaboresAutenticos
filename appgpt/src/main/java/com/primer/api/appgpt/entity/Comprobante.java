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
import java.time.LocalDateTime;

@Entity
@Table(
    name = "Comprobante",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"Tipo", "Serie", "Numero"})
    }
)
public class Comprobante {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "IdComprobante")
    private Integer idComprobante;

    @ManyToOne
    @JoinColumn(name = "IdVenta", nullable = false)
    private Venta venta;

    @Column(name = "Tipo", length = 12, nullable = false)
    private String tipo; 

    @Column(name = "Serie", length = 4, nullable = false)
    private String serie;

    @Column(name = "Numero", nullable = false)
    private Integer numero;

    @Column(name = "Total", nullable = false, precision = 10, scale = 2)
    private BigDecimal total;

    @Column(name = "FechaEmision", nullable = false)
    private LocalDateTime fechaEmision = LocalDateTime.now(java.time.ZoneId.of("America/Lima"));

    @Column(name = "EstadoSunat", length = 20)
    private String estadoSunat = "PENDIENTE";

    @Column(name = "XmlComprobante", columnDefinition = "LONGTEXT")
    private String xmlComprobante;

    @Column(name = "FirmaHash", length = 128)
    private String firmaHash;

    public Integer getIdComprobante() {
        return idComprobante;
    }

    public void setIdComprobante(Integer idComprobante) {
        this.idComprobante = idComprobante;
    }

    public Venta getVenta() {
        return venta;
    }

    public void setVenta(Venta venta) {
        this.venta = venta;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getSerie() {
        return serie;
    }

    public void setSerie(String serie) {
        this.serie = serie;
    }

    public Integer getNumero() {
        return numero;
    }

    public void setNumero(Integer numero) {
        this.numero = numero;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    public LocalDateTime getFechaEmision() {
        return fechaEmision;
    }

    public void setFechaEmision(LocalDateTime fechaEmision) {
        this.fechaEmision = fechaEmision;
    }

    public String getEstadoSunat() { return estadoSunat; }
    public void setEstadoSunat(String estadoSunat) { this.estadoSunat = estadoSunat; }
    public String getXmlComprobante() { return xmlComprobante; }
    public void setXmlComprobante(String xmlComprobante) { this.xmlComprobante = xmlComprobante; }
    public String getFirmaHash() { return firmaHash; }
    public void setFirmaHash(String firmaHash) { this.firmaHash = firmaHash; }


    
}
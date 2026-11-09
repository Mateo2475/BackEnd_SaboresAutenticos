package com.primer.api.appgpt.dto.response;

import java.math.BigDecimal;

public class CuentaCreditoResponse {

    private Integer idCliente;
    private String nombres;
    private String apellidos;
    private String dni;
    private Integer year;
    private Integer month;
    private BigDecimal limiteCredito;
    private BigDecimal montoConsumido;
    private BigDecimal montoAbonado;
    private BigDecimal deudaPendiente;
    private BigDecimal saldoDisponible;
    private String estadoDeuda; 

    public CuentaCreditoResponse() {
    }

    public Integer getIdCliente() {
        return idCliente;
    }

    public void setIdCliente(Integer idCliente) {
        this.idCliente = idCliente;
    }

    public String getNombres() {
        return nombres;
    }

    public void setNombres(String nombres) {
        this.nombres = nombres;
    }

    public String getApellidos() {
        return apellidos;
    }

    public void setApellidos(String apellidos) {
        this.apellidos = apellidos;
    }

    public String getDni() {
        return dni;
    }

    public void setDni(String dni) {
        this.dni = dni;
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

    public BigDecimal getDeudaPendiente() {
        return deudaPendiente;
    }

    public void setDeudaPendiente(BigDecimal deudaPendiente) {
        this.deudaPendiente = deudaPendiente;
    }

    public BigDecimal getSaldoDisponible() {
        return saldoDisponible;
    }

    public void setSaldoDisponible(BigDecimal saldoDisponible) {
        this.saldoDisponible = saldoDisponible;
    }

    public String getEstadoDeuda() {
        return estadoDeuda;
    }

    public void setEstadoDeuda(String estadoDeuda) {
        this.estadoDeuda = estadoDeuda;
    }
}
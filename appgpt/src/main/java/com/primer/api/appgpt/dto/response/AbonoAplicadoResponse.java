package com.primer.api.appgpt.dto.response;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Resultado de un abono que puede distribuirse entre deudas de varios meses.
 */
public class AbonoAplicadoResponse {

    private Integer idCliente;
    private String cliente;
    private BigDecimal montoSolicitado;
    private BigDecimal montoAplicado;
    private BigDecimal deudaRestante;
    private String mensaje;
    private List<AbonoResponse> abonos = new ArrayList<>();

    public AbonoAplicadoResponse() {
    }

    public Integer getIdCliente() {
        return this.idCliente;
    }

    public void setIdCliente(Integer idCliente) {
        this.idCliente = idCliente;
    }

    public String getCliente() {
        return cliente;
    }

    public void setCliente(String cliente) {
        this.cliente = cliente;
    }

    public BigDecimal getMontoSolicitado() {
        return montoSolicitado;
    }

    public void setMontoSolicitado(BigDecimal montoSolicitado) {
        this.montoSolicitado = montoSolicitado;
    }

    public BigDecimal getMontoAplicado() {
        return montoAplicado;
    }

    public void setMontoAplicado(BigDecimal montoAplicado) {
        this.montoAplicado = montoAplicado;
    }

    public BigDecimal getDeudaRestante() {
        return deudaRestante;
    }

    public void setDeudaRestante(BigDecimal deudaRestante) {
        this.deudaRestante = deudaRestante;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }

    public List<AbonoResponse> getAbonos() {
        return abonos;
    }

    public void setAbonos(List<AbonoResponse> abonos) {
        this.abonos = abonos;
    }
}

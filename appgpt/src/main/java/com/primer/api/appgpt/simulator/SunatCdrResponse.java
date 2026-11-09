package com.primer.api.appgpt.simulator;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Respuesta simulada de SUNAT (CDR - Constancia de Recepción).
 */
public class SunatCdrResponse {

    private boolean aceptado;
    private String codigoSunat;   // 0 = aceptado, 99 = observado/rechazado
    private String ticket;
    private String mensaje;
    private String xmlCdr;
    private LocalDateTime fecha;

    public SunatCdrResponse() {
    }

    public SunatCdrResponse(boolean aceptado, String codigoSunat, String ticket,
                            String mensaje, String xmlCdr, LocalDateTime fecha) {
        this.aceptado = aceptado;
        this.codigoSunat = codigoSunat;
        this.ticket = ticket;
        this.mensaje = mensaje;
        this.xmlCdr = xmlCdr;
        this.fecha = fecha;
    }

    public boolean isAceptado() {
        return aceptado;
    }

    public void setAceptado(boolean aceptado) {
        this.aceptado = aceptado;
    }

    public String getCodigoSunat() {
        return codigoSunat;
    }

    public void setCodigoSunat(String codigoSunat) {
        this.codigoSunat = codigoSunat;
    }

    public String getTicket() {
        return ticket;
    }

    public void setTicket(String ticket) {
        this.ticket = ticket;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }

    public String getXmlCdr() {
        return xmlCdr;
    }

    public void setXmlCdr(String xmlCdr) {
        this.xmlCdr = xmlCdr;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public void setFecha(LocalDateTime fecha) {
        this.fecha = fecha;
    }

    public static String nuevoTicket() {
        return String.valueOf(System.currentTimeMillis() % 1000000000L);
    }
}

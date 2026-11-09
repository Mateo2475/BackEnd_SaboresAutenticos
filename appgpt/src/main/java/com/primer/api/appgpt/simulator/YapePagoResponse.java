package com.primer.api.appgpt.simulator;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Respuesta simulada de Yape para un pago.
 */
public class YapePagoResponse {

    private boolean aprobado;
    private String codigoOperacion;
    private String mensaje;
    private BigDecimal monto;
    private LocalDateTime fecha;

    public YapePagoResponse() {
    }

    public YapePagoResponse(boolean aprobado, String codigoOperacion, String mensaje,
                            BigDecimal monto, LocalDateTime fecha) {
        this.aprobado = aprobado;
        this.codigoOperacion = codigoOperacion;
        this.mensaje = mensaje;
        this.monto = monto;
        this.fecha = fecha;
    }

    public boolean isAprobado() {
        return aprobado;
    }

    public void setAprobado(boolean aprobado) {
        this.aprobado = aprobado;
    }

    public String getCodigoOperacion() {
        return codigoOperacion;
    }

    public void setCodigoOperacion(String codigoOperacion) {
        this.codigoOperacion = codigoOperacion;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }

    public BigDecimal getMonto() {
        return monto;
    }

    public void setMonto(BigDecimal monto) {
        this.monto = monto;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public void setFecha(LocalDateTime fecha) {
        this.fecha = fecha;
    }

    public static String nuevoCodigoOperacion() {
        return "YAPE-" + UUID.randomUUID().toString().substring(0, 12).toUpperCase();
    }
}

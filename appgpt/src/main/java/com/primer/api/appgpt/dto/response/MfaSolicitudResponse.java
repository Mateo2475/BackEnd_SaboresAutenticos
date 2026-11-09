package com.primer.api.appgpt.dto.response;

/**
 * Respuesta de la simulación de MFA (RF11 / CU-020).
 * En un entorno real el código viajaría por SMS/correo y NO se devolvería en la API.
 */
public class MfaSolicitudResponse {

    private String codigo;
    private long expiraEnSegundos;
    private String mensaje;

    public MfaSolicitudResponse() {
    }

    public MfaSolicitudResponse(String codigo, long expiraEnSegundos, String mensaje) {
        this.codigo = codigo;
        this.expiraEnSegundos = expiraEnSegundos;
        this.mensaje = mensaje;
    }

    public String getCodigo() {
        return this.codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public long getExpiraEnSegundos() {
        return expiraEnSegundos;
    }

    public void setExpiraEnSegundos(long expiraEnSegundos) {
        this.expiraEnSegundos = expiraEnSegundos;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }
}

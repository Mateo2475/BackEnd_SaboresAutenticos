package com.primer.api.appgpt.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class AnulacionRequest {

    @NotBlank(message = "El motivo de anulación es obligatorio")
    @Size(min = 15, max = 200, message = "El motivo debe tener entre 15 y 200 caracteres")
    private String motivo;

    // RF11 / CU-020: código MFA simulado que autoriza la anulación.
    private String codigoMfa;

    // Ya no se usa: el anulador se toma siempre del token JWT. Se conserva por compatibilidad.
    private String usernameAnulador;

    public AnulacionRequest() {
    }

    public String getCodigoMfa() {
        return codigoMfa;
    }

    public void setCodigoMfa(String codigoMfa) {
        this.codigoMfa = codigoMfa;
    }

    public String getMotivo() {
        return motivo;
    }

    public void setMotivo(String motivo) {
        this.motivo = motivo;
    }

    public String getUsernameAnulador() {
        return usernameAnulador;
    }

    public void setUsernameAnulador(String usernameAnulador) {
        this.usernameAnulador = usernameAnulador;
    }
}

package com.primer.api.appgpt.dto.response;

import java.util.List;

public class AuthResponse {

    private String token;
    private String refreshToken;
    private String username;
    private List<String> roles;
    private long expiraEnSegundos;
    private Integer idEmpleado;

    public AuthResponse() {
    }

    public AuthResponse(String token, String refreshToken, String username,
                        List<String> roles, long expiraEnSegundos) {
        this(token, refreshToken, username, roles, expiraEnSegundos, null);
    }

    public AuthResponse(String token, String refreshToken, String username,
                        List<String> roles, long expiraEnSegundos, Integer idEmpleado) {
        this.token = token;
        this.refreshToken = refreshToken;
        this.username = username;
        this.roles = roles;
        this.expiraEnSegundos = expiraEnSegundos;
        this.idEmpleado = idEmpleado;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public List<String> getRoles() {
        return roles;
    }

    public void setRoles(List<String> roles) {
        this.roles = roles;
    }

    public long getExpiraEnSegundos() {
        return expiraEnSegundos;
    }

    public void setExpiraEnSegundos(long expiraEnSegundos) {
        this.expiraEnSegundos = expiraEnSegundos;
    }

    public Integer getIdEmpleado() {
        return idEmpleado;
    }

    public void setIdEmpleado(Integer idEmpleado) {
        this.idEmpleado = idEmpleado;
    }
}

package com.primer.api.appgpt.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "ParametroSistema")
public class ParametroSistema {

    @Id
    @Column(name = "Clave", length = 50)
    private String clave;

    @Column(name = "Valor", length = 100, nullable = false)
    private String valor;

    @Column(name = "Descripcion", length = 255)
    private String descripcion;

    public String getClave() { return clave; }
    public void setClave(String clave) { this.clave = clave; }
    public String getValor() { return valor; }
    public void setValor(String valor) { this.valor = valor; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
}
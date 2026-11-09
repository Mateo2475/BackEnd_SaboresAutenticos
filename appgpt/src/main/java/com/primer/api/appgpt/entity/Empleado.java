package com.primer.api.appgpt.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity 
@Table (name ="Empleado")
public class Empleado {

    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column (name = "IdEmpleado")
    private Integer idEmpleado;

    @OneToOne 
    @JoinColumn (name = "username", referencedColumnName = "username", unique = true)
    private Usuario usuario;
    @Column(name = "Nombres", length = 80, nullable = false)
    private String nombres;

    @Column(name = "Apellidos", length = 80, nullable = false)
    private String apellidos;

    @Column(name = "DNI", length = 8, nullable = false, unique = true)
    private String dni;

    @Column(name = "Correo", length = 100)
    private String correo;

    @Column(name = "Telefono", length = 15)
    private String telefono;

    public Integer getIdEmpleado() {
        return idEmpleado;
    }

    public void setIdEmpleado(Integer idEmpleado) {
        this.idEmpleado = idEmpleado;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
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

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }
    

}

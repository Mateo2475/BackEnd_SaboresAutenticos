package com.primer.api.appgpt.dto.response;

import java.time.LocalDateTime;
import java.util.List;

public class GuiaRemisionResponse {

    private Integer idGuia;
    private String numeroGuia;
    private String proveedor;
    private Integer idEmpleado;
    private String empleado;
    private LocalDateTime fechaIngreso;
    private List<DetalleResponse> detalles;

    public GuiaRemisionResponse() {
    }

    public Integer getIdGuia() {
        return idGuia;
    }

    public void setIdGuia(Integer idGuia) {
        this.idGuia = idGuia;
    }

    public String getNumeroGuia() {
        return numeroGuia;
    }

    public void setNumeroGuia(String numeroGuia) {
        this.numeroGuia = numeroGuia;
    }

    public String getProveedor() {
        return proveedor;
    }

    public void setProveedor(String proveedor) {
        this.proveedor = proveedor;
    }

    public Integer getIdEmpleado() {
        return idEmpleado;
    }

    public void setIdEmpleado(Integer idEmpleado) {
        this.idEmpleado = idEmpleado;
    }

    public String getEmpleado() {
        return empleado;
    }

    public void setEmpleado(String empleado) {
        this.empleado = empleado;
    }

    public LocalDateTime getFechaIngreso() {
        return fechaIngreso;
    }

    public void setFechaIngreso(LocalDateTime fechaIngreso) {
        this.fechaIngreso = fechaIngreso;
    }

    public List<DetalleResponse> getDetalles() {
        return detalles;
    }

    public void setDetalles(List<DetalleResponse> detalles) {
        this.detalles = detalles;
    }

    public static class DetalleResponse {
        private Integer idProducto;
        private String producto;
        private Integer cantidad;

        public Integer getIdProducto() {
            return idProducto;
        }

        public void setIdProducto(Integer idProducto) {
            this.idProducto = idProducto;
        }

        public String getProducto() {
            return producto;
        }

        public void setProducto(String producto) {
            this.producto = producto;
        }

        public Integer getCantidad() {
            return cantidad;
        }

        public void setCantidad(Integer cantidad) {
            this.cantidad = cantidad;
        }
    }
}

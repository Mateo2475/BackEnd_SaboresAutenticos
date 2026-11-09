package com.primer.api.appgpt.dto.request;

import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public class GuiaRemisionRequest {

    @NotBlank(message = "El número de guía es obligatorio")
    private String numeroGuia;

    @NotBlank(message = "El proveedor es obligatorio")
    private String proveedor;

    // Ya no se usa: el responsable es el usuario del token.
    private Integer idEmpleado;

    @NotEmpty(message = "La guía debe tener al menos un producto")
    private List<DetalleRequest> detalles;

    public GuiaRemisionRequest() {
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

    public List<DetalleRequest> getDetalles() {
        return detalles;
    }

    public void setDetalles(List<DetalleRequest> detalles) {
        this.detalles = detalles;
    }

    public static class DetalleRequest {

        @NotNull(message = "El producto es obligatorio")
        private Integer idProducto;

        @NotNull(message = "La cantidad es obligatoria")
        private Integer cantidad;

        public DetalleRequest() {
        }

        public Integer getIdProducto() {
            return idProducto;
        }

        public void setIdProducto(Integer idProducto) {
            this.idProducto = idProducto;
        }

        public Integer getCantidad() {
            return cantidad;
        }

        public void setCantidad(Integer cantidad) {
            this.cantidad = cantidad;
        }
    }
}

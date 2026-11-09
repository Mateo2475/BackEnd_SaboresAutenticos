package com.primer.api.appgpt.dto.request;

import java.math.BigDecimal;
import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public class VentaCrearRequest {

    private Integer idEmpleado;
    private Integer idCliente;  

    @NotBlank(message = "El tipo de pago es obligatorio")
    private String tipoPago;   

    @NotEmpty(message = "La venta debe incluir al menos un producto")
    @Valid
    private List<ItemCarritoRequest> items; 

    // Opcional: solo las ventas CONTADO llevan pagos (el servicio lo valida). CREDITO no.
    @Valid
    private List<PagoRequest> pagos;        

    public VentaCrearRequest() {
    }

    public Integer getIdEmpleado() {
        return idEmpleado;
    }

    public void setIdEmpleado(Integer idEmpleado) {
        this.idEmpleado = idEmpleado;
    }

    public Integer getIdCliente() {
        return idCliente;
    }

    public void setIdCliente(Integer idCliente) {
        this.idCliente = idCliente;
    }

    public String getTipoPago() {
        return tipoPago;
    }

    public void setTipoPago(String tipoPago) {
        this.tipoPago = tipoPago;
    }

    public List<ItemCarritoRequest> getItems() {
        return items;
    }

    public void setItems(List<ItemCarritoRequest> items) {
        this.items = items;
    }

    public List<PagoRequest> getPagos() {
        return pagos;
    }

    public void setPagos(List<PagoRequest> pagos) {
        this.pagos = pagos;
    }

    public static class ItemCarritoRequest {

        @NotNull(message = "El producto es obligatorio")
        private Integer idProducto;

        @NotNull(message = "La cantidad es obligatoria")
        @Min(value = 1, message = "La cantidad mínima es 1")
        private Integer cantidad;

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

    public static class PagoRequest {

        @NotBlank(message = "El método de pago es obligatorio")
        private String metodoPago; 

        @NotNull(message = "El monto es obligatorio")
        @DecimalMin(value = "0.01", message = "El monto debe ser mayor a 0")
        private BigDecimal monto;

        public String getMetodoPago() {
            return metodoPago;
        }

        public void setMetodoPago(String metodoPago) {
            this.metodoPago = metodoPago;
        }

        public BigDecimal getMonto() {
            return monto;
        }

        public void setMonto(BigDecimal monto) {
            this.monto = monto;
        }
    }
}
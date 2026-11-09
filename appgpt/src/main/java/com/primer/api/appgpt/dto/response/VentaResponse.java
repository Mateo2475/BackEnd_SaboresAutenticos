package com.primer.api.appgpt.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class VentaResponse {

    private Integer idVenta;
    private Integer idEmpleado;
    private String cajero;
    private Integer idCliente;
    private String cliente;
    private String tipoPago;
    private BigDecimal total;
    private String estado;
    private LocalDateTime fechaHora;
    private String motivoAnulacion;
    private boolean mfaVerificado;
    private LocalDateTime fechaAnulacion;
    private List<VentaDetalleResponse> detalles;
    private List<VentaPagoResponse> pagos;
    private ComprobanteResponse comprobante;

    public VentaResponse() {
    }

    public Integer getIdVenta() {
        return idVenta;
    }

    public void setIdVenta(Integer idVenta) {
        this.idVenta = idVenta;
    }

    public Integer getIdEmpleado() {
        return idEmpleado;
    }

    public void setIdEmpleado(Integer idEmpleado) {
        this.idEmpleado = idEmpleado;
    }

    public String getCajero() {
        return cajero;
    }

    public void setCajero(String cajero) {
        this.cajero = cajero;
    }

    public Integer getIdCliente() {
        return idCliente;
    }

    public void setIdCliente(Integer idCliente) {
        this.idCliente = idCliente;
    }

    public String getCliente() {
        return cliente;
    }

    public void setCliente(String cliente) {
        this.cliente = cliente;
    }

    public String getTipoPago() {
        return tipoPago;
    }

    public void setTipoPago(String tipoPago) {
        this.tipoPago = tipoPago;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public LocalDateTime getFechaHora() {
        return fechaHora;
    }

    public void setFechaHora(LocalDateTime fechaHora) {
        this.fechaHora = fechaHora;
    }

    public String getMotivoAnulacion() {
        return motivoAnulacion;
    }

    public void setMotivoAnulacion(String motivoAnulacion) {
        this.motivoAnulacion = motivoAnulacion;
    }

    public boolean isMfaVerificado() {
        return mfaVerificado;
    }

    public void setMfaVerificado(boolean mfaVerificado) {
        this.mfaVerificado = mfaVerificado;
    }

    public LocalDateTime getFechaAnulacion() {
        return fechaAnulacion;
    }

    public void setFechaAnulacion(LocalDateTime fechaAnulacion) {
        this.fechaAnulacion = fechaAnulacion;
    }

    public List<VentaDetalleResponse> getDetalles() {
        return detalles;
    }

    public void setDetalles(List<VentaDetalleResponse> detalles) {
        this.detalles = detalles;
    }

    public List<VentaPagoResponse> getPagos() {
        return pagos;
    }

    public void setPagos(List<VentaPagoResponse> pagos) {
        this.pagos = pagos;
    }

    public ComprobanteResponse getComprobante() {
        return comprobante;
    }

    public void setComprobante(ComprobanteResponse comprobante) {
        this.comprobante = comprobante;
    }

    public static class VentaDetalleResponse {
        private Integer idProducto;
        private String producto;
        private Integer cantidad;
        private BigDecimal precioUnitario;
        private BigDecimal subtotal;

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

        public BigDecimal getPrecioUnitario() {
            return precioUnitario;
        }

        public void setPrecioUnitario(BigDecimal precioUnitario) {
            this.precioUnitario = precioUnitario;
        }

        public BigDecimal getSubtotal() {
            return subtotal;
        }

        public void setSubtotal(BigDecimal subtotal) {
            this.subtotal = subtotal;
        }
    }

    public static class VentaPagoResponse {
        private String metodoPago;
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

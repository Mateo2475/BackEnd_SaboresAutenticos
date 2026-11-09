package com.primer.api.appgpt.dto;

import java.util.ArrayList;
import java.util.List;

import com.primer.api.appgpt.dto.response.AbonoResponse;
import com.primer.api.appgpt.dto.response.ClienteResponse;
import com.primer.api.appgpt.dto.response.ComprobanteResponse;
import com.primer.api.appgpt.dto.response.EmpleadoResponse;
import com.primer.api.appgpt.dto.response.GuiaRemisionResponse;
import com.primer.api.appgpt.dto.response.VentaResponse;
import com.primer.api.appgpt.entity.Abono;
import com.primer.api.appgpt.entity.Cliente;
import com.primer.api.appgpt.entity.Comprobante;
import com.primer.api.appgpt.entity.Empleado;
import com.primer.api.appgpt.entity.GuiaRemision;
import com.primer.api.appgpt.entity.GuiaRemisionDetalle;
import com.primer.api.appgpt.entity.Venta;
import com.primer.api.appgpt.entity.VentaDetalle;
import com.primer.api.appgpt.entity.VentaPago;

/**
 * Conversión de entidades JPA a DTOs de respuesta.
 * Evita exponer entidades crudas en la API REST.
 */
public final class EntityMapper {

    private EntityMapper() {
    }

    public static ClienteResponse toClienteResponse(Cliente c) {
        if (c == null) {
            return null;
        }
        ClienteResponse dto = new ClienteResponse();
        dto.setIdCliente(c.getIdCliente());
        dto.setNombres(c.getNombres());
        dto.setApellidos(c.getApellidos());
        dto.setDni(c.getDni());
        dto.setCorreo(c.getCorreo());
        dto.setTelefono(c.getTelefono());
        dto.setArea(c.getArea());
        dto.setActivo(c.isActivo());
        return dto;
    }

    public static EmpleadoResponse toEmpleadoResponse(Empleado e) {
        if (e == null) {
            return null;
        }
        EmpleadoResponse dto = new EmpleadoResponse();
        dto.setIdEmpleado(e.getIdEmpleado());
        dto.setUsername(e.getUsuario() != null ? e.getUsuario().getUsername() : null);
        dto.setNombres(e.getNombres());
        dto.setApellidos(e.getApellidos());
        dto.setDni(e.getDni());
        dto.setCorreo(e.getCorreo());
        dto.setTelefono(e.getTelefono());
        return dto;
    }

    public static VentaResponse toVentaResponse(Venta v, List<VentaDetalle> detalles,
                                                List<VentaPago> pagos, Comprobante comprobante) {
        if (v == null) {
            return null;
        }
        VentaResponse dto = new VentaResponse();
        dto.setIdVenta(v.getIdVenta());
        dto.setIdEmpleado(v.getEmpleado() != null ? v.getEmpleado().getIdEmpleado() : null);
        dto.setCajero(v.getEmpleado() != null
                ? v.getEmpleado().getNombres() + " " + v.getEmpleado().getApellidos() : null);
        dto.setIdCliente(v.getCliente() != null ? v.getCliente().getIdCliente() : null);
        dto.setCliente(v.getCliente() != null
                ? v.getCliente().getNombres() + " " + v.getCliente().getApellidos() : null);
        dto.setTipoPago(v.getTipoPago());
        dto.setTotal(v.getTotal());
        dto.setEstado(v.getEstado());
        dto.setFechaHora(v.getFechaHora());
        dto.setMotivoAnulacion(v.getMotivoAnulacion());
        dto.setMfaVerificado(v.isMfaVerificado());
        dto.setFechaAnulacion(v.getFechaAnulacion());

        if (detalles != null) {
            List<VentaResponse.VentaDetalleResponse> listaDetalles = new ArrayList<>();
            for (VentaDetalle d : detalles) {
                VentaResponse.VentaDetalleResponse dr = new VentaResponse.VentaDetalleResponse();
                dr.setIdProducto(d.getProducto() != null ? d.getProducto().getIdProducto() : null);
                dr.setProducto(d.getProducto() != null ? d.getProducto().getNombre() : null);
                dr.setCantidad(d.getCantidad());
                dr.setPrecioUnitario(d.getPrecioUnitario());
                dr.setSubtotal(d.getPrecioUnitario() != null && d.getCantidad() != null
                        ? d.getPrecioUnitario().multiply(new java.math.BigDecimal(d.getCantidad()))
                        : null);
                listaDetalles.add(dr);
            }
            dto.setDetalles(listaDetalles);
        }

        if (pagos != null) {
            List<VentaResponse.VentaPagoResponse> listaPagos = new ArrayList<>();
            for (VentaPago p : pagos) {
                VentaResponse.VentaPagoResponse pr = new VentaResponse.VentaPagoResponse();
                pr.setMetodoPago(p.getMetodoPago());
                pr.setMonto(p.getMonto());
                listaPagos.add(pr);
            }
            dto.setPagos(listaPagos);
        }

        dto.setComprobante(toComprobanteResponse(comprobante));
        return dto;
    }

    public static ComprobanteResponse toComprobanteResponse(Comprobante c) {
        if (c == null) {
            return null;
        }
        ComprobanteResponse dto = new ComprobanteResponse();
        dto.setIdComprobante(c.getIdComprobante());
        dto.setIdVenta(c.getVenta() != null ? c.getVenta().getIdVenta() : null);
        dto.setTipo(c.getTipo());
        dto.setSerie(c.getSerie());
        dto.setNumero(c.getNumero());
        dto.setTotal(c.getTotal());
        dto.setFechaEmision(c.getFechaEmision());
        return dto;
    }

    public static AbonoResponse toAbonoResponse(Abono a) {
        if (a == null) {
            return null;
        }
        AbonoResponse dto = new AbonoResponse();
        dto.setIdAbono(a.getIdAbono());
        if (a.getCuentaCredito() != null) {
            dto.setIdCuenta(a.getCuentaCredito().getIdCuenta());
            if (a.getCuentaCredito().getCliente() != null) {
                dto.setIdCliente(a.getCuentaCredito().getCliente().getIdCliente());
                dto.setCliente(a.getCuentaCredito().getCliente().getNombres() + " "
                        + a.getCuentaCredito().getCliente().getApellidos());
            }
        }
        if (a.getEmpleado() != null) {
            dto.setIdEmpleado(a.getEmpleado().getIdEmpleado());
            dto.setCajero(a.getEmpleado().getNombres() + " " + a.getEmpleado().getApellidos());
        }
        dto.setMonto(a.getMonto());
        dto.setMetodoPago(a.getMetodoPago());
        dto.setFechaHora(a.getFechaHora());
        return dto;
    }

    public static GuiaRemisionResponse toGuiaResponse(GuiaRemision g, List<GuiaRemisionDetalle> detalles) {
        if (g == null) {
            return null;
        }
        GuiaRemisionResponse dto = new GuiaRemisionResponse();
        dto.setIdGuia(g.getIdGuia());
        dto.setNumeroGuia(g.getNumeroGuia());
        dto.setProveedor(g.getProveedor());
        dto.setFechaIngreso(g.getFechaIngreso());
        if (g.getEmpleado() != null) {
            dto.setIdEmpleado(g.getEmpleado().getIdEmpleado());
            dto.setEmpleado(g.getEmpleado().getNombres() + " " + g.getEmpleado().getApellidos());
        }
        if (detalles != null) {
            List<GuiaRemisionResponse.DetalleResponse> lista = new ArrayList<>();
            for (GuiaRemisionDetalle d : detalles) {
                GuiaRemisionResponse.DetalleResponse dr = new GuiaRemisionResponse.DetalleResponse();
                dr.setIdProducto(d.getProducto() != null ? d.getProducto().getIdProducto() : null);
                dr.setProducto(d.getProducto() != null ? d.getProducto().getNombre() : null);
                dr.setCantidad(d.getCantidad());
                lista.add(dr);
            }
            dto.setDetalles(lista);
        }
        return dto;
    }
}

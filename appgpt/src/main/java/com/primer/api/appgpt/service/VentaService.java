package com.primer.api.appgpt.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.primer.api.appgpt.entity.Cliente;
import com.primer.api.appgpt.entity.Empleado;
import com.primer.api.appgpt.entity.Producto;
import com.primer.api.appgpt.entity.Venta;
import com.primer.api.appgpt.entity.VentaDetalle;
import com.primer.api.appgpt.entity.VentaPago;
import com.primer.api.appgpt.entity.AuditoriaAnulacion;
import com.primer.api.appgpt.repository.AuditoriaAnulacionRepository;
import com.primer.api.appgpt.repository.ProductoRepository;
import com.primer.api.appgpt.repository.VentaRepository;

import com.primer.api.appgpt.repository.VentaDetalleRepository;
import com.primer.api.appgpt.repository.VentaPagoRepository;
import com.primer.api.appgpt.simulator.YapePagoResponse;
import com.primer.api.appgpt.simulator.YapeSimulatorService;

@Service
public class VentaService {

    // El cajero cobra por QR y no conoce el teléfono del cliente; el simulador exige uno válido.
    private static final String TELEFONO_YAPE_SIMULADO = "999999999";

    @Autowired
    private VentaRepository ventaRepository;

    @Autowired
    private VentaDetalleRepository ventaDetalleRepository;

    @Autowired
    private VentaPagoRepository ventaPagoRepository;

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private CuentaCreditoService cuentaCreditoService;

    @Autowired
    private ComprobanteService comprobanteService;

    @Autowired
    private YapeSimulatorService yapeSimulatorService;

    @Autowired
    private NotificacionService notificacionService;

    @Autowired
    private AuditoriaAnulacionRepository auditoriaAnulacionRepository;

    @Transactional
    public Venta procesarVenta(Empleado cajero, Cliente cliente, String tipoPago,
                               List<VentaDetalle> carrito, List<VentaPago> pagos) {

        Venta nuevaVenta = new Venta();
        nuevaVenta.setEmpleado(cajero);
        nuevaVenta.setCliente(cliente);
        nuevaVenta.setTipoPago(tipoPago);
        nuevaVenta.setFechaHora(LocalDateTime.now(java.time.ZoneId.of("America/Lima")));
        nuevaVenta.setEstado("REGISTRADA");
        nuevaVenta.setTotal(BigDecimal.ZERO); // la columna Total es NOT NULL: se recalcula más abajo

        BigDecimal totalVenta = BigDecimal.ZERO;

        Venta ventaGuardada = ventaRepository.save(nuevaVenta);

        for (VentaDetalle item : carrito) {
            // RF10 / CU-011: bloqueo pesimista de la fila del producto para que dos
            // cajeros no puedan vender la misma última unidad al mismo tiempo.
            Producto productoBD = productoRepository.findByIdForUpdate(item.getProducto().getIdProducto())
                    .orElseThrow(() -> new RuntimeException("Producto no encontrado."));

            if (!productoBD.isActivo()) {
                throw new RuntimeException("El producto está desactivado: " + productoBD.getNombre());
            }

            if (productoBD.getStock() < item.getCantidad()) {
                throw new RuntimeException("Stock insuficiente para el producto: " + productoBD.getNombre());
            }

            productoBD.setStock(productoBD.getStock() - item.getCantidad());
            productoRepository.save(productoBD);

            item.setPrecioUnitario(productoBD.getPrecio());
            item.setVenta(ventaGuardada);

            BigDecimal subtotal = item.getPrecioUnitario().multiply(new BigDecimal(item.getCantidad()));
            totalVenta = totalVenta.add(subtotal);

            ventaDetalleRepository.save(item);
        }

        ventaGuardada.setTotal(totalVenta);

        if (tipoPago.equalsIgnoreCase("CREDITO")) {

            // CU-015: Validar Límite de Crédito
            if (cliente == null) {
                throw new RuntimeException("Para vender a crédito es obligatorio seleccionar un cliente.");
            }

            var cuentaActualizada = cuentaCreditoService.validarYRegistrarConsumo(cliente.getIdCliente(), totalVenta);

            ventaGuardada.setCuentaCredito(cuentaActualizada);

        } else {

            if (pagos == null || pagos.isEmpty()) {
                throw new RuntimeException("Debe especificar al menos un método de pago para ventas al contado.");
            }

            for (VentaPago pago : pagos) {
                String metodo = pago.getMetodoPago().toUpperCase();
                if (!metodo.equals("EFECTIVO") && !metodo.equals("YAPE")) {
                    throw new RuntimeException("Método de pago no válido: " + metodo);
                }
            }

            if (pagos.size() > 1) {
                if (pagos.size() > 2) {
                    throw new RuntimeException("Solo se permite combinar un máximo de 2 métodos de pago (YAPE y EFECTIVO).");
                }
                for (VentaPago pago : pagos) {
                    String metodo = pago.getMetodoPago().toUpperCase();
                    if (!metodo.equals("YAPE") && !metodo.equals("EFECTIVO")) {
                        throw new RuntimeException("Regla de negocio: Para pagos combinados, SOLO se permite combinar YAPE y EFECTIVO. No se admite combinar con: " + metodo);
                    }
                }
            }

            // Procesamos los cobros
            BigDecimal sumaPagos = BigDecimal.ZERO;
            for (VentaPago pago : pagos) {
                pago.setMetodoPago(pago.getMetodoPago().toUpperCase());
                pago.setVenta(ventaGuardada);

                // ACTOR EXTERNO: si paga con Yape se consulta el simulador
                if ("YAPE".equals(pago.getMetodoPago())) {
                    YapePagoResponse respuestaYape = yapeSimulatorService.pagar(
                            pago.getMonto(), TELEFONO_YAPE_SIMULADO, "VENTA-" + ventaGuardada.getIdVenta());
                    if (!respuestaYape.isAprobado()) {
                        throw new RuntimeException(respuestaYape.getMensaje());
                    }
                }

                sumaPagos = sumaPagos.add(pago.getMonto());
                ventaPagoRepository.save(pago);
            }

            // RF19 / RF20: el pago (simple o combinado) debe coincidir EXACTAMENTE
            // con el total de la venta. No se aceptan montos mayores.
            if (sumaPagos.compareTo(totalVenta) != 0) {
                throw new RuntimeException("El monto ingresado (S/ " + sumaPagos
                        + ") no cubre el total de la venta (S/ " + totalVenta + "). "
                        + "Debe ser exactamente igual al total.");
            }
        }

        if (totalVenta.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("El total de la venta debe ser mayor a S/ 0.00.");
        }

        Venta ventaFinal = ventaRepository.save(ventaGuardada);

        // ACTOR EXTERNO: comprobante electrónico enviado a SUNAT (simulado)
        comprobanteService.generarPorVenta(ventaFinal);

        // WEBSOCKET: notificar a los clientes conectados
        notificacionService.notificarNuevaVenta(ventaFinal);

        return ventaFinal;
    }

    /**
     * Anulación de venta (CU de administrador/cajero):
     * - Cambia estado a ANULADA
     * - Repone el stock descontado
     * - Revierte el consumo de crédito si la venta era a crédito
     * - Emite Nota de Crédito hacia SUNAT (simulado)
     */
    @Transactional
    public Venta anularVenta(Integer idVenta, String motivo, Empleado empleadoAnula,
                             boolean mfaVerificado) {
        Venta venta = ventaRepository.findById(idVenta)
                .orElseThrow(() -> new RuntimeException("Venta no encontrada con ID: " + idVenta));

        if ("ANULADA".equals(venta.getEstado())) {
            throw new RuntimeException("La venta ya fue anulada.");
        }

        // 1. Reponer stock
        List<VentaDetalle> detalles = ventaDetalleRepository.findByVentaIdVenta(idVenta);
        for (VentaDetalle detalle : detalles) {
            Producto producto = detalle.getProducto();
            producto.setStock(producto.getStock() + detalle.getCantidad());
            productoRepository.save(producto);
        }

        // 2. Revertir consumo de crédito
        if (venta.getCuentaCredito() != null) {
            var cuenta = venta.getCuentaCredito();
            BigDecimal nuevoConsumido = cuenta.getMontoConsumido().subtract(venta.getTotal());
            if (nuevoConsumido.compareTo(BigDecimal.ZERO) < 0) {
                nuevoConsumido = BigDecimal.ZERO;
            }
            cuenta.setMontoConsumido(nuevoConsumido);
            if (nuevoConsumido.compareTo(cuenta.getMontoAbonado()) <= 0) {
                cuenta.setFechaSaldoCero(LocalDateTime.now(java.time.ZoneId.of("America/Lima")));
            }
            cuentaCreditoService.actualizarCuenta(cuenta);
        }

        // 3. Marcar anulación
        venta.setEstado("ANULADA");
        venta.setMotivoAnulacion(motivo);
        venta.setMfaVerificado(mfaVerificado);
        venta.setEmpleadoAnula(empleadoAnula);
        venta.setFechaAnulacion(LocalDateTime.now(java.time.ZoneId.of("America/Lima")));
        Venta ventaAnulada = ventaRepository.save(venta);

        // 4. Auditoría de anulación (RF11)
        AuditoriaAnulacion auditoria = new AuditoriaAnulacion();
        auditoria.setVenta(ventaAnulada);
        auditoria.setEmpleadoAnula(empleadoAnula);
        auditoria.setMotivo(motivo);
        auditoria.setFechaHora(LocalDateTime.now(java.time.ZoneId.of("America/Lima")));
        auditoriaAnulacionRepository.save(auditoria);

        // 5. Nota de Crédito solo si la venta no es del mismo día (RF13 / RF21)
        boolean esVentaDelDia = venta.getFechaHora() != null
                && venta.getFechaHora().toLocalDate().equals(LocalDate.now(java.time.ZoneId.of("America/Lima")));
        if (!esVentaDelDia) {
            comprobanteService.emitirNotaCredito(ventaAnulada, motivo);
        }

        // 6. Notificar por WebSocket
        notificacionService.notificarVentaAnulada(ventaAnulada);

        return ventaAnulada;
    }

    @Transactional(readOnly = true)
    public Venta obtenerPorId(Integer id) {
        return ventaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Venta no encontrada con ID: " + id));
    }

    @Transactional(readOnly = true)
    public List<VentaDetalle> obtenerDetalles(Integer idVenta) {
        return ventaDetalleRepository.findByVentaIdVenta(idVenta);
    }

    @Transactional(readOnly = true)
    public List<VentaPago> obtenerPagos(Integer idVenta) {
        return ventaPagoRepository.findByVentaIdVenta(idVenta);
    }
}

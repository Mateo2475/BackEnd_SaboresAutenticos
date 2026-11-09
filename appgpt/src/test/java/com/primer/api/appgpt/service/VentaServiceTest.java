package com.primer.api.appgpt.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.primer.api.appgpt.entity.Cliente;
import com.primer.api.appgpt.entity.Empleado;
import com.primer.api.appgpt.entity.Producto;
import com.primer.api.appgpt.entity.Venta;
import com.primer.api.appgpt.entity.VentaDetalle;
import com.primer.api.appgpt.entity.VentaPago;
import com.primer.api.appgpt.repository.ProductoRepository;
import com.primer.api.appgpt.repository.VentaDetalleRepository;
import com.primer.api.appgpt.repository.VentaPagoRepository;
import com.primer.api.appgpt.repository.AuditoriaAnulacionRepository;
import com.primer.api.appgpt.repository.VentaRepository;
import com.primer.api.appgpt.simulator.YapePagoResponse;
import com.primer.api.appgpt.simulator.YapeSimulatorService;

/**
 * Pruebas unitarias de VentaService con Mockito:
 * reglas de negocio de venta, simulación de Yape, anulación y notificaciones.
 */
@ExtendWith(MockitoExtension.class)
class VentaServiceTest {

    @Mock private VentaRepository ventaRepository;
    @Mock private VentaDetalleRepository ventaDetalleRepository;
    @Mock private VentaPagoRepository ventaPagoRepository;
    @Mock private ProductoRepository productoRepository;
    @Mock private CuentaCreditoService cuentaCreditoService;
    @Mock private ComprobanteService comprobanteService;
    @Mock private YapeSimulatorService yapeSimulatorService;
    @Mock private NotificacionService notificacionService;
    @Mock private AuditoriaAnulacionRepository auditoriaAnulacionRepository;

    private VentaService ventaService;

    private Producto producto;
    private Empleado cajero;

    @BeforeEach
    void configurar() {
        ventaService = new VentaService();
        ReflectionTestUtils.setField(ventaService, "ventaRepository", ventaRepository);
        ReflectionTestUtils.setField(ventaService, "ventaDetalleRepository", ventaDetalleRepository);
        ReflectionTestUtils.setField(ventaService, "ventaPagoRepository", ventaPagoRepository);
        ReflectionTestUtils.setField(ventaService, "productoRepository", productoRepository);
        ReflectionTestUtils.setField(ventaService, "cuentaCreditoService", cuentaCreditoService);
        ReflectionTestUtils.setField(ventaService, "comprobanteService", comprobanteService);
        ReflectionTestUtils.setField(ventaService, "yapeSimulatorService", yapeSimulatorService);
        ReflectionTestUtils.setField(ventaService, "notificacionService", notificacionService);
        ReflectionTestUtils.setField(ventaService, "auditoriaAnulacionRepository", auditoriaAnulacionRepository);

        producto = new Producto();
        producto.setIdProducto(1);
        producto.setNombre("Ceviche clásico");
        producto.setPrecio(new BigDecimal("25.00"));
        producto.setStock(10);

        cajero = new Empleado();
        cajero.setIdEmpleado(1);
    }

    private VentaDetalle detalle(int cantidad) {
        VentaDetalle item = new VentaDetalle();
        item.setProducto(producto);
        item.setCantidad(cantidad);
        return item;
    }

    private VentaPago pago(String metodo, String monto) {
        VentaPago p = new VentaPago();
        p.setMetodoPago(metodo);
        p.setMonto(new BigDecimal(monto));
        return p;
    }

    private void guardarVentaComoArgumento() {
        when(ventaRepository.save(any(Venta.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    @Test
    void ventaAlContadoDeberiaDescontarStockYNotificar() {
        guardarVentaComoArgumento();
        when(productoRepository.findByIdForUpdate(1)).thenReturn(Optional.of(producto));

        Venta venta = ventaService.procesarVenta(cajero, null, "CONTADO",
                List.of(detalle(2)), List.of(pago("EFECTIVO", "50.00")));

        assertEquals(new BigDecimal("50.00"), venta.getTotal());
        assertEquals("REGISTRADA", venta.getEstado());
        assertEquals(8, producto.getStock()); // 10 - 2

        verify(comprobanteService).generarPorVenta(any(Venta.class));
        verify(notificacionService).notificarNuevaVenta(any(Venta.class));
        verify(ventaPagoRepository).save(any(VentaPago.class));
        verify(ventaDetalleRepository).save(any(VentaDetalle.class));
    }

    @Test
    void stockInsuficienteDeberiaRechazarLaVenta() {
        guardarVentaComoArgumento();
        when(productoRepository.findByIdForUpdate(1)).thenReturn(Optional.of(producto));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> ventaService.procesarVenta(cajero, null, "CONTADO",
                        List.of(detalle(50)), List.of(pago("EFECTIVO", "1250.00"))));

        assertTrue(ex.getMessage().contains("Stock insuficiente"));
        verify(comprobanteService, never()).generarPorVenta(any());
        verify(notificacionService, never()).notificarNuevaVenta(any());
    }

    @Test
    void pagoInsuficienteDeberiaRechazarLaVenta() {
        guardarVentaComoArgumento();
        when(productoRepository.findByIdForUpdate(1)).thenReturn(Optional.of(producto));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> ventaService.procesarVenta(cajero, null, "CONTADO",
                        List.of(detalle(2)), List.of(pago("EFECTIVO", "10.00"))));

        assertTrue(ex.getMessage().contains("no cubre el total"));
        verify(comprobanteService, never()).generarPorVenta(any());
    }

    @Test
    void ventaConYapeAprobadoDeberiaContinuar() {
        guardarVentaComoArgumento();
        when(productoRepository.findByIdForUpdate(1)).thenReturn(Optional.of(producto));
        when(yapeSimulatorService.pagar(any(), any(), any()))
                .thenReturn(new YapePagoResponse(true, "YAPE-ABC", "Pago aprobado por Yape.",
                        new BigDecimal("50.00"), java.time.LocalDateTime.now(java.time.ZoneId.of("America/Lima"))));

        Venta venta = ventaService.procesarVenta(cajero, null, "CONTADO",
                List.of(detalle(2)), List.of(pago("YAPE", "50.00")));

        assertEquals(new BigDecimal("50.00"), venta.getTotal());
        verify(yapeSimulatorService).pagar(any(), any(), any());
        verify(comprobanteService).generarPorVenta(any(Venta.class));
    }

    @Test
    void ventaConYapeRechazadoDeberiaFallar() {
        guardarVentaComoArgumento();
        when(productoRepository.findByIdForUpdate(1)).thenReturn(Optional.of(producto));
        when(yapeSimulatorService.pagar(any(), any(), any()))
                .thenReturn(new YapePagoResponse(false, null, "Yape rechazó el pago.",
                        new BigDecimal("50.00"), java.time.LocalDateTime.now(java.time.ZoneId.of("America/Lima"))));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> ventaService.procesarVenta(cajero, null, "CONTADO",
                        List.of(detalle(2)), List.of(pago("YAPE", "50.00"))));

        assertTrue(ex.getMessage().contains("Yape rechazó"));
        verify(comprobanteService, never()).generarPorVenta(any());
        verify(notificacionService, never()).notificarNuevaVenta(any());
    }

    @Test
    void ventaACreditoSinClienteDeberiaFallar() {
        guardarVentaComoArgumento();
        when(productoRepository.findByIdForUpdate(1)).thenReturn(Optional.of(producto));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> ventaService.procesarVenta(cajero, null, "CREDITO",
                        List.of(detalle(1)), List.of()));

        assertTrue(ex.getMessage().contains("crédito"));
    }

    @Test
    void anularVentaDeberiaReponerStockYNotificar() {
        Venta ventaExistente = new Venta();
        ventaExistente.setIdVenta(7);
        ventaExistente.setEstado("REGISTRADA");
        ventaExistente.setTotal(new BigDecimal("50.00"));
        ventaExistente.setFechaHora(java.time.LocalDateTime.now().minusDays(1));

        VentaDetalle item = detalle(2);
        item.setVenta(ventaExistente);

        when(ventaRepository.findById(7)).thenReturn(Optional.of(ventaExistente));
        when(ventaDetalleRepository.findByVentaIdVenta(7)).thenReturn(List.of(item));
        when(ventaRepository.save(any(Venta.class))).thenAnswer(inv -> inv.getArgument(0));

        producto.setStock(8);
        Venta anulada = ventaService.anularVenta(7, "Error de captura", cajero,true );

        assertEquals("ANULADA", anulada.getEstado());
        assertEquals("Error de captura", anulada.getMotivoAnulacion());
        assertEquals(10, producto.getStock()); // 8 + 2 repuestos

        verify(comprobanteService).emitirNotaCredito(any(Venta.class), any(String.class));
        verify(notificacionService).notificarVentaAnulada(any(Venta.class));
    }

    @Test
    void ventaYaAnuladaNoDeberiaAnularseDosVeces() {
        Venta ventaExistente = new Venta();
        ventaExistente.setIdVenta(8);
        ventaExistente.setEstado("ANULADA");

        when(ventaRepository.findById(8)).thenReturn(Optional.of(ventaExistente));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> ventaService.anularVenta(8, "Duplicado", cajero, true));

        assertTrue(ex.getMessage().contains("ya fue anulada"));
        verify(comprobanteService, never()).emitirNotaCredito(any(), any());
    }

    @Test
    void obtenerPorIdInexistenteDeberiaFallar() {
        when(ventaRepository.findById(99)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> ventaService.obtenerPorId(99));
    }
}

package com.primer.api.appgpt.simulator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Pruebas del simulador de pagos Yape (actor externo).
 */
class YapeSimulatorTest {

    private YapeSimulator yape;

    @BeforeEach
    void configurar() {
        yape = new YapeSimulator();
    }

    @Test
    void pagoValidoDeberiaAprobarse() {
        YapePagoResponse respuesta = yape.pagar(new BigDecimal("150.00"), "987654321", "VENTA-1");

        assertTrue(respuesta.isAprobado());
        assertNotNull(respuesta.getCodigoOperacion());
        assertTrue(respuesta.getCodigoOperacion().startsWith("YAPE-"));
        assertNotNull(respuesta.getFecha());
    }

    @Test
    void montoCeroDeberiaRechazarse() {
        YapePagoResponse respuesta = yape.pagar(BigDecimal.ZERO, "987654321", null);

        assertFalse(respuesta.isAprobado());
        assertEquals(null, respuesta.getCodigoOperacion());
    }

    @Test
    void montoNegativoDeberiaRechazarse() {
        YapePagoResponse respuesta = yape.pagar(new BigDecimal("-10.00"), "987654321", null);

        assertFalse(respuesta.isAprobado());
    }

    @Test
    void montoNuloDeberiaRechazarse() {
        assertFalse(yape.pagar(null, "987654321", null).isAprobado());
    }

    @Test
    void montoSobreLimiteDeberiaRechazarse() {
        YapePagoResponse respuesta = yape.pagar(new BigDecimal("5000.01"), "987654321", null);

        assertFalse(respuesta.isAprobado());
        assertTrue(respuesta.getMensaje().contains("5000"));
    }

    @Test
    void telefonoInvalidoDeberiaRechazarse() {
        assertFalse(yape.pagar(new BigDecimal("50.00"), "12345", null).isAprobado());
        assertFalse(yape.pagar(new BigDecimal("50.00"), null, null).isAprobado());
        assertFalse(yape.pagar(new BigDecimal("50.00"), "9876543210", null).isAprobado());
    }
}

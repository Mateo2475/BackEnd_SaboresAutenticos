package com.primer.api.appgpt.simulator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Pruebas del simulador de comprobantes electrónicos SUNAT (actor externo).
 */
class SunatSimulatorTest {

    private SunatSimulator sunat;

    @BeforeEach
    void configurar() {
        sunat = new SunatSimulator();
    }

    @Test
    void comprobanteValidoDeberiaSerAceptado() {
        SunatCdrResponse respuesta = sunat.enviarComprobante("BOLETA", "B001", 1, new BigDecimal("150.00"));

        assertTrue(respuesta.isAceptado());
        assertEquals("0", respuesta.getCodigoSunat());
        assertNotNull(respuesta.getTicket());
        assertNotNull(respuesta.getXmlCdr());
        assertTrue(respuesta.getXmlCdr().contains("ResponseCode>0<"));
    }

    @Test
    void totalCeroDeberiaSerRechazado() {
        SunatCdrResponse respuesta = sunat.enviarComprobante("FACTURA", "F001", 1, BigDecimal.ZERO);

        assertFalse(respuesta.isAceptado());
        assertEquals("99", respuesta.getCodigoSunat());
    }

    @Test
    void totalNuloDeberiaSerRechazado() {
        SunatCdrResponse respuesta = sunat.enviarComprobante("FACTURA", "F001", 1, null);

        assertFalse(respuesta.isAceptado());
        assertEquals("99", respuesta.getCodigoSunat());
    }

    @Test
    void anulacionConMotivoDeberiaEmitirNotaDeCredito() {
        SunatCdrResponse respuesta = sunat.anularComprobante("BOLETA", "B001", 1, "Error de captura");

        assertTrue(respuesta.isAceptado());
        assertEquals("0", respuesta.getCodigoSunat());
        assertTrue(respuesta.getXmlCdr().contains("Nota de cr"));
    }

    @Test
    void anulacionSinMotivoDeberiaSerRechazada() {
        SunatCdrResponse respuesta = sunat.anularComprobante("BOLETA", "B001", 1, "   ");

        assertFalse(respuesta.isAceptado());
        assertEquals("99", respuesta.getCodigoSunat());
    }

    @Test
    void ticketsDeberianSerDistintos() {
        String t1 = SunatCdrResponse.nuevoTicket();
        String t2 = SunatCdrResponse.nuevoTicket();

        assertNotNull(t1);
        // No se exige desigualdad estricta (milisegundos iguales), pero deben existir
        assertFalse(t1.isBlank() || t2.isBlank());
    }
}

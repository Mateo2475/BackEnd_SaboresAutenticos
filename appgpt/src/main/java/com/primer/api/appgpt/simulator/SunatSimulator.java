package com.primer.api.appgpt.simulator;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.stereotype.Service;

/**
 * Simulador de envío de comprobantes electrónicos a SUNAT.
 *
 * Reglas simuladas:
 * - Total debe ser mayor a 0 (si no, SUNAT responde código 99).
 * - Todo comprobante válido se acepta con código "0".
 * - Se genera un ticket y un XML de CDR simulado.
 */
@Service
public class SunatSimulator implements SunatSimulatorService {

    @Override
    public SunatCdrResponse enviarComprobante(String tipo, String serie, Integer numero,
                                              BigDecimal total) {
        LocalDateTime fecha = LocalDateTime.now(java.time.ZoneId.of("America/Lima"));
        String ticket = SunatCdrResponse.nuevoTicket();

        if (total == null || total.compareTo(BigDecimal.ZERO) <= 0) {
            return new SunatCdrResponse(false, "99", ticket,
                    "SUNAT rechazó el comprobante: el total debe ser mayor a 0.",
                    "<ResponseCode>99</ResponseCode><Description>Total inválido</Description>", fecha);
        }

        String xml = "<ElectronicDocumentResponse>"
                + "<Ticket>" + ticket + "</Ticket>"
                + "<ResponseCode>0</ResponseCode>"
                + "<Description>Comprobante " + tipo + " " + serie + "-" + numero + " aceptado</Description>"
                + "</ElectronicDocumentResponse>";

        return new SunatCdrResponse(true, "0", ticket,
                "Comprobante aceptado por SUNAT.", xml, fecha);
    }

    @Override
    public SunatCdrResponse anularComprobante(String tipo, String serie, Integer numero, String motivo) {
        LocalDateTime fecha = LocalDateTime.now(java.time.ZoneId.of("America/Lima"));
        String ticket = SunatCdrResponse.nuevoTicket();

        if (motivo == null || motivo.isBlank()) {
            return new SunatCdrResponse(false, "99", ticket,
                    "SUNAT rechazó la anulación: falta el motivo.",
                    "<ResponseCode>99</ResponseCode><Description>Motivo requerido</Description>", fecha);
        }

        String xml = "<ElectronicDocumentResponse>"
                + "<Ticket>" + ticket + "</Ticket>"
                + "<ResponseCode>0</ResponseCode>"
                + "<Description>Nota de crédito por anulación de " + tipo + " " + serie + "-" + numero
                + " aceptada</Description>"
                + "</ElectronicDocumentResponse>";

        return new SunatCdrResponse(true, "0", ticket,
                "Anulación aceptada por SUNAT. Se emite Nota de Crédito.", xml, fecha);
    }

    public String generarCodigoInterno() {
        return "SUNAT-" + UUID.randomUUID().toString().substring(0, 12).toUpperCase();
    }
}

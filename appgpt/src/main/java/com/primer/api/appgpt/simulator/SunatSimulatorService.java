package com.primer.api.appgpt.simulator;

import java.time.LocalDateTime;

/**
 * Contrato del actor externo SUNAT. La implementación real enviaría
 * el XML firmado y esperaría el CDR; aquí se simula.
 */
public interface SunatSimulatorService {

    SunatCdrResponse enviarComprobante(String tipo, String serie, Integer numero, java.math.BigDecimal total);

    SunatCdrResponse anularComprobante(String tipo, String serie, Integer numero, String motivo);
}

package com.primer.api.appgpt.simulator;

import java.math.BigDecimal;

/**
 * Contrato del actor externo Yape. La implementación real llamaría
 * a la API de Yape; aquí se simula para desarrollo y pruebas (JUnit).
 */
public interface YapeSimulatorService {

    YapePagoResponse pagar(BigDecimal monto, String telefonoOrigen, String referencia);
}

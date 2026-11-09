package com.primer.api.appgpt.simulator;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

/**
 * Simulador de pagos con Yape.
 *
 * Reglas simuladas:
 * - Monto debe ser mayor a 0.
 * - Monto máximo por operación: S/ 5,000.00
 * - Teléfono de origen debe tener 9 dígitos (empieza con 9).
 */
@Service
public class YapeSimulator implements YapeSimulatorService {

    public static final BigDecimal LIMITE_MONTO = new BigDecimal("5000.00");

    @Override
    public YapePagoResponse pagar(BigDecimal monto, String telefonoOrigen, String referencia) {
        LocalDateTime fecha = LocalDateTime.now(java.time.ZoneId.of("America/Lima"));

        if (monto == null || monto.compareTo(BigDecimal.ZERO) <= 0) {
            return new YapePagoResponse(false, null, "Monto inválido: debe ser mayor a S/ 0.00.",
                    monto, fecha);
        }

        if (monto.compareTo(LIMITE_MONTO) > 0) {
            return new YapePagoResponse(false, null,
                    "Yape rechazó el pago: el monto supera el límite de S/ " + LIMITE_MONTO + " por operación.",
                    monto, fecha);
        }

        if (telefonoOrigen == null || !telefonoOrigen.matches("9\\d{8}")) {
            return new YapePagoResponse(false, null,
                    "Yape rechazó el pago: el teléfono de origen debe tener 9 dígitos y empezar con 9.",
                    monto, fecha);
        }

        return new YapePagoResponse(true, YapePagoResponse.nuevoCodigoOperacion(),
                "Pago aprobado por Yape." + (referencia != null ? " Referencia: " + referencia : ""),
                monto, fecha);
    }
}

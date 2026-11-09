package com.primer.api.appgpt.service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.primer.api.appgpt.dto.response.MfaSolicitudResponse;

/**
 * Simulación de MFA para anulaciones (RF11 / CU-020).
 *
 * El código de 6 dígitos se guarda en memoria durante 5 minutos. En producción
 * se enviaría por SMS/correo o se validaría con Google Authenticator/TOTP.
 * Aquí se devuelve en la respuesta para que el frontend pueda probar el flujo.
 */
@Service
public class MfaService {

    private static final Logger log = LoggerFactory.getLogger(MfaService.class);
    private static final int VIGENCIA_SEGUNDOS = 300;

    private final SecureRandom random = new SecureRandom();
    private final Map<String, CodigoMfa> codigos = new ConcurrentHashMap<>();

    public MfaSolicitudResponse solicitarCodigo(String username) {
        String codigo = String.format("%06d", random.nextInt(1_000_000));
        LocalDateTime expira = LocalDateTime.now(java.time.ZoneId.of("America/Lima")).plusSeconds(VIGENCIA_SEGUNDOS);
        codigos.put(username, new CodigoMfa(codigo, expira));

        // Simulación del envío por SMS/correo: se registra en el log.
        log.info("[MFA-SIMULADO] Código {} generado para el usuario {} (vence {})",
                codigo, username, expira);

        return new MfaSolicitudResponse(codigo, VIGENCIA_SEGUNDOS,
                "Código MFA simulado generado. En producción se enviaría por SMS/correo.");
    }

    public void validarCodigo(String username, String codigo) {
        if (codigo == null || codigo.isBlank()) {
            throw new RuntimeException("El código MFA es obligatorio para anular una venta.");
        }

        CodigoMfa guardado = codigos.get(username);
        if (guardado == null) {
            throw new RuntimeException("No se solicitó un código MFA para este usuario.");
        }
        if (LocalDateTime.now(java.time.ZoneId.of("America/Lima")).isAfter(guardado.expira())) {
            codigos.remove(username);
            throw new RuntimeException("El código MFA expiró. Solicite uno nuevo.");
        }
        if (!guardado.codigo().equals(codigo.trim())) {
            throw new RuntimeException("El código MFA es incorrecto.");
        }

        // Un código solo puede usarse una vez
        codigos.remove(username);
    }

    private record CodigoMfa(String codigo, LocalDateTime expira) {
    }
}

package com.primer.api.appgpt.jwt;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.jsonwebtoken.Claims;

/**
 * Pruebas unitarias del servicio JWT (RS256):
 * generación, extracción de datos y validación de tokens.
 */
class JwtServiceTest {

    private JwtService jwtService;

    @BeforeEach
    void configurar() throws Exception {
        RsaKeyProperties claves = new RsaKeyProperties();
        claves.inicializar(); // genera un par RSA temporal (sin properties)
        jwtService = new JwtService(claves, 3_600_000L, 604_800_000L);
    }

    @Test
    void generarTokenDeberiaContenerUsernameYRoles() {
        String token = jwtService.generarToken("admin", List.of("ADMIN"));

        Claims claims = jwtService.extraerClaims(token);
        assertEquals("admin", claims.getSubject());
        assertEquals("access", claims.get("tipo"));
        assertNotNull(claims.get("roles"));
    }

    @Test
    void esTokenValidoDeberiaAceptarTokenCorrecto() {
        String token = jwtService.generarToken("cajero", List.of("CAJERO"));

        assertTrue(jwtService.esTokenValido(token, "cajero"));
        assertEquals("cajero", jwtService.extraerUsername(token));
    }

    @Test
    void esTokenValidoDeberiaRechazarUsernameDistinto() {
        String token = jwtService.generarToken("admin", List.of("ADMIN"));

        assertFalse(jwtService.esTokenValido(token, "otro-usuario"));
    }

    @Test
    void esTokenValidoDeberiaRechazarRefreshToken() {
        String refresh = jwtService.generarRefreshToken("admin");

        assertTrue(jwtService.esRefreshToken(refresh));
        // El refresh token no debe servir como access token
        assertFalse(jwtService.esTokenValido(refresh, "admin"));
    }

    @Test
    void esTokenValidoDeberiaRechazarTokenManipulado() {
        String token = jwtService.generarToken("admin", List.of("ADMIN"));
        String manipulado = token.substring(0, token.length() - 3) + "abc";

        assertFalse(jwtService.esTokenValido(manipulado, "admin"));
    }

    @Test
    void esTokenValidoDeberiaRechazarTokenBasura() {
        assertFalse(jwtService.esTokenValido("esto-no-es-un-jwt", "admin"));
    }

    @Test
    void expiracionDeberiaSerLaConfigurada() {
        assertEquals(3_600L, jwtService.getExpiracionSegundos());
    }
}

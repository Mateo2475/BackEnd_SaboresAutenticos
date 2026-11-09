package com.primer.api.appgpt.jwt;

import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

/**
 * Servicio de tokens JWT firmados con RSA (RS256).
 * Genera access token y refresh token, extrae datos y valida vigencia.
 */
@Service
public class JwtService {

    private final RsaKeyProperties claves;
    private final long expiracionMs;
    private final long expiracionRefreshMs;

    public JwtService(RsaKeyProperties claves,
                      @Value("${jwt.expiracion-ms:3600000}") long expiracionMs,
                      @Value("${jwt.expiracion-refresh-ms:604800000}") long expiracionRefreshMs) {
        this.claves = claves;
        this.expiracionMs = expiracionMs;
        this.expiracionRefreshMs = expiracionRefreshMs;
    }

    public String generarToken(String username, List<String> roles) {
        Date ahora = new Date();
        return Jwts.builder()
                .setSubject(username)
                .claim("roles", roles)
                .claim("tipo", "access")
                .setIssuedAt(ahora)
                .setExpiration(new Date(ahora.getTime() + expiracionMs))
                .signWith(claves.getPrivateKey(), SignatureAlgorithm.RS256)
                .compact();
    }

    public String generarRefreshToken(String username) {
        Date ahora = new Date();
        return Jwts.builder()
                .setSubject(username)
                .claim("tipo", "refresh")
                .setIssuedAt(ahora)
                .setExpiration(new Date(ahora.getTime() + expiracionRefreshMs))
                .signWith(claves.getPrivateKey(), SignatureAlgorithm.RS256)
                .compact();
    }

    public Claims extraerClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(claves.getPublicKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public String extraerUsername(String token) {
        return extraerClaims(token).getSubject();
    }

    public boolean esRefreshToken(String token) {
        return "refresh".equals(extraerClaims(token).get("tipo"));
    }

    public boolean esTokenValido(String token, String username) {
        try {
            Claims claims = extraerClaims(token);
            return username.equals(claims.getSubject())
                    && "access".equals(claims.get("tipo"))
                    && claims.getExpiration().after(new Date());
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public long getExpiracionSegundos() {
        return expiracionMs / 1000;
    }
}

package com.primer.api.appgpt.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.primer.api.appgpt.entity.Usuario;
import com.primer.api.appgpt.repository.AuthorityRepository;
import com.primer.api.appgpt.repository.UsuarioRepository;

/**
 * Gestión de usuarios (solo ADMIN, protegido en SecurityConfig).
 */
@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    private final UsuarioRepository usuarioRepository;
    private final AuthorityRepository authorityRepository;

    public UsuarioController(UsuarioRepository usuarioRepository, AuthorityRepository authorityRepository) {
        this.usuarioRepository = usuarioRepository;
        this.authorityRepository = authorityRepository;
    }

    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> listar() {
        List<Map<String, Object>> respuesta = usuarioRepository.findAll().stream()
                .map(u -> {
                    var roles = authorityRepository.findByUsername(u.getUsername()).stream()
                            .map(a -> a.getAuthority().replace("ROLE_", ""))
                            .toList();
                    return Map.<String, Object>of(
                            "username", u.getUsername(),
                            "enabled", u.isEnabled(),
                            "intentosFallidos", u.getIntentosFallidos() == null ? 0 : u.getIntentosFallidos(),
                            "bloqueadoHasta", u.getBloqueadoHasta() == null ? "NO" : u.getBloqueadoHasta().toString(),
                            "roles", roles);
                })
                .toList();
        return ResponseEntity.ok(respuesta);
    }

    @PutMapping("/{username}/enabled")
    public ResponseEntity<?> habilitarDeshabilitar(@PathVariable String username,
                                                   @RequestBody Map<String, Boolean> body) {
        try {
            Usuario usuario = usuarioRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado."));
            boolean enabled = Boolean.TRUE.equals(body.get("enabled"));
            usuario.setEnabled(enabled);
            if (enabled) {
                // Al habilitar se limpia el bloqueo por intentos
                usuario.setIntentosFallidos(0);
                usuario.setBloqueadoHasta(null);
            }
            usuarioRepository.save(usuario);
            return ResponseEntity.ok(Map.of("mensaje",
                    enabled ? "Usuario habilitado." : "Usuario deshabilitado."));
        } catch (RuntimeException ex) {
            return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
        }
    }

    @PutMapping("/{username}/desbloquear")
    public ResponseEntity<?> desbloquear(@PathVariable String username) {
        try {
            Usuario usuario = usuarioRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado."));
            usuario.setIntentosFallidos(0);
            usuario.setBloqueadoHasta(null);
            usuarioRepository.save(usuario);
            return ResponseEntity.ok(Map.of("mensaje", "Cuenta desbloqueada."));
        } catch (RuntimeException ex) {
            return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
        }
    }
}

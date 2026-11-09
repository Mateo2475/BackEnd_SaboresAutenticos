package com.primer.api.appgpt.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import com.primer.api.appgpt.dto.request.LoginRequest;
import com.primer.api.appgpt.dto.response.AuthResponse;
import com.primer.api.appgpt.entity.Authority;
import com.primer.api.appgpt.entity.Usuario;
import com.primer.api.appgpt.jwt.JwtService;
import com.primer.api.appgpt.repository.AuthorityRepository;
import com.primer.api.appgpt.repository.EmpleadoRepository;
import com.primer.api.appgpt.repository.UsuarioRepository;

/**
 * Pruebas unitarias de AuthService:
 * login exitoso, bloqueo por intentos fallidos y registro.
 */
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock private UsuarioRepository usuarioRepository;
    @Mock private AuthorityRepository authorityRepository;
    @Mock private EmpleadoRepository empleadoRepository;
    @Mock private AuthenticationManager authenticationManager;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtService jwtService;

    private AuthService authService;

    @BeforeEach
    void configurar() {
        authService = new AuthService(usuarioRepository, authorityRepository,
                empleadoRepository, authenticationManager, passwordEncoder, jwtService);
        ReflectionTestUtils.setField(authService, "intentosMaximos", 5);
        ReflectionTestUtils.setField(authService, "bloqueoMinutos", 15);
    }

    private LoginRequest login(String username, String password) {
        LoginRequest request = new LoginRequest();
        request.setUsername(username);
        request.setPassword(password);
        return request;
    }

    @Test
    void loginExitosoDeberiaDevolverTokenYRoles() {
        Usuario usuario = new Usuario();
        usuario.setUsername("admin");
        usuario.setIntentosFallidos(0);

        Authority authority = new Authority();
        authority.setUsername("admin");
        authority.setAuthority("ROLE_ADMIN");

        // Igual que en producción: DaoAuthenticationProvider devuelve un UserDetails como principal (no un String)
        org.springframework.security.core.userdetails.UserDetails detalles =
                org.springframework.security.core.userdetails.User.withUsername("admin")
                        .password("x").authorities("ROLE_ADMIN").build();
        Authentication autenticacion = new UsernamePasswordAuthenticationToken(detalles, "x");

        when(usuarioRepository.findByUsername("admin")).thenReturn(Optional.of(usuario));
        when(authenticationManager.authenticate(any())).thenReturn(autenticacion);
        when(authorityRepository.findByUsername("admin")).thenReturn(List.of(authority));
        when(jwtService.generarToken(any(), any())).thenReturn("token-access");
        when(jwtService.generarRefreshToken(any())).thenReturn("token-refresh");
        when(jwtService.getExpiracionSegundos()).thenReturn(3600L);

        AuthResponse respuesta = authService.login(login("admin", "admin123"));

        assertEquals("token-access", respuesta.getToken());
        assertEquals("token-refresh", respuesta.getRefreshToken());
        assertEquals("admin", respuesta.getUsername());
        assertEquals(List.of("ADMIN"), respuesta.getRoles());
        assertEquals(3600L, respuesta.getExpiraEnSegundos());

        // Éxito: se reinician los intentos
        assertEquals(0, usuario.getIntentosFallidos());
        assertNull(usuario.getBloqueadoHasta());
        verify(usuarioRepository).save(usuario);
    }

    @Test
    void cuentaBloqueadaDeberiaLanzarLockedExceptionSinAutenticar() {
        Usuario usuario = new Usuario();
        usuario.setUsername("cajero");
        usuario.setBloqueadoHasta(LocalDateTime.now(java.time.ZoneId.of("America/Lima")).plusMinutes(10));

        when(usuarioRepository.findByUsername("cajero")).thenReturn(Optional.of(usuario));

        assertThrows(LockedException.class, () -> authService.login(login("cajero", "cajero123")));

        // No se debe ni intentar autenticar contra la BD
        verify(authenticationManager, never()).authenticate(any());
    }

    @Test
    void quintoIntentoFallidoDeberiaBloquearLaCuenta() {
        Usuario usuario = new Usuario();
        usuario.setUsername("cajero");
        usuario.setIntentosFallidos(4); // ya venía con 4 intentos

        when(usuarioRepository.findByUsername("cajero")).thenReturn(Optional.of(usuario));
        when(authenticationManager.authenticate(any()))
                .thenThrow(new BadCredentialsException("mal"));

        assertThrows(BadCredentialsException.class,
                () -> authService.login(login("cajero", "clave-mala")));

        ArgumentCaptor<Usuario> captor = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarioRepository).save(captor.capture());

        Usuario guardado = captor.getValue();
        assertEquals(5, guardado.getIntentosFallidos());
        assertNotNull(guardado.getBloqueadoHasta());
        assertTrue(guardado.getBloqueadoHasta().isAfter(LocalDateTime.now(java.time.ZoneId.of("America/Lima"))));
    }

    @Test
    void usuarioInexistenteDeberiaLanzarBadCredentials() {
        when(usuarioRepository.findByUsername("fantasma")).thenReturn(Optional.empty());

        assertThrows(BadCredentialsException.class,
                () -> authService.login(login("fantasma", "123")));
    }

    @Test
    void registroConRolInvalidoDeberiaFallar() {
        com.primer.api.appgpt.dto.request.RegisterRequest request =
                new com.primer.api.appgpt.dto.request.RegisterRequest();
        request.setUsername("nuevo");
        request.setPassword("secreta123");
        request.setRol("SUPERVISOR");

        assertThrows(RuntimeException.class, () -> authService.register(request));
        verify(usuarioRepository, never()).save(any());
    }
}

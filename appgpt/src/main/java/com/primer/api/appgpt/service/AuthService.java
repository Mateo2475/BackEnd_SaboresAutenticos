package com.primer.api.appgpt.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.primer.api.appgpt.dto.request.LoginRequest;
import com.primer.api.appgpt.dto.request.RefreshTokenRequest;
import com.primer.api.appgpt.dto.request.RegisterRequest;
import com.primer.api.appgpt.dto.response.AuthResponse;
import com.primer.api.appgpt.entity.Authority;
import com.primer.api.appgpt.entity.Empleado;
import com.primer.api.appgpt.entity.Usuario;
import com.primer.api.appgpt.jwt.JwtService;
import com.primer.api.appgpt.repository.AuthorityRepository;
import com.primer.api.appgpt.repository.EmpleadoRepository;
import com.primer.api.appgpt.repository.UsuarioRepository;

import io.jsonwebtoken.JwtException;

/**
 * Servicio de autenticación:
 * - Login contra MySQL (users/authorities) con BCrypt
 * - Control de intentos fallidos y bloqueo temporal de la cuenta
 * - Registro de usuarios con rol
 * - Refresh token
 */
@Service
public class AuthService {

    private static final String PREFIJO_ROL = "ROLE_";

    private final UsuarioRepository usuarioRepository;
    private final AuthorityRepository authorityRepository;
    private final EmpleadoRepository empleadoRepository;
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Value("${app.seguridad.intentos-maximos:5}")
    private int intentosMaximos;

    @Value("${app.seguridad.bloqueo-minutos:15}")
    private int bloqueoMinutos;

    public AuthService(UsuarioRepository usuarioRepository,
                       AuthorityRepository authorityRepository,
                       EmpleadoRepository empleadoRepository,
                       AuthenticationManager authenticationManager,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService) {
        this.usuarioRepository = usuarioRepository;
        this.authorityRepository = authorityRepository;
        this.empleadoRepository = empleadoRepository;
        this.authenticationManager = authenticationManager;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    // noRollbackFor: si la autenticación falla se lanza una excepción, pero el contador
    // de intentos fallidos y el bloqueo DEBEN guardarse igualmente.
    @Transactional(noRollbackFor = AuthenticationException.class)
    public AuthResponse login(LoginRequest request) {
        Usuario usuario = usuarioRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new BadCredentialsException("Usuario o contraseña incorrectos."));

        // Cuenta bloqueada por intentos fallidos: se rechaza antes de comparar contraseñas
        if (usuario.getBloqueadoHasta() != null && usuario.getBloqueadoHasta().isAfter(LocalDateTime.now(java.time.ZoneId.of("America/Lima")))) {
            throw new LockedException("Cuenta bloqueada hasta " + usuario.getBloqueadoHasta().withNano(0)
                    + " por " + intentosMaximos + " intentos fallidos.");
        }

        try {
            Authentication autenticacion = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));

            // Éxito: se reinician los intentos fallidos y el bloqueo
            usuario.setIntentosFallidos(0);
            usuario.setBloqueadoHasta(null);
            usuarioRepository.save(usuario);

            // getName() devuelve el username; getPrincipal() es un UserDetails, no un String
            return construirRespuesta(autenticacion.getName());

        } catch (AuthenticationException ex) {
            registrarIntentoFallido(usuario);
            throw ex;
        }
    }

    private void registrarIntentoFallido(Usuario usuario) {
        int intentos = (usuario.getIntentosFallidos() == null ? 0 : usuario.getIntentosFallidos()) + 1;
        usuario.setIntentosFallidos(intentos);

        if (intentos >= intentosMaximos) {
            usuario.setBloqueadoHasta(LocalDateTime.now(java.time.ZoneId.of("America/Lima")).plusMinutes(bloqueoMinutos));
        }
        usuarioRepository.save(usuario);
    }

    @Transactional
    public void register(RegisterRequest request) {
        if (usuarioRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("El usuario ya existe: " + request.getUsername());
        }

        String rol = request.getRol().toUpperCase();
        if (!rol.equals("ADMIN") && !rol.equals("CAJERO")
                && !rol.equals("ALMACENERO") && !rol.equals("CONTADOR")) {
            throw new RuntimeException("Rol no válido. Use ADMIN, CAJERO, ALMACENERO o CONTADOR.");
        }

        Usuario usuario = new Usuario();
        usuario.setUsername(request.getUsername());
        usuario.setPassword(passwordEncoder.encode(request.getPassword()));
        usuario.setEnabled(true);
        usuario.setIntentosFallidos(0);
        usuario.setBloqueadoHasta(null);
        usuarioRepository.save(usuario);

        Authority authority = new Authority();
        authority.setUsername(usuario.getUsername());
        authority.setAuthority(PREFIJO_ROL + rol);
        authorityRepository.save(authority);

        // Si se enviaron datos personales, se crea el empleado vinculado
        if (request.getNombres() != null && request.getDni() != null) {
            Empleado empleado = new Empleado();
            empleado.setUsuario(usuario);
            empleado.setNombres(request.getNombres());
            empleado.setApellidos(request.getApellidos());
            empleado.setDni(request.getDni());
            empleado.setCorreo(request.getCorreo());
            empleado.setTelefono(request.getTelefono());
            empleadoRepository.save(empleado);
        }
    }

    @Transactional(readOnly = true)
    public AuthResponse refresh(RefreshTokenRequest request) {
        try {
            String username = jwtService.extraerUsername(request.getRefreshToken());
            if (!jwtService.esRefreshToken(request.getRefreshToken())) {
                throw new JwtException("El token enviado no es un refresh token.");
            }
            if (!usuarioRepository.existsByUsername(username)) {
                throw new JwtException("Usuario inexistente.");
            }
            return construirRespuesta(username);
        } catch (JwtException | IllegalArgumentException ex) {
            throw new RuntimeException("Refresh token inválido o expirado.");
        }
    }

    private AuthResponse construirRespuesta(String username) {
        List<String> roles = authorityRepository.findByUsername(username).stream()
                .map(Authority::getAuthority)
                .map(r -> r.startsWith(PREFIJO_ROL) ? r.substring(PREFIJO_ROL.length()) : r)
                .toList();

        String token = jwtService.generarToken(username, roles);
        String refreshToken = jwtService.generarRefreshToken(username);

        Integer idEmpleado = empleadoRepository.buscarPorUsername(username)
                .map(Empleado::getIdEmpleado)
                .orElse(null);

        return new AuthResponse(token, refreshToken, username, roles, jwtService.getExpiracionSegundos(), idEmpleado);
    }
}

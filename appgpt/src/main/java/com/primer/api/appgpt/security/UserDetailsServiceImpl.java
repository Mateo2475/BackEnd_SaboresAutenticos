package com.primer.api.appgpt.security;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.primer.api.appgpt.entity.Authority;
import com.primer.api.appgpt.entity.Usuario;
import com.primer.api.appgpt.repository.AuthorityRepository;
import com.primer.api.appgpt.repository.UsuarioRepository;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

/**
 * Carga el usuario desde la tabla users y sus roles desde la tabla authorities
 * (ambas de MySQL). Si la cuenta está bloqueada por intentos fallidos,
 * se lanza UsernameNotFoundException con mensaje de bloqueo para que
 * Spring Security no realice la comparación de contraseña.
 */
@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;
    private final AuthorityRepository authorityRepository;

    public UserDetailsServiceImpl(UsuarioRepository usuarioRepository, AuthorityRepository authorityRepository) {
        this.usuarioRepository = usuarioRepository;
        this.authorityRepository = authorityRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + username));

        LocalDateTime ahora = LocalDateTime.now(java.time.ZoneId.of("America/Lima"));
        if (usuario.getBloqueadoHasta() != null && usuario.getBloqueadoHasta().isAfter(ahora)) {
            throw new UsernameNotFoundException(
                    "Cuenta bloqueada hasta " + usuario.getBloqueadoHasta().withNano(0)
                            + " por intentos fallidos.");
        }

        List<Authority> autoridades = authorityRepository.findByUsername(username);
        List<GrantedAuthority> roles = new ArrayList<>();
        for (Authority a : autoridades) {
            roles.add(new SimpleGrantedAuthority(a.getAuthority()));
        }

        return User.withUsername(usuario.getUsername())
                .password(usuario.getPassword())
                .authorities(roles)
                .disabled(!usuario.isEnabled())
                .build();
    }
}

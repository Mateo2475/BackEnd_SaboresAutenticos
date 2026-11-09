package com.primer.api.appgpt.security;

import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

/**
 * Configuración principal de Spring Security:
 * - Stateless (sin sesiones, todo por JWT)
 * - CORS habilitado para el frontend Angular
 * - Reglas de acceso por roles (ADMIN, CAJERO, ALMACENERO)
 * - Filtro JWT agregado antes del filtro de autenticación estándar
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Value("${app.cors.allowed-origins:http://localhost:4200}")
    private String allowedOrigins;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
            .authorizeHttpRequests(auth -> auth
                // ---- PÚBLICO (login/registro y pruebas del profesor) ----
                .requestMatchers("/api/auth/register").hasRole("ADMIN")
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/ws/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/saludo").permitAll()
                .requestMatchers("/actuator/health").permitAll()

                // ---- USUARIOS Y EMPLEADOS: solo administrador ----
                .requestMatchers("/api/usuarios/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.GET, "/api/empleados/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/empleados/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/empleados/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/empleados/**").hasRole("ADMIN")

                // ---- CATÁLOGOS Y ALMACÉN: ADMIN o ALMACENERO ----
                .requestMatchers(HttpMethod.POST, "/api/categorias/**").hasAnyRole("ADMIN", "ALMACENERO")
                .requestMatchers(HttpMethod.PUT, "/api/categorias/**").hasAnyRole("ADMIN", "ALMACENERO")
                .requestMatchers(HttpMethod.DELETE, "/api/categorias/**").hasAnyRole("ADMIN", "ALMACENERO")
                .requestMatchers(HttpMethod.POST, "/api/productos/**").hasAnyRole("ADMIN", "ALMACENERO")
                .requestMatchers(HttpMethod.PUT, "/api/productos/**").hasAnyRole("ADMIN", "ALMACENERO")
                .requestMatchers(HttpMethod.DELETE, "/api/productos/**").hasAnyRole("ADMIN", "ALMACENERO")
                .requestMatchers("/api/guias/**").hasAnyRole("ADMIN", "ALMACENERO")

                // ---- PARÁMETROS DEL SISTEMA: solo ADMIN ----
                .requestMatchers("/api/parametros/**").hasRole("ADMIN")

                // ---- REPORTES, DASHBOARD Y ALERTAS: ADMIN o CONTADOR ----
                .requestMatchers("/api/reportes/**").hasAnyRole("ADMIN", "CONTADOR")
                .requestMatchers("/api/alertas/**").hasAnyRole("ADMIN", "CONTADOR")
                .requestMatchers("/api/dashboard/**").hasAnyRole("ADMIN", "CONTADOR", "CAJERO")

                // ---- CONSULTAS DE SOLO LECTURA: ADMIN, CAJERO o CONTADOR ----
                .requestMatchers(HttpMethod.GET, "/api/ventas/**").hasAnyRole("ADMIN", "CAJERO", "CONTADOR")
                .requestMatchers(HttpMethod.GET, "/api/comprobantes/**").hasAnyRole("ADMIN", "CAJERO", "CONTADOR")
                .requestMatchers(HttpMethod.GET, "/api/creditos/**").hasAnyRole("ADMIN", "CAJERO", "CONTADOR")
                .requestMatchers(HttpMethod.GET, "/api/abonos/**").hasAnyRole("ADMIN", "CAJERO", "CONTADOR")

                // ---- VENTAS, ABONOS Y CRÉDITOS: ADMIN o CAJERO ----
                .requestMatchers(HttpMethod.POST, "/api/ventas/*/anulacion").hasRole("ADMIN")
                .requestMatchers("/api/ventas/**").hasAnyRole("ADMIN", "CAJERO")
                .requestMatchers("/api/abonos/**").hasAnyRole("ADMIN", "CAJERO")
                .requestMatchers("/api/creditos/**").hasAnyRole("ADMIN", "CAJERO")
                .requestMatchers("/api/comprobantes/**").hasAnyRole("ADMIN", "CAJERO")

                // ---- CLIENTES: ADMIN, CAJERO o CONTADOR ----
                .requestMatchers("/api/clientes/**").hasAnyRole("ADMIN", "CAJERO", "CONTADOR")

                // ---- CUALQUIER OTRA COSA REQUIERE SESIÓN ----
                .anyRequest().authenticated()
            )
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint((request, response, authException) -> {
                    response.setStatus(401);
                    response.setContentType("application/json");
                    response.getWriter().write("{\"error\":\"No autenticado. Envíe el token en el encabezado Authorization.\"}");
                })
                .accessDeniedHandler((request, response, accessDeniedException) -> {
                    response.setStatus(403);
                    response.setContentType("application/json");
                    response.getWriter().write("{\"error\":\"No tiene permisos para realizar esta operación.\"}");
                })
            );

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuracion = new CorsConfiguration();
        configuracion.setAllowedOrigins(Arrays.asList(allowedOrigins.split(",")));
        configuracion.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuracion.setAllowedHeaders(List.of("Authorization", "Content-Type", "Accept"));
        configuracion.setExposedHeaders(List.of("Content-Disposition"));
        configuracion.setAllowCredentials(true);
        configuracion.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource fuente = new UrlBasedCorsConfigurationSource();
        fuente.registerCorsConfiguration("/**", configuracion);
        return fuente;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }
}

package com.primer.api.appgpt.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.primer.api.appgpt.dto.EntityMapper;
import com.primer.api.appgpt.dto.request.GuiaRemisionRequest;
import com.primer.api.appgpt.dto.response.GuiaRemisionResponse;
import com.primer.api.appgpt.entity.Empleado;
import com.primer.api.appgpt.service.EmpleadoService;
import com.primer.api.appgpt.service.GuiaRemisionService;
import com.primer.api.appgpt.service.NotificacionService;

import jakarta.validation.Valid;

/**
 * Ingreso de mercadería por Guía de Remisión (rol ALMACENERO/ADMIN).
 * Al registrar la guía se incrementa el stock de los productos.
 */
@RestController
@RequestMapping("/api/guias")
public class GuiaRemisionController {

    private final GuiaRemisionService guiaRemisionService;
    private final EmpleadoService empleadoService;
    private final NotificacionService notificacionService;

    public GuiaRemisionController(GuiaRemisionService guiaRemisionService,
                                  EmpleadoService empleadoService,
                                  NotificacionService notificacionService) {
        this.guiaRemisionService = guiaRemisionService;
        this.empleadoService = empleadoService;
        this.notificacionService = notificacionService;
    }

    @GetMapping
    public ResponseEntity<List<GuiaRemisionResponse>> listar() {
        List<GuiaRemisionResponse> respuesta = guiaRemisionService.listarTodas().stream()
                .map(g -> EntityMapper.toGuiaResponse(g, guiaRemisionService.obtenerDetalles(g.getIdGuia())))
                .toList();
        return ResponseEntity.ok(respuesta);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> obtenerPorId(@PathVariable Integer id) {
        try {
            var guia = guiaRemisionService.obtenerPorId(id);
            return ResponseEntity.ok(EntityMapper.toGuiaResponse(guia, guiaRemisionService.obtenerDetalles(id)));
        } catch (RuntimeException ex) {
            return ResponseEntity.badRequest().body(java.util.Map.of("error", ex.getMessage()));
        }
    }

    @PostMapping
    public ResponseEntity<?> registrarIngreso(@Valid @RequestBody GuiaRemisionRequest request,
                                              Authentication authentication) {
        try {
            Empleado empleado = empleadoService.buscarPorUsername(authentication.getName());
            var guia = guiaRemisionService.registrarIngreso(request, empleado);
            notificacionService.notificarIngresoMercaderia(guia.getIdGuia(), guia.getNumeroGuia());
            return ResponseEntity.ok(EntityMapper.toGuiaResponse(
                    guia, guiaRemisionService.obtenerDetalles(guia.getIdGuia())));
        } catch (RuntimeException ex) {
            return ResponseEntity.badRequest().body(java.util.Map.of("error", ex.getMessage()));
        }
    }
}

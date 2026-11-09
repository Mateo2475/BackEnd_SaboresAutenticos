package com.primer.api.appgpt.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.primer.api.appgpt.service.AlertaDeudaService;

/**
 * Alertas de deuda por SMS/correo (simuladas) - RF28 / CU-030.
 */
@RestController
@RequestMapping("/api/alertas")
public class AlertaController {

    private final AlertaDeudaService alertaDeudaService;

    public AlertaController(AlertaDeudaService alertaDeudaService) {
        this.alertaDeudaService = alertaDeudaService;
    }

    @GetMapping("/deudas")
    public ResponseEntity<List<Map<String, Object>>> previsualizar() {
        return ResponseEntity.ok(alertaDeudaService.previsualizar());
    }

    @PostMapping("/deudas/enviar")
    public ResponseEntity<List<Map<String, Object>>> enviar() {
        return ResponseEntity.ok(alertaDeudaService.enviarAlertas());
    }

    @PostMapping("/deudas/enviar-async")
    public ResponseEntity<?> enviarAsync() {
        alertaDeudaService.enviarAlertasAsync();
        return ResponseEntity.accepted().body(Map.of("mensaje", "Alertas encoladas para envío en segundo plano."));
    }

    @PostMapping("/deudas/enviar/{idCliente}")
    public ResponseEntity<List<Map<String, Object>>> enviarIndividual(@PathVariable Integer idCliente) {
        return ResponseEntity.ok(alertaDeudaService.enviarAlertaIndividual(idCliente));
    }
}

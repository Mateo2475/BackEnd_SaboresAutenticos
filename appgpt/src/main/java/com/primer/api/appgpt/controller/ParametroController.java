package com.primer.api.appgpt.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.primer.api.appgpt.entity.ParametroSistema;
import com.primer.api.appgpt.service.ParametroService;

@RestController
@RequestMapping("/api/parametros")
public class ParametroController {

    private final ParametroService parametroService;

    public ParametroController(ParametroService parametroService) {
        this.parametroService = parametroService;
    }

    @GetMapping
    public ResponseEntity<List<ParametroSistema>> listar() {
        return ResponseEntity.ok(parametroService.listar());
    }

    @PutMapping
    public ResponseEntity<?> actualizar(@RequestParam String clave, @RequestParam String valor) {
        try {
            return ResponseEntity.ok(parametroService.actualizarValor(clave, valor));
        } catch (RuntimeException ex) {
            return ResponseEntity.badRequest().body(java.util.Map.of("error", ex.getMessage()));
        }
    }

    @PutMapping("/objeto")
    public ResponseEntity<?> guardar(@RequestBody ParametroSistema parametro) {
        try {
            return ResponseEntity.ok(parametroService.guardar(parametro));
        } catch (RuntimeException ex) {
            return ResponseEntity.badRequest().body(java.util.Map.of("error", ex.getMessage()));
        }
    }
}
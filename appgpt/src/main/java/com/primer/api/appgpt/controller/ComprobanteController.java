package com.primer.api.appgpt.controller;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import java.util.Map;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.primer.api.appgpt.dto.EntityMapper;
import com.primer.api.appgpt.entity.Comprobante;
import com.primer.api.appgpt.dto.response.ComprobanteResponse;
import com.primer.api.appgpt.repository.ComprobanteRepository;
import com.primer.api.appgpt.service.ComprobanteService;

@RestController
@RequestMapping("/api/comprobantes")
public class ComprobanteController {

    private final ComprobanteService comprobanteService;
    private final ComprobanteRepository comprobanteRepository;

    public ComprobanteController(ComprobanteService comprobanteService, ComprobanteRepository comprobanteRepository) {
        this.comprobanteService = comprobanteService;
        this.comprobanteRepository = comprobanteRepository;
    }

    /**
     * Historial de comprobantes con filtro de 30 días por defecto (RF22).
     * Ejemplo: /api/comprobantes?desde=2026-01-01&hasta=2026-01-31
     */
    @GetMapping
    public ResponseEntity<?> listar(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {

        LocalDateTime inicio = desde != null ? desde.atStartOfDay() : null;
        LocalDateTime fin = hasta != null ? hasta.atTime(23, 59, 59) : null;

        if (page != null || size != null) {
            int p = page == null ? 0 : page;
            int s = size == null ? 20 : size;
            Pageable pageable = PageRequest.of(p, s);
            Page<Comprobante> comprobantes;
            if (inicio != null && fin != null) {
                comprobantes = comprobanteRepository.findByFechaEmisionBetweenOrderByFechaEmisionDesc(inicio, fin, pageable);
            } else if (inicio != null) {
                comprobantes = comprobanteRepository.findByFechaEmisionAfterOrderByFechaEmisionDesc(inicio, pageable);
            } else if (fin != null) {
                comprobantes = comprobanteRepository.findByFechaEmisionBetweenOrderByFechaEmisionDesc(
                        LocalDateTime.of(2000, 1, 1, 0, 0), fin, pageable);
            } else {
                comprobantes = comprobanteRepository.findAll(pageable);
            }
            return ResponseEntity.ok(comprobantes.map(EntityMapper::toComprobanteResponse));
        }

        List<ComprobanteResponse> respuesta = comprobanteService.listarPorRango(inicio, fin).stream()
                .map(EntityMapper::toComprobanteResponse)
                .toList();
        return ResponseEntity.ok(respuesta);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> obtenerPorId(@PathVariable Integer id) {
        try {
            return ResponseEntity.ok(EntityMapper.toComprobanteResponse(comprobanteService.obtenerPorId(id)));
        } catch (RuntimeException ex) {
            return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
        }
    }

    @GetMapping("/venta/{idVenta}")
    public ResponseEntity<List<ComprobanteResponse>> porVenta(@PathVariable Integer idVenta) {
        List<ComprobanteResponse> respuesta = comprobanteService.listarPorVenta(idVenta).stream()
                .map(EntityMapper::toComprobanteResponse)
                .toList();
        return ResponseEntity.ok(respuesta);
    }

    @GetMapping("/{id}/pdf")
    public ResponseEntity<byte[]> pdf(@PathVariable Integer id) {
        byte[] contenido = comprobanteService.generarPdfComprobante(id);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"comprobante_" + id + ".pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(contenido);
    }

    @GetMapping("/{id}/reimprimir")
    public ResponseEntity<byte[]> reimprimir(@PathVariable Integer id) {
        return pdf(id);
    }

    @GetMapping("/{id}/xml")
    public ResponseEntity<String> xml(@PathVariable Integer id) {
        return ResponseEntity.ok(comprobanteService.generarXmlComprobante(id));
    }
}

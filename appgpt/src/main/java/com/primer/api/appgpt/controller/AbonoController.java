package com.primer.api.appgpt.controller;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.primer.api.appgpt.dto.EntityMapper;
import com.primer.api.appgpt.dto.request.AbonoCrearRequest;
import com.primer.api.appgpt.dto.response.AbonoAplicadoResponse;
import com.primer.api.appgpt.dto.response.AbonoResponse;
import com.primer.api.appgpt.entity.Empleado;
import com.primer.api.appgpt.repository.AbonoRepository;
import com.primer.api.appgpt.repository.EmpleadoRepository;
import com.primer.api.appgpt.service.AbonoService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/abonos")
public class AbonoController {

    private final AbonoService abonoService;
    private final AbonoRepository abonoRepository;
    private final EmpleadoRepository empleadoRepository;

    public AbonoController(AbonoService abonoService,
                           AbonoRepository abonoRepository,
                           EmpleadoRepository empleadoRepository) {
        this.abonoService = abonoService;
        this.abonoRepository = abonoRepository;
        this.empleadoRepository = empleadoRepository;
    }

    @GetMapping
    public ResponseEntity<?> listarAbonos(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        if (page != null || size != null) {
            int p = page == null ? 0 : page;
            int s = size == null ? 20 : size;
            org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(p, s);
            org.springframework.data.domain.Page<com.primer.api.appgpt.entity.Abono> abonos = abonoRepository.findAll(pageable);
            return ResponseEntity.ok(abonos.map(EntityMapper::toAbonoResponse));
        }
        List<AbonoResponse> respuesta = abonoRepository.findAll().stream()
                .map(EntityMapper::toAbonoResponse)
                .toList();
        return ResponseEntity.ok(respuesta);
    }

    @GetMapping("/historial-cajero/{idEmpleado}")
    public ResponseEntity<List<Map<String, Object>>> obtenerHistorialPorCajero(
            @PathVariable Integer idEmpleado,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
        LocalDateTime ini = desde != null ? desde.atStartOfDay() : null;
        LocalDateTime fin = hasta != null ? hasta.atTime(23, 59, 59) : null;

        List<Object[]> filas = abonoRepository.obtenerHistorialCobrosPorCajeroYFechas(idEmpleado, ini, fin);
        List<Map<String, Object>> resultado = new ArrayList<>();

        for (Object[] f : filas) {
            Map<String, Object> item = new HashMap<>();
            item.put("idEmpleado", f[0]);
            item.put("cajero", f[1]);
            item.put("fechaOperacion", f[2]);
            item.put("tipoOperacion", f[3]);
            item.put("metodoPago", f[4]);
            item.put("monto", f[5]);
            resultado.add(item);
        }
        return ResponseEntity.ok(resultado);
    }

    @GetMapping("/historial-cajero/{idEmpleado}/pdf")
    public ResponseEntity<byte[]> historialCajeroPdf(
            @PathVariable Integer idEmpleado,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
        List<Map<String, Object>> datos = obtenerHistorialPorCajero(idEmpleado, desde, hasta).getBody();
        List<String> lineas = new ArrayList<>();
        for (Map<String, Object> d : datos) {
            lineas.add(d.get("fechaOperacion") + "  " + d.get("tipoOperacion") + "  "
                    + d.get("metodoPago") + "  S/ " + d.get("monto"));
        }
        byte[] pdf = com.primer.api.appgpt.util.PdfUtil.generar("Historial por cajero", lineas);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"historial_cajero.pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    @PostMapping
    public ResponseEntity<?> registrarAbono(@Valid @RequestBody AbonoCrearRequest request,
                                            Authentication authentication) {
        try {
            Empleado cajero = empleadoRepository.buscarPorUsername(authentication.getName())
                    .orElseThrow(() -> new RuntimeException(
                            "No existe un empleado vinculado al usuario: " + authentication.getName()));

            AbonoAplicadoResponse resultado = abonoService.registrarAbono(
                    request.getIdCliente(),
                    cajero,
                    request.getMonto(),
                    request.getMetodoPago()
            );
            return ResponseEntity.ok(resultado);
        } catch (RuntimeException ex) {
            return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
        }
    }
}

package com.primer.api.appgpt.controller;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.primer.api.appgpt.repository.ComprobanteRepository;
import com.primer.api.appgpt.repository.VentaRepository;
import com.primer.api.appgpt.service.CuentaCreditoService;

/**
 * Dashboard gerencial (RF25 / CU-028): deuda total, deudores y ventas del día.
 */
@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final CuentaCreditoService cuentaCreditoService;
    private final VentaRepository ventaRepository;
    private final ComprobanteRepository comprobanteRepository;

    public DashboardController(CuentaCreditoService cuentaCreditoService,
                               VentaRepository ventaRepository,
                               ComprobanteRepository comprobanteRepository) {
        this.cuentaCreditoService = cuentaCreditoService;
        this.ventaRepository = ventaRepository;
        this.comprobanteRepository = comprobanteRepository;
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> resumen() {
        LocalDateTime inicioHoy = LocalDate.now(java.time.ZoneId.of("America/Lima")).atStartOfDay();
        LocalDateTime finHoy = LocalDate.now(java.time.ZoneId.of("America/Lima")).atTime(23, 59, 59);

        Map<String, Object> respuesta = new LinkedHashMap<>();
        respuesta.put("totalPorCobrar", cuentaCreditoService.totalDeudaGlobal());
        respuesta.put("clientesConDeuda", cuentaCreditoService.contarClientesConDeuda());
        respuesta.put("ventasHoy", ventaRepository.totalVentasEntre(inicioHoy, finHoy));
        respuesta.put("cantidadVentasHoy", ventaRepository.contarVentasEntre(inicioHoy, finHoy));
        respuesta.put("comprobantesHoy", comprobanteRepository
                .findByFechaEmisionBetweenOrderByFechaEmisionDesc(inicioHoy, finHoy).size());
        respuesta.put("totalPorCobrarFormateado", "S/ " + cuentaCreditoService.totalDeudaGlobal());

        List<Map<String, Object>> topDeudores = new ArrayList<>();
        List<Object[]> filas = cuentaCreditoService.listarTopDeudoresAgrupado();
        int limite = Math.min(10, filas.size());
        for (int i = 0; i < limite; i++) {
            Object[] f = filas.get(i);
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("idCliente", f[0]);
            item.put("nombres", f[1]);
            item.put("apellidos", f[2]);
            item.put("dni", f[3]);
            item.put("deudaPendiente", f[4]);
            topDeudores.add(item);
        }
        respuesta.put("topDeudores", topDeudores);

        return ResponseEntity.ok(respuesta);
    }
}

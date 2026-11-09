package com.primer.api.appgpt.controller;

import java.time.LocalDate;
import java.util.List;
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

import com.primer.api.appgpt.service.ReporteService;

/**
 * Reportes y exportaciones (RF14, RF15, RF23).
 * Acceso: ADMIN y CONTADOR.
 */
@RestController
@RequestMapping("/api/reportes")
public class ReporteController {

    private final ReporteService reporteService;

    public ReporteController(ReporteService reporteService) {
        this.reporteService = reporteService;
    }

    @GetMapping("/ventas")
    public ResponseEntity<Map<String, Object>> ventas(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
        return ResponseEntity.ok(reporteService.ventasPorRango(desde, hasta));
    }

    @GetMapping("/ventas/excel")
    public ResponseEntity<byte[]> ventasExcel(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
        byte[] contenido = reporteService.exportarVentasXlsx(desde, hasta);
        return descarga(contenido, reporteService.nombreArchivo("ventas", desde, hasta, "xlsx"),
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
    }

    @GetMapping("/ventas/pdf")
    public ResponseEntity<byte[]> ventasPdf(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
        byte[] contenido = reporteService.exportarVentasPdf(desde, hasta);
        return descarga(contenido, reporteService.nombreArchivo("ventas", desde, hasta, "pdf"),
                MediaType.APPLICATION_PDF_VALUE);
    }

    @GetMapping("/creditos")
    public ResponseEntity<List<Map<String, Object>>> creditos() {
        return ResponseEntity.ok(reporteService.creditosPendientes());
    }

    @GetMapping("/creditos/excel")
    public ResponseEntity<byte[]> creditosExcel() {
        byte[] contenido = reporteService.exportarCreditosXlsx();
        return descarga(contenido, "creditos_pendientes.xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
    }

    @GetMapping("/creditos/pdf")
    public ResponseEntity<byte[]> creditosPdf() {
        byte[] contenido = reporteService.exportarCreditosPdf();
        return descarga(contenido, "creditos_pendientes.pdf", MediaType.APPLICATION_PDF_VALUE);
    }

    @GetMapping("/productos")
    public ResponseEntity<List<Map<String, Object>>> productos() {
        return ResponseEntity.ok(reporteService.productosMasVendidos());
    }

    @GetMapping("/productos/excel")
    public ResponseEntity<byte[]> productosExcel() {
        byte[] contenido = reporteService.exportarProductosXlsx();
        return descarga(contenido, "productos_mas_vendidos.xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
    }

    @GetMapping("/productos/pdf")
    public ResponseEntity<byte[]> productosPdf() {
        byte[] contenido = reporteService.exportarProductosPdf();
        return descarga(contenido, "productos_mas_vendidos.pdf", MediaType.APPLICATION_PDF_VALUE);
    }

    @GetMapping("/cliente/{idCliente}/pdf")
    public ResponseEntity<byte[]> clientePdf(@PathVariable Integer idCliente) {
        byte[] contenido = reporteService.exportarClientePdf(idCliente);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"reporte_cliente_" + idCliente + ".pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(contenido);
    }

    private ResponseEntity<byte[]> descarga(byte[] contenido, String nombreArchivo, String contentType) {
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + nombreArchivo + "\"")
                .contentType(MediaType.parseMediaType(contentType))
                .body(contenido);
    }
}

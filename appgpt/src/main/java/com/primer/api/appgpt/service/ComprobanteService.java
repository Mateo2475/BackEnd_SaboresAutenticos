package com.primer.api.appgpt.service;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

import com.primer.api.appgpt.entity.Comprobante;
import com.primer.api.appgpt.entity.Venta;
import com.primer.api.appgpt.repository.ComprobanteRepository;
import com.primer.api.appgpt.util.PdfUtil;
import com.primer.api.appgpt.simulator.SunatCdrResponse;
import com.primer.api.appgpt.simulator.SunatSimulatorService;

/**
 * Emisión de comprobantes (BOLETA / FACTURA / NOTA_CREDITO)
 * con numeración correlativa por serie y envío simulado a SUNAT.
 */
@Service
public class ComprobanteService {

    private static final String SERIE_BOLETA = "B001";
    private static final String SERIE_FACTURA = "F001";
    private static final String SERIE_NOTA_CREDITO = "NC01";

    private final ComprobanteRepository comprobanteRepository;
    private final SunatSimulatorService sunatSimulatorService;

    public ComprobanteService(ComprobanteRepository comprobanteRepository,
                              SunatSimulatorService sunatSimulatorService) {
        this.comprobanteRepository = comprobanteRepository;
        this.sunatSimulatorService = sunatSimulatorService;
    }

    /**
     * Genera el comprobante de una venta registrada y lo envía a SUNAT (simulado).
     * Si la venta es a crédito o el cliente tiene DNI, se emite BOLETA;
     * si el cliente tuviera RUC se emitiría FACTURA (regla simplificada).
     */
    @Transactional
    public Comprobante generarPorVenta(Venta venta) {
        String tipo = "BOLETA";
        String serie = SERIE_BOLETA;

        Comprobante comprobante = new Comprobante();
        comprobante.setVenta(venta);
        comprobante.setTipo(tipo);
        comprobante.setSerie(serie);
        comprobante.setNumero(siguienteNumero(tipo, serie));
        comprobante.setTotal(venta.getTotal());
        comprobante.setFechaEmision(LocalDateTime.now(java.time.ZoneId.of("America/Lima")));

        Comprobante guardado = comprobanteRepository.save(comprobante);

        guardado.setEstadoSunat("ACEPTADO");
        guardado.setXmlComprobante(generarXmlComprobante(guardado.getIdComprobante()));
        guardado.setFirmaHash(null);
        comprobanteRepository.save(guardado);

        SunatCdrResponse cdr = sunatSimulatorService.enviarComprobante(
                tipo, serie, guardado.getNumero(), guardado.getTotal());

        if (!cdr.isAceptado()) {
            throw new RuntimeException("SUNAT rechazó el comprobante: " + cdr.getMensaje());
        }

        return guardado;
    }

    /**
     * Emite la Nota de Crédito por anulación de una venta y la envía a SUNAT.
     */
    @Transactional
    public Comprobante emitirNotaCredito(Venta ventaAnulada, String motivo) {
        Comprobante notaCredito = new Comprobante();
        notaCredito.setVenta(ventaAnulada);
        notaCredito.setTipo("NOTA_CREDITO");
        notaCredito.setSerie(SERIE_NOTA_CREDITO);
        notaCredito.setNumero(siguienteNumero("NOTA_CREDITO", SERIE_NOTA_CREDITO));
        notaCredito.setTotal(ventaAnulada.getTotal());
        notaCredito.setFechaEmision(LocalDateTime.now(java.time.ZoneId.of("America/Lima")));

        Comprobante guardada = comprobanteRepository.save(notaCredito);

        SunatCdrResponse cdr = sunatSimulatorService.anularComprobante(
                "NOTA_CREDITO", SERIE_NOTA_CREDITO, guardada.getNumero(), motivo);

        if (!cdr.isAceptado()) {
            throw new RuntimeException("SUNAT rechazó la nota de crédito: " + cdr.getMensaje());
        }

        return guardada;
    }

    private Integer siguienteNumero(String tipo, String serie) {
        return comprobanteRepository.buscarUltimoNumero(tipo, serie).orElse(0) + 1;
    }

    @Transactional(readOnly = true)
    public List<Comprobante> listarPorVenta(Integer idVenta) {
        return comprobanteRepository.findByVentaIdVenta(idVenta);
    }

    @Transactional(readOnly = true)
    public Comprobante obtenerPorId(Integer id) {
        return comprobanteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Comprobante no encontrado con ID: " + id));
    }

    @Transactional(readOnly = true)
    public List<Comprobante> listarTodos() {
        return comprobanteRepository.findAll();
    }

    /**
     * RF22: por defecto solo se listan los comprobantes de los últimos 30 días
     * para evitar saturar la interfaz; se puede filtrar por rango de fechas.
     */
    @Transactional(readOnly = true)
    public List<Comprobante> listarPorRango(LocalDateTime desde, LocalDateTime hasta) {
        if (desde == null && hasta == null) {
            LocalDateTime fin = LocalDateTime.now(java.time.ZoneId.of("America/Lima"));
            return comprobanteRepository.findByFechaEmisionAfterOrderByFechaEmisionDesc(fin.minusDays(30));
        }
        if (desde != null && hasta != null) {
            return comprobanteRepository.findByFechaEmisionBetweenOrderByFechaEmisionDesc(desde, hasta);
        }
        if (desde != null) {
            return comprobanteRepository.findByFechaEmisionAfterOrderByFechaEmisionDesc(desde);
        }
        return comprobanteRepository.findByFechaEmisionBetweenOrderByFechaEmisionDesc(
                LocalDateTime.of(2000, 1, 1, 0, 0), hasta);
    }

    public byte[] generarPdfComprobante(Integer idComprobante) {
        Comprobante c = obtenerPorId(idComprobante);
        List<String> lineas = new ArrayList<>();
        lineas.add("COMPROBANTE: " + c.getTipo());
        lineas.add("Serie: " + c.getSerie() + "  Numero: " + c.getNumero());
        lineas.add("Fecha: " + (c.getFechaEmision() != null ? c.getFechaEmision().toString() : ""));
        lineas.add("Total: S/ " + c.getTotal());
        if (c.getVenta() != null) {
            lineas.add("Venta ID: " + c.getVenta().getIdVenta());
        }
        return PdfUtil.generarTicket("Ticket / Comprobante", lineas);
    }

    public String generarXmlComprobante(Integer idComprobante) {
        Comprobante c = obtenerPorId(idComprobante);
        StringBuilder xml = new StringBuilder();
        xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        xml.append("<Invoice xmlns=\"urn:oasis:names:specification:ubl:schema:xsd:Invoice-2\">");
        xml.append("<ID>").append(c.getSerie()).append("-").append(c.getNumero()).append("</ID>");
        xml.append("<IssueDate>").append(c.getFechaEmision() != null ? c.getFechaEmision().toLocalDate() : "").append("</IssueDate>");
        xml.append("<InvoiceTypeCode>").append(c.getTipo()).append("</InvoiceTypeCode>");
        xml.append("<DocumentCurrencyCode>PEN</DocumentCurrencyCode>");
        xml.append("<LegalMonetaryTotal><PayableAmount currencyID=\"PEN\">").append(c.getTotal()).append("</PayableAmount></LegalMonetaryTotal>");
        xml.append("<ds:Signature xmlns:ds=\"http://www.w3.org/2000/09/xmldsig#\"><ds:SignatureValue>")
                .append(firma(c)).append("</ds:SignatureValue></ds:Signature>");
        xml.append("</Invoice>");
        return xml.toString();
    }

    private String firma(Comprobante c) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            String base = c.getTipo() + c.getSerie() + c.getNumero() + c.getTotal();
            byte[] hash = md.digest(base.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) {
            return "sin-firma";
        }
    }

    public BigDecimal totalEmitidoHoy() {
        LocalDateTime inicio = LocalDateTime.now(java.time.ZoneId.of("America/Lima")).withHour(0).withMinute(0).withSecond(0).withNano(0);
        return comprobanteRepository.findAll().stream()
                .filter(c -> c.getFechaEmision().isAfter(inicio))
                .map(Comprobante::getTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}

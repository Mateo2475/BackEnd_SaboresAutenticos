package com.primer.api.appgpt.service;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.primer.api.appgpt.entity.Cliente;
import com.primer.api.appgpt.entity.Venta;
import com.primer.api.appgpt.repository.ClienteRepository;
import com.primer.api.appgpt.repository.CuentaCreditoRepository;
import com.primer.api.appgpt.repository.ProductoRepository;
import com.primer.api.appgpt.repository.VentaRepository;
import com.primer.api.appgpt.util.PdfUtil;
import com.primer.api.appgpt.util.XlsxUtil;

/**
 * Reportes y exportaciones (RF14, RF15, RF23, RF25).
 * La exportación "Excel" se entrega como CSV compatible con Excel.
 */
@Service
public class ReporteService {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final DateTimeFormatter FMT_DIA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final VentaRepository ventaRepository;
    private final CuentaCreditoRepository cuentaCreditoRepository;
    private final ProductoRepository productoRepository;
    private final ClienteRepository clienteRepository;

    public ReporteService(VentaRepository ventaRepository,
                          CuentaCreditoRepository cuentaCreditoRepository,
                          ProductoRepository productoRepository) {
        this(ventaRepository, cuentaCreditoRepository, productoRepository, null);
    }

    @org.springframework.beans.factory.annotation.Autowired
    public ReporteService(VentaRepository ventaRepository,
                          CuentaCreditoRepository cuentaCreditoRepository,
                          ProductoRepository productoRepository,
                          ClienteRepository clienteRepository) {
        this.ventaRepository = ventaRepository;
        this.cuentaCreditoRepository = cuentaCreditoRepository;
        this.productoRepository = productoRepository;
        this.clienteRepository = clienteRepository;
    }

    private LocalDateTime inicio(LocalDate desde) {
        LocalDate d = desde != null ? desde : LocalDate.now(java.time.ZoneId.of("America/Lima")).minusDays(30);
        return d.atStartOfDay();
    }

    private LocalDateTime fin(LocalDate hasta) {
        LocalDate d = hasta != null ? hasta : LocalDate.now(java.time.ZoneId.of("America/Lima"));
        return d.atTime(23, 59, 59);
    }

    @Transactional(readOnly = true)
    public Map<String, Object> ventasPorRango(LocalDate desde, LocalDate hasta) {
        LocalDateTime ini = inicio(desde);
        LocalDateTime fin = fin(hasta);

        Map<String, Object> respuesta = new LinkedHashMap<>();
        respuesta.put("desde", ini.toLocalDate());
        respuesta.put("hasta", fin.toLocalDate());
        respuesta.put("cantidadVentas", ventaRepository.contarVentasEntre(ini, fin));
        respuesta.put("totalVendido", ventaRepository.totalVentasEntre(ini, fin));

        List<Map<String, Object>> porTipoPago = new ArrayList<>();
        for (Object[] fila : ventaRepository.resumenPorTipoPago(ini, fin)) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("tipoPago", fila[0]);
            item.put("cantidad", fila[1]);
            item.put("total", fila[2]);
            porTipoPago.add(item);
        }
        respuesta.put("porTipoPago", porTipoPago);

        List<Map<String, Object>> porDia = new ArrayList<>();
        for (Object[] fila : ventaRepository.resumenPorDia(ini, fin)) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("dia", fila[0]);
            item.put("cantidad", fila[1]);
            item.put("total", fila[2]);
            porDia.add(item);
        }
        respuesta.put("porDia", porDia);

        return respuesta;
    }

    @Transactional(readOnly = true)
    public List<Venta> listarVentas(LocalDate desde, LocalDate hasta) {
        return ventaRepository.findByFechaHoraBetweenAndEstadoOrderByFechaHoraDesc(
                inicio(desde), fin(hasta), "REGISTRADA");
    }

    public byte[] exportarVentasXlsx(LocalDate desde, LocalDate hasta) {
        List<String> enc = List.of("ID Venta", "Fecha", "Tipo Pago", "Cliente", "Cajero", "Total");
        List<List<String>> filas = new ArrayList<>();
        for (Venta v : listarVentas(desde, hasta)) {
            filas.add(List.of(
                    String.valueOf(v.getIdVenta()),
                    v.getFechaHora() != null ? v.getFechaHora().format(FMT) : "",
                    v.getTipoPago(),
                    v.getCliente() != null ? v.getCliente().getNombres() + " " + v.getCliente().getApellidos() : "Varios",
                    v.getEmpleado() != null ? v.getEmpleado().getNombres() + " " + v.getEmpleado().getApellidos() : "",
                    String.valueOf(v.getTotal())));
        }
        return XlsxUtil.generar("Ventas", enc, filas);
    }

    public byte[] exportarVentasCsv(LocalDate desde, LocalDate hasta) {
        StringBuilder csv = new StringBuilder("\uFEFF");
        csv.append("ID Venta;Fecha;Tipo Pago;Cliente;Cajero;Total\n");
        for (Venta v : listarVentas(desde, hasta)) {
            csv.append(csv(String.valueOf(v.getIdVenta()))).append(';')
                    .append(csv(v.getFechaHora() != null ? v.getFechaHora().format(FMT) : "")).append(';')
                    .append(csv(v.getTipoPago())).append(';')
                    .append(csv(v.getCliente() != null
                            ? v.getCliente().getNombres() + " " + v.getCliente().getApellidos() : "Varios")).append(';')
                    .append(csv(v.getEmpleado() != null
                            ? v.getEmpleado().getNombres() + " " + v.getEmpleado().getApellidos() : "")).append(';')
                    .append(csv(String.valueOf(v.getTotal()))).append('\n');
        }
        return csv.toString().getBytes(StandardCharsets.UTF_8);
    }

    public byte[] exportarVentasPdf(LocalDate desde, LocalDate hasta) {
        List<String> lineas = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;
        for (Venta v : listarVentas(desde, hasta)) {
            lineas.add("#" + v.getIdVenta()
                    + "  " + (v.getFechaHora() != null ? v.getFechaHora().format(FMT) : "")
                    + "  " + v.getTipoPago()
                    + "  " + (v.getCliente() != null ? v.getCliente().getApellidos() : "Varios")
                    + "  S/ " + v.getTotal());
            total = total.add(v.getTotal());
        }
        lineas.add(" ");
        lineas.add("TOTAL: S/ " + total);
        return PdfUtil.generar("Reporte de ventas " + inicio(desde).toLocalDate()
                + " al " + fin(hasta).toLocalDate(), lineas);
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> creditosPendientes() {
        List<Map<String, Object>> resultado = new ArrayList<>();
        for (Object[] f : cuentaCreditoRepository.listarDeudoresDashboard()) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("idCliente", f[0]);
            item.put("nombres", f[1]);
            item.put("apellidos", f[2]);
            item.put("dni", f[3]);
            item.put("year", f[4]);
            item.put("month", f[5]);
            item.put("limiteCredito", f[6]);
            item.put("montoConsumido", f[7]);
            item.put("montoAbonado", f[8]);
            item.put("deudaPendiente", f[9]);
            item.put("estadoDeuda", f[10]);
            BigDecimal limite = f[6] != null ? new BigDecimal(f[6].toString()) : BigDecimal.ZERO;
            BigDecimal consumido = f[7] != null ? new BigDecimal(f[7].toString()) : BigDecimal.ZERO;
            item.put("saldoDisponible", limite.subtract(consumido));
            resultado.add(item);
        }
        return resultado;
    }

    public byte[] exportarCreditosCsv() {
        StringBuilder csv = new StringBuilder("\uFEFF");
        csv.append("Cliente;DNI;Año;Mes;Límite;Consumido;Abonado;Deuda\n");
        for (Map<String, Object> c : creditosPendientes()) {
            csv.append(c.get("nombres")).append(' ').append(c.get("apellidos")).append(';')
                    .append(c.get("dni")).append(';')
                    .append(c.get("year")).append(';')
                    .append(c.get("month")).append(';')
                    .append(c.get("limiteCredito")).append(';')
                    .append(c.get("montoConsumido")).append(';')
                    .append(c.get("montoAbonado")).append(';')
                    .append(c.get("deudaPendiente")).append('\n');
        }
        return csv.toString().getBytes(StandardCharsets.UTF_8);
    }

    public byte[] exportarCreditosPdf() {
        List<String> lineas = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;
        for (Map<String, Object> c : creditosPendientes()) {
            lineas.add(c.get("apellidos") + ", " + c.get("nombres")
                    + "  DNI " + c.get("dni")
                    + "  Deuda S/ " + c.get("deudaPendiente"));
            total = total.add(new BigDecimal(c.get("deudaPendiente").toString()));
        }
        lineas.add(" ");
        lineas.add("TOTAL POR COBRAR: S/ " + total);
        return PdfUtil.generar("Reporte de créditos pendientes", lineas);
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> productosMasVendidos() {
        List<Map<String, Object>> resultado = new ArrayList<>();
        for (Object[] f : productoRepository.obtenerRankingDeVentas()) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("idProducto", f[0]);
            item.put("nombre", f[1]);
            item.put("categoria", f[2]);
            item.put("totalUnidadesVendidas", f[3]);
            item.put("ingresoTotalGenerado", f[4]);
            resultado.add(item);
        }
        return resultado;
    }

    public byte[] exportarProductosCsv() {
        StringBuilder csv = new StringBuilder("\uFEFF");
        csv.append("Producto;Categoría;Unidades vendidas;Ingreso total\n");
        for (Map<String, Object> p : productosMasVendidos()) {
            csv.append(p.get("nombre")).append(';')
                    .append(p.get("categoria")).append(';')
                    .append(p.get("totalUnidadesVendidas")).append(';')
                    .append(p.get("ingresoTotalGenerado")).append('\n');
        }
        return csv.toString().getBytes(StandardCharsets.UTF_8);
    }

    public byte[] exportarProductosPdf() {
        List<String> lineas = new ArrayList<>();
        for (Map<String, Object> p : productosMasVendidos()) {
            lineas.add(p.get("nombre") + "  [" + p.get("categoria") + "]"
                    + "  Unidades: " + p.get("totalUnidadesVendidas")
                    + "  Ingreso: S/ " + p.get("ingresoTotalGenerado"));
        }
        return PdfUtil.generar("Productos más vendidos", lineas);
    }

    @Transactional(readOnly = true)
    public byte[] exportarClientePdf(Integer idCliente) {
        List<Venta> ventas = ventaRepository.findByCliente_IdCliente(idCliente);
        List<String> lineas = new ArrayList<>();
        String nombreCliente = "Cliente #" + idCliente;
        if (clienteRepository != null) {
            clienteRepository.findById(idCliente).ifPresent(c ->
                    lineas.add("Cliente: " + c.getNombres() + " " + c.getApellidos() + "  DNI " + c.getDni()));
        }
        BigDecimal total = BigDecimal.ZERO;
        for (Venta v : ventas) {
            lineas.add("#" + v.getIdVenta() + "  " + (v.getFechaHora() != null ? v.getFechaHora().format(FMT) : "")
                    + "  " + v.getTipoPago() + "  S/ " + v.getTotal());
            total = total.add(v.getTotal());
        }
        lineas.add(" ");
        lineas.add("TOTAL CONSUMIDO: S/ " + total);
        lineas.add(" ");
        lineas.add(">>> MARCA DE AGUA: DOCUMENTO DE USO INTERNO - COPIA <<<");
        return PdfUtil.generar("Reporte de consumo por cliente " + nombreCliente, lineas);
    }

    public byte[] exportarCreditosXlsx() {
        List<String> enc = List.of("Cliente", "DNI", "Año", "Mes", "Límite", "Consumido", "Abonado", "Deuda");
        List<List<String>> filas = new ArrayList<>();
        for (Map<String, Object> c : creditosPendientes()) {
            filas.add(List.of(
                    c.get("nombres") + " " + c.get("apellidos"),
                    String.valueOf(c.get("dni")),
                    String.valueOf(c.get("year")),
                    String.valueOf(c.get("month")),
                    String.valueOf(c.get("limiteCredito")),
                    String.valueOf(c.get("montoConsumido")),
                    String.valueOf(c.get("montoAbonado")),
                    String.valueOf(c.get("deudaPendiente"))));
        }
        return XlsxUtil.generar("Creditos", enc, filas);
    }

    public byte[] exportarProductosXlsx() {
        List<String> enc = List.of("Producto", "Categoría", "Unidades vendidas", "Ingreso total");
        List<List<String>> filas = new ArrayList<>();
        for (Map<String, Object> p : productosMasVendidos()) {
            filas.add(List.of(
                    String.valueOf(p.get("nombre")),
                    String.valueOf(p.get("categoria")),
                    String.valueOf(p.get("totalUnidadesVendidas")),
                    String.valueOf(p.get("ingresoTotalGenerado"))));
        }
        return XlsxUtil.generar("Productos", enc, filas);
    }

    private static String csv(String valor) {
        if (valor == null) return "";
        String v = valor.replace("\"", "\"\"");
        if (v.contains(";") || v.contains("\"") || v.contains("\n") || v.contains(",")) {
            return "\"" + v + "\"";
        }
        return v;
    }

    public String nombreArchivo(String prefijo, LocalDate desde, LocalDate hasta, String extension) {
        LocalDate d = desde != null ? desde : LocalDate.now(java.time.ZoneId.of("America/Lima")).minusDays(30);
        LocalDate h = hasta != null ? hasta : LocalDate.now(java.time.ZoneId.of("America/Lima"));
        return prefijo + "_" + d.format(FMT_DIA).replace('/', '-')
                + "_" + h.format(FMT_DIA).replace('/', '-') + "." + extension;
    }
}

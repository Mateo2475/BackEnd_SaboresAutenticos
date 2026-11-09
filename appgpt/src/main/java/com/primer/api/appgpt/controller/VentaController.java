package com.primer.api.appgpt.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
import com.primer.api.appgpt.dto.request.AnulacionRequest;
import com.primer.api.appgpt.dto.request.VentaCrearRequest;
import com.primer.api.appgpt.dto.response.VentaResponse;
import com.primer.api.appgpt.entity.Cliente;
import com.primer.api.appgpt.entity.Empleado;
import com.primer.api.appgpt.entity.Producto;
import com.primer.api.appgpt.entity.Venta;
import com.primer.api.appgpt.entity.VentaDetalle;
import com.primer.api.appgpt.entity.VentaPago;
import com.primer.api.appgpt.repository.ClienteRepository;
import com.primer.api.appgpt.repository.EmpleadoRepository;
import com.primer.api.appgpt.repository.VentaRepository;
import com.primer.api.appgpt.service.ComprobanteService;
import com.primer.api.appgpt.service.MfaService;
import com.primer.api.appgpt.service.VentaService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/ventas")
public class VentaController {

    private final VentaService ventaService;
    private final VentaRepository ventaRepository;
    private final EmpleadoRepository empleadoRepository;
    private final ClienteRepository clienteRepository;
    private final ComprobanteService comprobanteService;
    private final MfaService mfaService;

    public VentaController(VentaService ventaService,
                           VentaRepository ventaRepository,
                           EmpleadoRepository empleadoRepository,
                           ClienteRepository clienteRepository,
                           ComprobanteService comprobanteService,
                           MfaService mfaService) {
        this.ventaService = ventaService;
        this.ventaRepository = ventaRepository;
        this.empleadoRepository = empleadoRepository;
        this.clienteRepository = clienteRepository;
        this.comprobanteService = comprobanteService;
        this.mfaService = mfaService;
    }

    @GetMapping
    public ResponseEntity<?> listarVentas(
            @RequestParam(required = false) Integer clienteId,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        if (page != null || size != null || clienteId != null) {
            int p = page == null ? 0 : page;
            int s = size == null ? 20 : size;
            Pageable pageable = PageRequest.of(p, s, Sort.by("fechaHora").descending());
            Page<Venta> ventas = clienteId != null
                    ? ventaRepository.findByCliente_IdCliente(clienteId, pageable)
                    : ventaRepository.findAll(pageable);
            return ResponseEntity.ok(ventas.map(this::construirRespuesta));
        }
        List<VentaResponse> respuesta = new ArrayList<>();
        for (Venta v : ventaRepository.findAll()) {
            respuesta.add(construirRespuesta(v));
        }
        return ResponseEntity.ok(respuesta);
    }

    @GetMapping("/cliente/{idCliente}")
    public ResponseEntity<Page<VentaResponse>> listarPorCliente(
            @PathVariable Integer idCliente,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("fechaHora").descending());
        Page<Venta> ventas = ventaRepository.findByCliente_IdCliente(idCliente, pageable);
        return ResponseEntity.ok(ventas.map(this::construirRespuesta));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> obtenerPorId(@PathVariable Integer id) {
        try {
            return ResponseEntity.ok(construirRespuesta(ventaService.obtenerPorId(id)));
        } catch (RuntimeException ex) {
            return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
        }
    }

    @GetMapping("/reporte-mensual")
    public ResponseEntity<List<Map<String, Object>>> obtenerReporteMensual() {
        List<Object[]> filas = ventaRepository.obtenerReporteVentasMensual();
        List<Map<String, Object>> resultado = new ArrayList<>();

        for (Object[] f : filas) {
            Map<String, Object> item = new HashMap<>();
            item.put("anio", f[0]);
            item.put("mes", f[1]);
            item.put("tipoPago", f[2]);
            item.put("cantidadVentas", f[3]);
            item.put("totalRecaudado", f[4]);
            resultado.add(item);
        }
        return ResponseEntity.ok(resultado);
    }

    @PostMapping
    public ResponseEntity<?> registrarVenta(@Valid @RequestBody VentaCrearRequest request,
                                            Authentication authentication) {
        try {
            // El cajero es siempre el usuario autenticado (no se confía en el body)
            Empleado cajero = empleadoRepository.buscarPorUsername(authentication.getName())
                    .orElseThrow(() -> new RuntimeException(
                            "No existe un empleado vinculado al usuario: " + authentication.getName()));

            Cliente cliente = null;
            if (request.getIdCliente() != null) {
                cliente = clienteRepository.findById(request.getIdCliente())
                        .orElseThrow(() -> new RuntimeException("Cliente no encontrado."));
            }

            List<VentaDetalle> carrito = new ArrayList<>();
            if (request.getItems() != null) {
                for (VentaCrearRequest.ItemCarritoRequest itemReq : request.getItems()) {
                    VentaDetalle detalle = new VentaDetalle();
                    Producto prod = new Producto();
                    prod.setIdProducto(itemReq.getIdProducto());
                    detalle.setProducto(prod);
                    detalle.setCantidad(itemReq.getCantidad());
                    carrito.add(detalle);
                }
            }

            List<VentaPago> pagos = new ArrayList<>();
            if (request.getPagos() != null) {
                for (VentaCrearRequest.PagoRequest pagoReq : request.getPagos()) {
                    VentaPago vp = new VentaPago();
                    vp.setMetodoPago(pagoReq.getMetodoPago());
                    vp.setMonto(pagoReq.getMonto());
                    pagos.add(vp);
                }
            }

            Venta ventaProcesada = ventaService.procesarVenta(
                    cajero, cliente, request.getTipoPago(), carrito, pagos);
            return ResponseEntity.ok(construirRespuesta(ventaProcesada));
        } catch (RuntimeException ex) {
            return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
        }
    }

    /**
     * Simulación de MFA para anulaciones (RF11 / CU-020).
     * Devuelve el código que en producción llegaría por SMS/correo.
     */
    @PostMapping("/mfa/solicitar")
    public ResponseEntity<?> solicitarMfa(Authentication authentication) {
        try {
            return ResponseEntity.ok(mfaService.solicitarCodigo(authentication.getName()));
        } catch (RuntimeException ex) {
            return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
        }
    }

    /**
     * Anulación de venta: repone stock, revierte crédito y emite Nota de Crédito (SUNAT simulado).
     * El anulador se toma del token JWT (username autenticado).
     */
    @PostMapping("/{id}/anulacion")
    public ResponseEntity<?> anularVenta(@PathVariable Integer id,
                                         @Valid @RequestBody AnulacionRequest request,
                                         Authentication authentication) {
        try {
            String username = authentication.getName(); // siempre el usuario del token

            // RF11 / CU-020: validación MFA simulada antes de anular.
            mfaService.validarCodigo(username, request.getCodigoMfa());

            Empleado empleadoAnula = empleadoRepository.buscarPorUsername(username)
                    .orElseThrow(() -> new RuntimeException(
                            "No existe un empleado vinculado al usuario: " + username));

            Venta anulada = ventaService.anularVenta(id, request.getMotivo(), empleadoAnula, true);
            return ResponseEntity.ok(construirRespuesta(anulada));
        } catch (RuntimeException ex) {
            return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
        }
    }

    private VentaResponse construirRespuesta(Venta v) {
        var comprobante = comprobanteService.listarPorVenta(v.getIdVenta()).stream()
                .filter(c -> !"NOTA_CREDITO".equals(c.getTipo()))
                .findFirst()
                .orElse(null);
        return EntityMapper.toVentaResponse(
                v,
                ventaService.obtenerDetalles(v.getIdVenta()),
                ventaService.obtenerPagos(v.getIdVenta()),
                comprobante);
    }
}

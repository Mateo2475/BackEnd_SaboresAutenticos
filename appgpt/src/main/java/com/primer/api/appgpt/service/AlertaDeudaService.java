package com.primer.api.appgpt.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.primer.api.appgpt.entity.HistorialAlerta;
import com.primer.api.appgpt.repository.ClienteRepository;
import com.primer.api.appgpt.repository.CuentaCreditoRepository;
import com.primer.api.appgpt.repository.HistorialAlertaRepository;

/**
 * Simulación de alertas de deuda por SMS/correo (RF28 / CU-030).
 *
 * No se integra un proveedor real: se simula el envío con reintentos y se
 * registra el resultado en el log. El frontend recibe el detalle de cada envío.
 */
@Service
public class AlertaDeudaService {

    private static final Logger log = LoggerFactory.getLogger(AlertaDeudaService.class);
    private static final int MAX_REINTENTOS = 3;

    private final CuentaCreditoRepository cuentaCreditoRepository;
    private final ClienteRepository clienteRepository;
    private final NotificacionService notificacionService;
    private final HistorialAlertaRepository historialAlertaRepository;

    public AlertaDeudaService(CuentaCreditoRepository cuentaCreditoRepository,
                              ClienteRepository clienteRepository,
                              NotificacionService notificacionService) {
        this(cuentaCreditoRepository, clienteRepository, notificacionService, null);
    }

    @org.springframework.beans.factory.annotation.Autowired
    public AlertaDeudaService(CuentaCreditoRepository cuentaCreditoRepository,
                              ClienteRepository clienteRepository,
                              NotificacionService notificacionService,
                              HistorialAlertaRepository historialAlertaRepository) {
        this.cuentaCreditoRepository = cuentaCreditoRepository;
        this.clienteRepository = clienteRepository;
        this.notificacionService = notificacionService;
        this.historialAlertaRepository = historialAlertaRepository;
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> previsualizar() {
        List<Map<String, Object>> resultado = new ArrayList<>();
        for (Object[] f : cuentaCreditoRepository.listarDeudoresDashboard()) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("idCliente", f[0]);
            item.put("cliente", f[1] + " " + f[2]);
            item.put("dni", f[3]);
            item.put("deudaPendiente", f[9]);
            resultado.add(item);
        }
        return resultado;
    }

    /**
     * Simula el envío de la alerta por SMS y correo a cada cliente con deuda.
     * Reintenta hasta 3 veces (RF28) y devuelve el detalle de cada envío.
     */
    public List<Map<String, Object>> enviarAlertas() {
        List<Map<String, Object>> resultado = new ArrayList<>();

        for (Object[] f : cuentaCreditoRepository.listarDeudoresDashboard()) {
            Integer idCliente = ((Number) f[0]).intValue();
            String cliente = f[1] + " " + f[2];
            String deuda = f[9].toString();

            var clienteBD = clienteRepository.findById(idCliente).orElse(null);
            String telefono = clienteBD != null ? clienteBD.getTelefono() : null;
            String correo = clienteBD != null ? clienteBD.getCorreo() : null;

            Map<String, Object> sms = simularEnvio(cliente, "SMS", telefono,
                    "Estimado(a) " + cliente + ", su deuda pendiente es S/ " + deuda
                            + ". Por favor regularice su pago. Sabores Auténticos.");
            Map<String, Object> correoR = simularEnvio(cliente, "CORREO", correo,
                    "Estado de cuenta: deuda pendiente S/ " + deuda + ".");
            resultado.add(sms);
            resultado.add(correoR);

            if (historialAlertaRepository != null && clienteBD != null) {
                HistorialAlerta h1 = new HistorialAlerta();
                h1.setCliente(clienteBD);
                h1.setTipoAlerta("DEUDA");
                h1.setCanal("SMS");
                h1.setEstado(String.valueOf(sms.get("estado")));
                h1.setIntentos(((Number) sms.get("intentos")).intValue());
                h1.setMensaje("Alerta SMS. Deuda S/ " + deuda);
                historialAlertaRepository.save(h1);

                HistorialAlerta h2 = new HistorialAlerta();
                h2.setCliente(clienteBD);
                h2.setTipoAlerta("DEUDA");
                h2.setCanal("CORREO");
                h2.setEstado(String.valueOf(correoR.get("estado")));
                h2.setIntentos(((Number) correoR.get("intentos")).intValue());
                h2.setMensaje("Alerta correo. Deuda S/ " + deuda);
                historialAlertaRepository.save(h2);
            }
        }

        notificacionService.notificarAlertasDeuda(resultado.size());
        return resultado;
    }

    @Async
    public void enviarAlertasAsync() {
        enviarAlertas();
    }

    public List<Map<String, Object>> enviarAlertaIndividual(Integer idCliente) {
        List<Map<String, Object>> resultado = new ArrayList<>();
        Object[] fila = null;
        for (Object[] f : cuentaCreditoRepository.listarDeudoresDashboard()) {
            if (((Number) f[0]).intValue() == idCliente) {
                fila = f;
                break;
            }
        }
        if (fila == null) {
            throw new RuntimeException("El cliente no tiene deuda pendiente.");
        }
        String cliente = fila[1] + " " + fila[2];
        String deuda = fila[9].toString();
        var clienteBD = clienteRepository.findById(idCliente).orElse(null);
        String telefono = clienteBD != null ? clienteBD.getTelefono() : null;
        String correo = clienteBD != null ? clienteBD.getCorreo() : null;
        resultado.add(simularEnvio(cliente, "SMS", telefono,
                "Estimado(a) " + cliente + ", su deuda pendiente es S/ " + deuda));
        resultado.add(simularEnvio(cliente, "CORREO", correo,
                "Estado de cuenta: deuda pendiente S/ " + deuda + "."));
        if (historialAlertaRepository != null && clienteBD != null) {
            HistorialAlerta h1 = new HistorialAlerta();
            h1.setCliente(clienteBD);
            h1.setTipoAlerta("DEUDA_INDIVIDUAL");
            h1.setCanal("SMS");
            h1.setEstado("ENVIADO");
            h1.setIntentos(1);
            h1.setMensaje("Alerta individual SMS. Deuda S/ " + deuda);
            historialAlertaRepository.save(h1);

            HistorialAlerta h2 = new HistorialAlerta();
            h2.setCliente(clienteBD);
            h2.setTipoAlerta("DEUDA_INDIVIDUAL");
            h2.setCanal("CORREO");
            h2.setEstado("ENVIADO");
            h2.setIntentos(1);
            h2.setMensaje("Alerta individual correo. Deuda S/ " + deuda);
            historialAlertaRepository.save(h2);
        }
        return resultado;
    }

    private Map<String, Object> simularEnvio(String cliente, String canal, String destino, String mensaje) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("cliente", cliente);
        item.put("canal", canal);
        item.put("destino", destino != null && !destino.isBlank() ? destino : "NO REGISTRADO");
        item.put("mensaje", mensaje);

        boolean enviado = false;
        int intento = 0;
        while (!enviado && intento < MAX_REINTENTOS) {
            intento++;
            try {
                if (destino == null || destino.isBlank()) {
                    throw new IllegalStateException("El cliente no tiene " + canal.toLowerCase() + " registrado.");
                }
                // Simulación de la llamada al proveedor externo
                log.info("[{} SIMULADO] Para {} ({}) -> {}", canal, cliente, destino, mensaje);
                enviado = true;
            } catch (Exception ex) {
                log.warn("[{} SIMULADO] Intento {} fallido para {}: {}", canal, intento, cliente, ex.getMessage());
                try {
                    Thread.sleep(200L * intento);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                }
            }
        }

        item.put("intentos", intento);
        item.put("estado", enviado ? "ENVIADO" : "FALLIDO");
        item.put("fechaHora", LocalDateTime.now(java.time.ZoneId.of("America/Lima")));
        return item;
    }
}

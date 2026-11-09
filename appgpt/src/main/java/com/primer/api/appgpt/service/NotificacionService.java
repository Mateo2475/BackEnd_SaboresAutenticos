package com.primer.api.appgpt.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import com.primer.api.appgpt.entity.Venta;

/**
 * Envía notificaciones en tiempo real a los clientes Angular
 * conectados por WebSocket (STOMP) a través de /topic/*.
 */
@Service
public class NotificacionService {

    private static final Logger log = LoggerFactory.getLogger(NotificacionService.class);

    private final SimpMessagingTemplate messagingTemplate;

    public NotificacionService(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    public void notificarNuevaVenta(Venta venta) {
        Map<String, Object> evento = new HashMap<>();
        evento.put("tipo", "VENTA_REGISTRADA");
        evento.put("idVenta", venta.getIdVenta());
        evento.put("total", venta.getTotal());
        evento.put("tipoPago", venta.getTipoPago());
        evento.put("fecha", venta.getFechaHora());
        evento.put("mensaje", "Nueva venta #" + venta.getIdVenta() + " registrada por S/ " + venta.getTotal());
        enviar("/topic/ventas", evento);
        log.info("WebSocket: venta #{} notificada", venta.getIdVenta());
    }

    public void notificarVentaAnulada(Venta venta) {
        Map<String, Object> evento = new HashMap<>();
        evento.put("tipo", "VENTA_ANULADA");
        evento.put("idVenta", venta.getIdVenta());
        evento.put("motivo", venta.getMotivoAnulacion());
        evento.put("fecha", venta.getFechaAnulacion());
        evento.put("mensaje", "La venta #" + venta.getIdVenta() + " fue anulada.");
        enviar("/topic/ventas", evento);
        log.info("WebSocket: anulación de venta #{} notificada", venta.getIdVenta());
    }

    public void notificarAlertasInventario(List<Map<String, Object>> alertas) {
        Map<String, Object> evento = new HashMap<>();
        evento.put("tipo", "ALERTAS_INVENTARIO");
        evento.put("cantidad", alertas.size());
        evento.put("alertas", alertas);
        evento.put("mensaje", "Hay " + alertas.size() + " producto(s) con stock bajo o por vencer.");
        enviar("/topic/inventario", evento);
        log.info("WebSocket: {} alertas de inventario notificadas", alertas.size());
    }

    public void notificarAlertasDeuda(int cantidadEnvios) {
        Map<String, Object> evento = new HashMap<>();
        evento.put("tipo", "ALERTAS_DEUDA");
        evento.put("cantidad", cantidadEnvios);
        evento.put("mensaje", "Se procesaron " + cantidadEnvios + " alertas de deuda (SMS/correo simulado).");
        enviar("/topic/alertas", evento);
    }

    public void notificarIngresoMercaderia(Integer idGuia, String numeroGuia) {
        Map<String, Object> evento = new HashMap<>();
        evento.put("tipo", "INGRESO_MERCADERIA");
        evento.put("idGuia", idGuia);
        evento.put("numeroGuia", numeroGuia);
        evento.put("mensaje", "Ingreso de mercadería registrado: guía " + numeroGuia);
        enviar("/topic/inventario", evento);
    }

    private void enviar(String topico, Object payload) {
        try {
            messagingTemplate.convertAndSend(topico, payload);
        } catch (Exception ex) {
            // Nunca debe romper el flujo principal por fallo de notificación
            log.warn("No se pudo notificar por WebSocket: {}", ex.getMessage());
        }
    }
}

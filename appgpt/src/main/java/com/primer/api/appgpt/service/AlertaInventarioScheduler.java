package com.primer.api.appgpt.service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.primer.api.appgpt.repository.ProductoRepository;

/**
 * Job programado que consulta la vista V_AlertasInventario y envía
 * por WebSocket (/topic/inventario) los productos con stock bajo,
 * sin stock, vencidos o próximos a vencer.
 *
 * El cron se configura con la propiedad app.alertas.cron
 * (por defecto: todos los días a las 08:00).
 */
@Component
public class AlertaInventarioScheduler {

    private static final Logger log = LoggerFactory.getLogger(AlertaInventarioScheduler.class);

    private final ProductoRepository productoRepository;
    private final NotificacionService notificacionService;

    public AlertaInventarioScheduler(ProductoRepository productoRepository,
                                     NotificacionService notificacionService) {
        this.productoRepository = productoRepository;
        this.notificacionService = notificacionService;
    }

    /**
     * Ejecuta la revisión automática de inventario según el cron configurado
     * en la propiedad app.alertas.cron.
     */
    @Scheduled(cron = "${app.alertas.cron:0 0 0 * * *}")
    public void revisarInventario() {
        try {
            List<Object[]> filas = productoRepository.obtenerAlertasInventario();
            if (filas.isEmpty()) {
                log.debug("Alertas de inventario: sin novedades.");
                return;
            }

            List<Map<String, Object>> alertas = new ArrayList<>();
            for (Object[] fila : filas) {
                Map<String, Object> alerta = new LinkedHashMap<>();
                alerta.put("idProducto", fila[0]);
                alerta.put("nombre", fila[1]);
                alerta.put("categoria", fila[2]);
                alerta.put("stock", fila[3]);
                alerta.put("stockMinimo", fila[4]);
                alerta.put("fechaCaducidad", fila[5]);
                alerta.put("estadoStock", fila[6]);
                alerta.put("estadoCaducidad", fila[7]);
                alertas.add(alerta);
            }

            notificacionService.notificarAlertasInventario(alertas);
        } catch (Exception ex) {
            // La vista puede no existir todavía en la BD; no debe tumbar el scheduler.
            log.warn("No se pudieron consultar las alertas de inventario: {}", ex.getMessage());
        }
    }
}

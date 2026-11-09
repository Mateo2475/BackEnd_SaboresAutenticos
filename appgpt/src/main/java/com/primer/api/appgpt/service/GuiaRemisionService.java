package com.primer.api.appgpt.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.primer.api.appgpt.dto.request.GuiaRemisionRequest;
import com.primer.api.appgpt.entity.Empleado;
import com.primer.api.appgpt.entity.GuiaRemision;
import com.primer.api.appgpt.entity.GuiaRemisionDetalle;
import com.primer.api.appgpt.entity.Producto;
import com.primer.api.appgpt.repository.GuiaRemisionDetalleRepository;
import com.primer.api.appgpt.repository.GuiaRemisionRepository;
import com.primer.api.appgpt.repository.ProductoRepository;

/**
 * Ingreso de mercadería por Guía de Remisión:
 * registra la guía con sus detalles y AUMENTA el stock de los productos.
 */
@Service
public class GuiaRemisionService {

    private final GuiaRemisionRepository guiaRemisionRepository;
    private final GuiaRemisionDetalleRepository guiaRemisionDetalleRepository;
    private final ProductoRepository productoRepository;

    public GuiaRemisionService(GuiaRemisionRepository guiaRemisionRepository,
                               GuiaRemisionDetalleRepository guiaRemisionDetalleRepository,
                               ProductoRepository productoRepository) {
        this.guiaRemisionRepository = guiaRemisionRepository;
        this.guiaRemisionDetalleRepository = guiaRemisionDetalleRepository;
        this.productoRepository = productoRepository;
    }

    @Transactional
    public GuiaRemision registrarIngreso(GuiaRemisionRequest request, Empleado empleadoResponsable) {
        if (request.getDetalles() == null || request.getDetalles().isEmpty()) {
            throw new RuntimeException("La guía de remisión debe tener al menos un producto.");
        }

        GuiaRemision guia = new GuiaRemision();
        guia.setNumeroGuia(request.getNumeroGuia());
        guia.setProveedor(request.getProveedor());
        guia.setEmpleado(empleadoResponsable);
        guia.setFechaIngreso(LocalDateTime.now(java.time.ZoneId.of("America/Lima")));
        GuiaRemision guiaGuardada = guiaRemisionRepository.save(guia);

        for (GuiaRemisionRequest.DetalleRequest detalleReq : request.getDetalles()) {
            if (detalleReq.getCantidad() == null || detalleReq.getCantidad() <= 0) {
                throw new RuntimeException("La cantidad de cada producto debe ser mayor a 0.");
            }

            Producto producto = productoRepository.findById(detalleReq.getIdProducto())
                    .orElseThrow(() -> new RuntimeException(
                            "Producto no encontrado con ID: " + detalleReq.getIdProducto()));

            GuiaRemisionDetalle detalle = new GuiaRemisionDetalle();
            detalle.setGuiaRemision(guiaGuardada);
            detalle.setProducto(producto);
            detalle.setCantidad(detalleReq.getCantidad());
            guiaRemisionDetalleRepository.save(detalle);

            // Ingreso de mercadería: se incrementa el stock
            producto.setStock(producto.getStock() + detalleReq.getCantidad());
            productoRepository.save(producto);
        }

        return guiaGuardada;
    }

    @Transactional(readOnly = true)
    public List<GuiaRemision> listarTodas() {
        return guiaRemisionRepository.findAllByOrderByFechaIngresoDesc();
    }

    @Transactional(readOnly = true)
    public GuiaRemision obtenerPorId(Integer id) {
        return guiaRemisionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Guía de remisión no encontrada con ID: " + id));
    }

    @Transactional(readOnly = true)
    public List<GuiaRemisionDetalle> obtenerDetalles(Integer idGuia) {
        return guiaRemisionDetalleRepository.findByGuiaRemisionIdGuia(idGuia);
    }
}

package com.primer.api.appgpt.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.primer.api.appgpt.dto.response.ProductoResponse;
import com.primer.api.appgpt.entity.Producto;
import com.primer.api.appgpt.repository.ProductoRepository;
import com.primer.api.appgpt.service.ProductoService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/productos")
public class ProductoController {

    @Autowired
    private ProductoService productoService;

    @Autowired
    private ProductoRepository productoRepository;

    @GetMapping
    public ResponseEntity<?> listarProductos(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        if (page != null || size != null) {
            int p = page == null ? 0 : page;
            int s = size == null ? 20 : size;
            Pageable pageable = PageRequest.of(p, s);
            Page<Producto> productos = productoRepository.findByActivoTrue(pageable);
            return ResponseEntity.ok(productos.map(this::mapearAResponse));
        }
        List<ProductoResponse> respuesta = productoService.listarProductosActivos().stream()
                .map(this::mapearAResponse)
                .toList();
        return ResponseEntity.ok(respuesta);
    }

    @GetMapping("/todos")
    public ResponseEntity<List<ProductoResponse>> listarTodos() {
        List<ProductoResponse> respuesta = productoService.listarTodos().stream()
                .map(this::mapearAResponse)
                .toList();
        return ResponseEntity.ok(respuesta);
    }

    @GetMapping("/buscar")
    public ResponseEntity<List<ProductoResponse>> buscarPorNombre(@RequestParam String nombre) {
        List<ProductoResponse> respuesta = productoService.buscarPorNombre(nombre).stream()
                .map(this::mapearAResponse)
                .toList();
        return ResponseEntity.ok(respuesta);
    }

    @GetMapping("/{id}/stock")
    public ResponseEntity<Map<String, Object>> consultarStock(@PathVariable Integer id) {
        Integer stock = productoService.consultarStockDisponible(id);
        Map<String, Object> respuesta = new HashMap<>();
        respuesta.put("idProducto", id);
        respuesta.put("stockDisponible", stock);
        return ResponseEntity.ok(respuesta);
    }

    @GetMapping("/ranking-ventas")
    public ResponseEntity<List<Map<String, Object>>> obtenerRankingMasVendidos() {
        List<Object[]> filas = productoService.obtenerRankingProductosMasVendidos();
        List<Map<String, Object>> resultado = new ArrayList<>();

        for (Object[] fila : filas) {
            Map<String, Object> item = new HashMap<>();
            item.put("idProducto", fila[0]);
            item.put("nombre", fila[1]);
            item.put("categoria", fila[2]);
            item.put("totalUnidadesVendidas", fila[3]);
            item.put("ingresoTotalGenerado", fila[4]);
            resultado.add(item);
        }
        return ResponseEntity.ok(resultado);
    }

    @GetMapping("/alertas")
    public ResponseEntity<List<Map<String, Object>>> obtenerAlertasInventario() {
        List<Object[]> filas = productoService.obtenerAlertasDeInventario();
        List<Map<String, Object>> resultado = new ArrayList<>();

        for (Object[] fila : filas) {
            Map<String, Object> item = new HashMap<>();
            item.put("idProducto", fila[0]);
            item.put("nombre", fila[1]);
            item.put("categoria", fila[2]);
            item.put("stock", fila[3]);
            item.put("stockMinimo", fila[4]);
            item.put("fechaCaducidad", fila[5]);
            item.put("estadoStock", fila[6]);
            item.put("estadoCaducidad", fila[7]);
            resultado.add(item);
        }
        return ResponseEntity.ok(resultado);
    }

    @PostMapping
    public ResponseEntity<?> guardarProducto(@Valid @RequestBody Producto producto) {
        try {
            return ResponseEntity.ok(mapearAResponse(productoService.guardarProducto(producto)));
        } catch (RuntimeException ex) {
            return ResponseEntity.badRequest().body(java.util.Map.of("error", ex.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> actualizarProducto(@PathVariable Integer id,
                                                @Valid @RequestBody Producto producto) {
        try {
            Producto existente = productoService.obtenerPorId(id);
            existente.setNombre(producto.getNombre());
            existente.setPrecio(producto.getPrecio());
            existente.setStock(producto.getStock());
            existente.setStockMinimo(producto.getStockMinimo());
            existente.setFechaCaducidad(producto.getFechaCaducidad());
            existente.setActivo(producto.isActivo());
            if (producto.getCategoria() != null) {
                existente.setCategoria(producto.getCategoria());
            }
            return ResponseEntity.ok(mapearAResponse(productoService.guardarProducto(existente)));
        } catch (RuntimeException ex) {
            return ResponseEntity.badRequest().body(java.util.Map.of("error", ex.getMessage()));
        }
    }

    @PutMapping("/{id}/activar")
    public ResponseEntity<?> activarProducto(@PathVariable Integer id) {
        try {
            Producto existente = productoService.obtenerPorId(id);
            existente.setActivo(true);
            return ResponseEntity.ok(mapearAResponse(productoService.guardarProducto(existente)));
        } catch (RuntimeException ex) {
            return ResponseEntity.badRequest().body(java.util.Map.of("error", ex.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> desactivarProducto(@PathVariable Integer id) {
        try {
            productoService.desactivarProducto(id);
            return ResponseEntity.ok(java.util.Map.of("mensaje", "Producto desactivado."));
        } catch (RuntimeException ex) {
            return ResponseEntity.badRequest().body(java.util.Map.of("error", ex.getMessage()));
        }
    }

    private ProductoResponse mapearAResponse(Producto p) {
        ProductoResponse dto = new ProductoResponse();
        dto.setIdProducto(p.getIdProducto());
        dto.setIdCategoria(p.getCategoria() != null ? p.getCategoria().getIdCategoria() : null);
        dto.setNombre(p.getNombre());
        dto.setCategoria(p.getCategoria() != null ? p.getCategoria().getNombre() : "Sin Categoría");
        dto.setPrecio(p.getPrecio());
        dto.setStock(p.getStock());
        dto.setStockMinimo(p.getStockMinimo());
        dto.setActivo(p.isActivo());
        dto.setDisponible(p.getStock() != null && p.getStock() > 0); // RNF10.2
        dto.setFechaCaducidad(p.getFechaCaducidad());
        return dto;
    }
}
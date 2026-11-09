package com.primer.api.appgpt.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.primer.api.appgpt.entity.Producto;
import com.primer.api.appgpt.repository.ProductoRepository;

@Service
public class ProductoService {

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired(required = false)
    private ParametroService parametroService;

    public List<Producto> listarProductosActivos() {
        return productoRepository.findByNombreContainingIgnoreCaseAndActivoTrue("");
    }

    public List<Producto> listarTodos() {
        return productoRepository.findAll();
    }

    public List<Producto> buscarPorNombre(String nombre) {
        return productoRepository.findByNombreContainingIgnoreCaseAndActivoTrue(nombre);
    }

    @Transactional
    public Producto guardarProducto(Producto producto) {
        if (producto.getPrecio() == null || producto.getPrecio().compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("El precio del producto debe ser un valor monetario mayor a S/ 0.00.");
        }

        if (producto.getStock() == null || producto.getStock() < 0) {
            throw new RuntimeException("El stock del producto no puede ser negativo.");
        }

        return productoRepository.save(producto);
    }

    public Integer consultarStockDisponible(Integer idProducto) {
        Producto producto = productoRepository.findById(idProducto)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado con ID: " + idProducto));
        return producto.getStock();
    }

    public Producto obtenerPorId(Integer idProducto) {
        return productoRepository.findById(idProducto)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado con ID: " + idProducto));
    }

    @Transactional
    public void desactivarProducto(Integer idProducto) {
        Producto producto = productoRepository.findById(idProducto)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado."));
        producto.setActivo(false);
        productoRepository.save(producto);
    }

    public List<Object[]> obtenerRankingProductosMasVendidos() {
        return productoRepository.obtenerRankingDeVentas();
    }

    public List<Object[]> obtenerAlertasDeInventario() {
        return productoRepository.obtenerAlertasInventario();
    }
}
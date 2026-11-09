package com.primer.api.appgpt.service;

import java.util.List;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.primer.api.appgpt.entity.Categoria;
import com.primer.api.appgpt.repository.CategoriaRepository;
import com.primer.api.appgpt.repository.ProductoRepository;

@Service
public class CategoriaService {

    private final CategoriaRepository categoriaRepository;
    private final ProductoRepository productoRepository;

    public CategoriaService(CategoriaRepository categoriaRepository, ProductoRepository productoRepository) {
        this.categoriaRepository = categoriaRepository;
        this.productoRepository = productoRepository;
    }

    @Cacheable("categorias")
    @Transactional(readOnly = true)
    public List<Categoria> listarTodas() {
        return categoriaRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Categoria> listarRaices() {
        return categoriaRepository.findByIdCategoriaPadreIsNullOrderByNombreAsc();
    }

    @Transactional(readOnly = true)
    public List<Categoria> listarSubcategorias(Integer idCategoriaPadre) {
        return categoriaRepository.findByIdCategoriaPadreOrderByNombreAsc(idCategoriaPadre);
    }

    @CacheEvict(value = "categorias", allEntries = true)
    @Transactional
    public Categoria guardar(Categoria categoria) {
        if (categoria.getNombre() == null || categoria.getNombre().isBlank()) {
            throw new RuntimeException("El nombre de la categoría es obligatorio.");
        }

        if (categoria.getIdCategoriaPadre() != null) {
            if (categoria.getIdCategoriaPadre().equals(categoria.getIdCategoria())) {
                throw new RuntimeException("Una categoría no puede ser su propia categoría padre.");
            }
            Categoria padre = categoriaRepository.findById(categoria.getIdCategoriaPadre())
                    .orElseThrow(() -> new RuntimeException("La categoría padre no existe."));
            if (padre.getIdCategoriaPadre() != null) {
                throw new RuntimeException("Solo se permiten dos niveles: categoría y subcategoría.");
            }
        }

        String nombreNormalizado = categoria.getNombre().trim();
        categoriaRepository.findByNombreIgnoreCaseAndIdCategoriaPadre(nombreNormalizado, categoria.getIdCategoriaPadre())
                .ifPresent(existente -> {
                    if (existente.getIdCategoria() == null
                            || !existente.getIdCategoria().equals(categoria.getIdCategoria())) {
                        throw new RuntimeException("Ya existe una categoría con ese nombre en el mismo nivel.");
                    }
                });

        categoria.setNombre(nombreNormalizado);
        return categoriaRepository.save(categoria);
    }

    @CacheEvict(value = "categorias", allEntries = true)
    @Transactional
    public void eliminar(Integer id) {
        Categoria categoria = categoriaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Categoría no encontrada con ID: " + id));

        boolean tieneSubcategorias = !categoriaRepository
                .findByIdCategoriaPadreOrderByNombreAsc(id).isEmpty();
        if (tieneSubcategorias) {
            throw new RuntimeException("No se puede eliminar: la categoría tiene subcategorías asociadas.");
        }

        // No se puede eliminar si tiene productos asociados
        boolean tieneProductos = productoRepository.existsByCategoria_IdCategoria(id);
        if (tieneProductos) {
            throw new RuntimeException(
                    "No se puede eliminar: la categoría tiene productos asociados.");
        }
        categoriaRepository.delete(categoria);
    }
}

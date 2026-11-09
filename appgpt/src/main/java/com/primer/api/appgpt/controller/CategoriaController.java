package com.primer.api.appgpt.controller;

import java.util.List;

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

import com.primer.api.appgpt.entity.Categoria;
import com.primer.api.appgpt.repository.CategoriaRepository;
import com.primer.api.appgpt.service.CategoriaService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/categorias")
public class CategoriaController {

    private final CategoriaService categoriaService;
    private final CategoriaRepository categoriaRepository;

    public CategoriaController(CategoriaService categoriaService, CategoriaRepository categoriaRepository) {
        this.categoriaService = categoriaService;
        this.categoriaRepository = categoriaRepository;
    }

    @GetMapping
    public ResponseEntity<?> listar(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        if (page != null || size != null) {
            int p = page == null ? 0 : page;
            int s = size == null ? 20 : size;
            Pageable pageable = PageRequest.of(p, s);
            return ResponseEntity.ok(categoriaRepository.findAll(pageable));
        }
        return ResponseEntity.ok(categoriaService.listarTodas());
    }

    @GetMapping("/raices")
    public ResponseEntity<List<Categoria>> listarRaices() {
        return ResponseEntity.ok(categoriaService.listarRaices());
    }

    @GetMapping("/{id}/subcategorias")
    public ResponseEntity<List<Categoria>> listarSubcategorias(@PathVariable Integer id) {
        return ResponseEntity.ok(categoriaService.listarSubcategorias(id));
    }

    @PostMapping
    public ResponseEntity<?> crear(@Valid @RequestBody Categoria categoria) {
        try {
            return ResponseEntity.ok(categoriaService.guardar(categoria));
        } catch (RuntimeException ex) {
            return ResponseEntity.badRequest().body(java.util.Map.of("error", ex.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> actualizar(@PathVariable Integer id, @Valid @RequestBody Categoria categoria) {
        try {
            categoria.setIdCategoria(id);
            return ResponseEntity.ok(categoriaService.guardar(categoria));
        } catch (RuntimeException ex) {
            return ResponseEntity.badRequest().body(java.util.Map.of("error", ex.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminar(@PathVariable Integer id) {
        try {
            categoriaService.eliminar(id);
            return ResponseEntity.ok(java.util.Map.of("mensaje", "Categoría eliminada."));
        } catch (RuntimeException ex) {
            return ResponseEntity.badRequest().body(java.util.Map.of("error", ex.getMessage()));
        }
    }
}

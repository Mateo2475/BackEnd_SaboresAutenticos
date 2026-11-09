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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.primer.api.appgpt.dto.EntityMapper;
import com.primer.api.appgpt.dto.response.EmpleadoResponse;
import com.primer.api.appgpt.entity.Empleado;
import com.primer.api.appgpt.repository.EmpleadoRepository;
import com.primer.api.appgpt.service.EmpleadoService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/empleados")
public class EmpleadoController {

    private final EmpleadoService empleadoService;
    private final EmpleadoRepository empleadoRepository;

    public EmpleadoController(EmpleadoService empleadoService, EmpleadoRepository empleadoRepository) {
        this.empleadoService = empleadoService;
        this.empleadoRepository = empleadoRepository;
    }

    @GetMapping
    public ResponseEntity<?> listar(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        if (page != null || size != null) {
            int p = page == null ? 0 : page;
            int s = size == null ? 20 : size;
            Pageable pageable = PageRequest.of(p, s);
            Page<Empleado> empleados = empleadoRepository.findAll(pageable);
            return ResponseEntity.ok(empleados.map(EntityMapper::toEmpleadoResponse));
        }
        List<EmpleadoResponse> respuesta = empleadoService.listarTodos().stream()
                .map(EntityMapper::toEmpleadoResponse)
                .toList();
        return ResponseEntity.ok(respuesta);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> obtenerPorId(@PathVariable Integer id) {
        try {
            return ResponseEntity.ok(EntityMapper.toEmpleadoResponse(empleadoService.obtenerPorId(id)));
        } catch (RuntimeException ex) {
            return ResponseEntity.badRequest().body(java.util.Map.of("error", ex.getMessage()));
        }
    }

    @PostMapping
    public ResponseEntity<?> crear(@Valid @RequestBody Empleado empleado) {
        try {
            return ResponseEntity.ok(EntityMapper.toEmpleadoResponse(empleadoService.guardar(empleado)));
        } catch (RuntimeException ex) {
            return ResponseEntity.badRequest().body(java.util.Map.of("error", ex.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> actualizar(@PathVariable Integer id, @Valid @RequestBody Empleado empleado) {
        try {
            Empleado existente = empleadoService.obtenerPorId(id);
            empleado.setIdEmpleado(id);
            empleado.setUsuario(existente.getUsuario()); // evita desvincular el login del empleado
            return ResponseEntity.ok(EntityMapper.toEmpleadoResponse(empleadoService.guardar(empleado)));
        } catch (RuntimeException ex) {
            return ResponseEntity.badRequest().body(java.util.Map.of("error", ex.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminar(@PathVariable Integer id) {
        try {
            empleadoService.eliminar(id);
            return ResponseEntity.ok(java.util.Map.of("mensaje", "Empleado eliminado."));
        } catch (RuntimeException ex) {
            return ResponseEntity.badRequest().body(java.util.Map.of("error", ex.getMessage()));
        }
    }
}

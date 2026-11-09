package com.primer.api.appgpt.controller;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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

import com.primer.api.appgpt.dto.EntityMapper;
import com.primer.api.appgpt.dto.response.ClienteResponse;
import com.primer.api.appgpt.entity.Cliente;
import com.primer.api.appgpt.repository.ClienteRepository;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/clientes")
public class ClienteController {

    private final ClienteRepository clienteRepository;

    public ClienteController(ClienteRepository clienteRepository) {
        this.clienteRepository = clienteRepository;
    }

    @GetMapping
    public ResponseEntity<?> listarTodos(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        if (page != null || size != null) {
            int p = page == null ? 0 : page;
            int s = size == null ? 20 : size;
            Pageable pageable = PageRequest.of(p, s, Sort.by("idCliente").ascending());
            Page<Cliente> clientes = clienteRepository.findAll(pageable);
            return ResponseEntity.ok(clientes.map(EntityMapper::toClienteResponse));
        }
        List<ClienteResponse> respuesta = clienteRepository.findAll().stream()
                .map(EntityMapper::toClienteResponse)
                .toList();
        return ResponseEntity.ok(respuesta);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> obtenerPorId(@PathVariable Integer id) {
        return clienteRepository.findById(id)
                .<ResponseEntity<?>>map(c -> ResponseEntity.ok(EntityMapper.toClienteResponse(c)))
                .orElseGet(() -> ResponseEntity.badRequest().body(
                        java.util.Map.of("error", "Cliente no encontrado con ID: " + id)));
    }

    @GetMapping("/buscar")
    public ResponseEntity<List<ClienteResponse>> buscarClientes(@RequestParam String filtro) {
        // RF06: busca por nombres, apellidos, DNI o teléfono
        List<ClienteResponse> respuesta = clienteRepository
                .buscarPorFiltro(filtro == null ? "" : filtro.trim())
                .stream()
                .map(EntityMapper::toClienteResponse)
                .toList();
        return ResponseEntity.ok(respuesta);
    }

    @PostMapping
    public ResponseEntity<?> guardarCliente(@Valid @RequestBody Cliente cliente) {
        try {
            if (cliente.getDni() == null || !cliente.getDni().matches("\\d{8}")) {
                return ResponseEntity.badRequest()
                        .body(java.util.Map.of("error", "El DNI debe tener 8 dígitos."));
            }
            if (clienteRepository.findByDni(cliente.getDni()).isPresent()) {
                return ResponseEntity.badRequest()
                        .body(java.util.Map.of("error", "Ya existe un cliente con ese DNI."));
            }
            validarCorreoUnico(cliente.getCorreo(), null);

            Cliente guardado = clienteRepository.save(cliente);
            return ResponseEntity.ok(EntityMapper.toClienteResponse(guardado));
        } catch (RuntimeException ex) {
            return ResponseEntity.badRequest().body(java.util.Map.of("error", ex.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> actualizar(@PathVariable Integer id, @Valid @RequestBody Cliente cliente) {
        try {
            Cliente existente = clienteRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Cliente no encontrado."));

            if (cliente.getDni() == null || !cliente.getDni().matches("\\d{8}")) {
                return ResponseEntity.badRequest()
                        .body(java.util.Map.of("error", "El DNI debe tener 8 dígitos."));
            }
            clienteRepository.findByDni(cliente.getDni()).ifPresent(otro -> {
                if (!otro.getIdCliente().equals(id)) {
                    throw new RuntimeException("Ya existe un cliente con ese DNI.");
                }
            });
            validarCorreoUnico(cliente.getCorreo(), id);

            existente.setNombres(cliente.getNombres());
            existente.setApellidos(cliente.getApellidos());
            existente.setDni(cliente.getDni());
            existente.setCorreo(cliente.getCorreo());
            existente.setTelefono(cliente.getTelefono());
            existente.setArea(cliente.getArea());
            return ResponseEntity.ok(EntityMapper.toClienteResponse(clienteRepository.save(existente)));
        } catch (RuntimeException ex) {
            return ResponseEntity.badRequest().body(java.util.Map.of("error", ex.getMessage()));
        }
    }

    /**
     * RF02: el correo del cliente debe ser único. Se ignora cuando está vacío.
     */
    private void validarCorreoUnico(String correo, Integer idClienteActual) {
        if (correo == null || correo.isBlank()) {
            return;
        }
        String normalizado = correo.trim().toLowerCase();
        boolean existe = idClienteActual == null
                ? clienteRepository.existsByCorreoIgnoreCase(normalizado)
                : clienteRepository.existsByCorreoIgnoreCaseAndIdClienteNot(normalizado, idClienteActual);
        if (existe) {
            throw new RuntimeException("Ya existe un cliente con el correo: " + normalizado);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> desactivar(@PathVariable Integer id) {
        try {
            Cliente existente = clienteRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Cliente no encontrado."));
            existente.setActivo(false);
            clienteRepository.save(existente);
            return ResponseEntity.ok(java.util.Map.of("mensaje", "Cliente desactivado."));
        } catch (RuntimeException ex) {
            return ResponseEntity.badRequest().body(java.util.Map.of("error", ex.getMessage()));
        }
    }

    @PutMapping("/{id}/activar")
    public ResponseEntity<?> activar(@PathVariable Integer id) {
        try {
            Cliente existente = clienteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado."));
            existente.setActivo(true);
            clienteRepository.save(existente);
            return ResponseEntity.ok(java.util.Map.of("mensaje", "Cliente activado."));
        } catch (RuntimeException ex) {
            return ResponseEntity.badRequest().body(java.util.Map.of("error", ex.getMessage()));
        }
    }
}

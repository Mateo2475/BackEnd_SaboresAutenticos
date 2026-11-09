package com.primer.api.appgpt.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.primer.api.appgpt.entity.Empleado;
import com.primer.api.appgpt.repository.EmpleadoRepository;

@Service
public class EmpleadoService {

    private final EmpleadoRepository empleadoRepository;

    public EmpleadoService(EmpleadoRepository empleadoRepository) {
        this.empleadoRepository = empleadoRepository;
    }

    @Transactional(readOnly = true)
    public List<Empleado> listarTodos() {
        return empleadoRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Empleado obtenerPorId(Integer id) {
        return empleadoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Empleado no encontrado con ID: " + id));
    }

    @Transactional(readOnly = true)
    public Empleado buscarPorUsername(String username) {
        return empleadoRepository.buscarPorUsername(username)
                .orElseThrow(() -> new RuntimeException(
                        "No existe un empleado vinculado al usuario: " + username));
    }

    @Transactional
    public Empleado guardar(Empleado empleado) {
        if (empleado.getDni() == null || !empleado.getDni().matches("\\d{8}")) {
            throw new RuntimeException("El DNI debe tener exactamente 8 dígitos.");
        }
        empleadoRepository.findByDni(empleado.getDni()).ifPresent(existente -> {
            if (existente.getIdEmpleado() == null
                    || !existente.getIdEmpleado().equals(empleado.getIdEmpleado())) {
                throw new RuntimeException("Ya existe un empleado con DNI: " + empleado.getDni());
            }
        });
        return empleadoRepository.save(empleado);
    }

    @Transactional
    public void eliminar(Integer id) {
        Empleado empleado = obtenerPorId(id);
        empleadoRepository.delete(empleado);
    }
}

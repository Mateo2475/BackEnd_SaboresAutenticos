package com.primer.api.appgpt.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.primer.api.appgpt.entity.Empleado;

public interface EmpleadoRepository extends JpaRepository<Empleado, Integer> {

    Optional<Empleado> findByDni(String dni);

    @Query("SELECT e FROM Empleado e WHERE e.usuario.username = :username")
    Optional<Empleado> buscarPorUsername(@Param("username") String username);
}

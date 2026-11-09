package com.primer.api.appgpt.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.primer.api.appgpt.entity.Cliente;

public interface ClienteRepository extends JpaRepository<Cliente, Integer> {

    Optional<Cliente> findByDni(String dni);

    boolean existsByCorreoIgnoreCase(String correo);

    boolean existsByCorreoIgnoreCaseAndIdClienteNot(String correo, Integer idCliente);

    List<Cliente> findByNombresContainingIgnoreCaseOrTelefonoContaining(String nombres, String telefono);

    /**
     * Búsqueda tolerante (RF06): nombres, apellidos, DNI o teléfono.
     */
    @Query("SELECT c FROM Cliente c WHERE "
            + "LOWER(c.nombres) LIKE LOWER(CONCAT('%', :filtro, '%')) "
            + "OR LOWER(c.apellidos) LIKE LOWER(CONCAT('%', :filtro, '%')) "
            + "OR c.dni LIKE CONCAT('%', :filtro, '%') "
            + "OR c.telefono LIKE CONCAT('%', :filtro, '%') "
            + "ORDER BY c.apellidos ASC, c.nombres ASC")
    List<Cliente> buscarPorFiltro(@Param("filtro") String filtro);
}

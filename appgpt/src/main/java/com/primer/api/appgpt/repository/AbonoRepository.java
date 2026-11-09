package com.primer.api.appgpt.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.primer.api.appgpt.entity.Abono;


public interface AbonoRepository extends JpaRepository<Abono, Integer> {

   
    @Query(value = "SELECT * FROM V_HistorialCobrosCajero WHERE IdEmpleado = :idEmpleado ORDER BY FechaOperacion DESC", nativeQuery = true)
    List<Object[]> obtenerHistorialCobrosPorCajero(@Param("idEmpleado") Integer idEmpleado);

    @Query(value = "SELECT * FROM V_HistorialCobrosCajero WHERE IdEmpleado = :idEmpleado "
            + "AND (:desde IS NULL OR FechaOperacion >= :desde) "
            + "AND (:hasta IS NULL OR FechaOperacion <= :hasta) "
            + "ORDER BY FechaOperacion DESC", nativeQuery = true)
    List<Object[]> obtenerHistorialCobrosPorCajeroYFechas(@Param("idEmpleado") Integer idEmpleado,
                                                          @Param("desde") java.time.LocalDateTime desde,
                                                          @Param("hasta") java.time.LocalDateTime hasta);

}
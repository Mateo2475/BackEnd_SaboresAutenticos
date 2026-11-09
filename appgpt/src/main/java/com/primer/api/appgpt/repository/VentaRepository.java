package com.primer.api.appgpt.repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.primer.api.appgpt.entity.Venta;

public interface VentaRepository extends JpaRepository<Venta, Integer> {

    @Query(value = "SELECT * FROM V_ReporteVentasMensual ORDER BY Anio DESC, Mes DESC", nativeQuery = true)
    List<Object[]> obtenerReporteVentasMensual();

    List<Venta> findByEstado(String estado);

    List<Venta> findByCliente_IdCliente(Integer idCliente);

    Page<Venta> findAll(Pageable pageable);

    Page<Venta> findByCliente_IdCliente(Integer idCliente, Pageable pageable);

    List<Venta> findByFechaHoraBetweenAndEstadoOrderByFechaHoraDesc(LocalDateTime desde,
                                                                   LocalDateTime hasta,
                                                                   String estado);

    @Query("SELECT COALESCE(SUM(v.total), 0) FROM Venta v WHERE v.estado = 'REGISTRADA' "
            + "AND v.fechaHora BETWEEN :desde AND :hasta")
    BigDecimal totalVentasEntre(@Param("desde") LocalDateTime desde, @Param("hasta") LocalDateTime hasta);

    @Query("SELECT COUNT(v) FROM Venta v WHERE v.estado = 'REGISTRADA' "
            + "AND v.fechaHora BETWEEN :desde AND :hasta")
    Long contarVentasEntre(@Param("desde") LocalDateTime desde, @Param("hasta") LocalDateTime hasta);

    @Query("SELECT v.tipoPago, COUNT(v), COALESCE(SUM(v.total), 0) FROM Venta v "
            + "WHERE v.estado = 'REGISTRADA' AND v.fechaHora BETWEEN :desde AND :hasta "
            + "GROUP BY v.tipoPago")
    List<Object[]> resumenPorTipoPago(@Param("desde") LocalDateTime desde, @Param("hasta") LocalDateTime hasta);

    @Query("SELECT FUNCTION('DATE', v.fechaHora), COUNT(v), COALESCE(SUM(v.total), 0) FROM Venta v "
            + "WHERE v.estado = 'REGISTRADA' AND v.fechaHora BETWEEN :desde AND :hasta "
            + "GROUP BY FUNCTION('DATE', v.fechaHora) ORDER BY FUNCTION('DATE', v.fechaHora)")
    List<Object[]> resumenPorDia(@Param("desde") LocalDateTime desde, @Param("hasta") LocalDateTime hasta);
}

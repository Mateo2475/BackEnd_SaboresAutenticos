package com.primer.api.appgpt.repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.primer.api.appgpt.entity.CuentaCredito;

import jakarta.persistence.LockModeType;

public interface CuentaCreditoRepository extends JpaRepository<CuentaCredito, Integer> {

    @Query("SELECT c FROM CuentaCredito c WHERE c.cliente.idCliente = :idCliente AND c.year = :year AND c.month = :month")
    Optional<CuentaCredito> findByClienteAndYearAndMonth(@Param("idCliente") Integer idCliente,
                                                        @Param("year") Integer year,
                                                        @Param("month") Integer month);

    /**
     * Bloqueo pesimista de la cuenta del mes: evita que dos cajeros validen
     * el mismo límite de crédito al mismo tiempo (RF10 / CU-011 / CU-015).
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT c FROM CuentaCredito c WHERE c.cliente.idCliente = :idCliente AND c.year = :year AND c.month = :month")
    Optional<CuentaCredito> findByClienteAndYearAndMonthForUpdate(@Param("idCliente") Integer idCliente,
                                                                 @Param("year") Integer year,
                                                                 @Param("month") Integer month);

    /**
     * Todas las cuentas del cliente con deuda pendiente, desde la más antigua.
     * Se usa para aplicar abonos a deudas de meses anteriores.
     */
    @Query("SELECT c FROM CuentaCredito c WHERE c.cliente.idCliente = :idCliente "
            + "AND (c.montoConsumido - c.montoAbonado) > 0 "
            + "ORDER BY c.year ASC, c.month ASC")
    List<CuentaCredito> findCuentasConDeuda(@Param("idCliente") Integer idCliente);

    @Query("SELECT COALESCE(SUM(c.montoConsumido - c.montoAbonado), 0) FROM CuentaCredito c "
            + "WHERE c.cliente.idCliente = :idCliente AND (c.montoConsumido - c.montoAbonado) > 0")
    BigDecimal totalDeudaCliente(@Param("idCliente") Integer idCliente);

    @Query("SELECT COALESCE(SUM(c.montoConsumido - c.montoAbonado), 0) FROM CuentaCredito c "
            + "WHERE (c.montoConsumido - c.montoAbonado) > 0")
    BigDecimal totalDeudaGlobal();

    @Query("SELECT COUNT(DISTINCT c.cliente.idCliente) FROM CuentaCredito c "
            + "WHERE (c.montoConsumido - c.montoAbonado) > 0")
    Long contarClientesConDeuda();

    @Query(value = "SELECT * FROM V_EstadoCuentaCliente WHERE IdCliente = :idCliente", nativeQuery = true)
    List<Object[]> obtenerEstadoCuentaPorCliente(@Param("idCliente") Integer idCliente);

    @Query(value = "SELECT * FROM V_EstadoCuentaCliente WHERE EstadoDeuda = 'CON DEUDA' ORDER BY DeudaPendiente DESC", nativeQuery = true)
    List<Object[]> listarDeudoresDashboard();

    @Query(value = "SELECT c.IdCliente, c.Nombres, c.Apellidos, c.DNI, COALESCE(SUM(cc.MontoConsumido - cc.MontoAbonado),0) AS DeudaTotal "
            + "FROM Cliente c INNER JOIN CuentaCredito cc ON c.IdCliente = cc.IdCliente "
            + "WHERE (cc.MontoConsumido - cc.MontoAbonado) > 0 "
            + "GROUP BY c.IdCliente, c.Nombres, c.Apellidos, c.DNI "
            + "ORDER BY DeudaTotal DESC, c.Apellidos ASC, c.Nombres ASC", nativeQuery = true)
    List<Object[]> listarTopDeudoresAgrupado();
}

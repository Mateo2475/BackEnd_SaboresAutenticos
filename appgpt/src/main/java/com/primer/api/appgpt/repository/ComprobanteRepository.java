package com.primer.api.appgpt.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.primer.api.appgpt.entity.Comprobante;

public interface ComprobanteRepository extends JpaRepository<Comprobante, Integer> {

    List<Comprobante> findByVentaIdVenta(Integer idVenta);

    List<Comprobante> findByFechaEmisionAfterOrderByFechaEmisionDesc(LocalDateTime desde);

    Page<Comprobante> findByFechaEmisionAfterOrderByFechaEmisionDesc(LocalDateTime desde, Pageable pageable);

    List<Comprobante> findByFechaEmisionBetweenOrderByFechaEmisionDesc(LocalDateTime desde, LocalDateTime hasta);

    Page<Comprobante> findByFechaEmisionBetweenOrderByFechaEmisionDesc(LocalDateTime desde, LocalDateTime hasta, Pageable pageable);

    Optional<Comprobante> findByTipoAndSerieAndNumero(String tipo, String serie, Integer numero);

    @Query("SELECT MAX(c.numero) FROM Comprobante c WHERE c.tipo = :tipo AND c.serie = :serie")
    Optional<Integer> buscarUltimoNumero(@Param("tipo") String tipo, @Param("serie") String serie);
}

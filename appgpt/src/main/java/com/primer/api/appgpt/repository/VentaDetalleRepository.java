package com.primer.api.appgpt.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.primer.api.appgpt.entity.VentaDetalle;

public interface VentaDetalleRepository extends JpaRepository<VentaDetalle, Integer> {

    List<VentaDetalle> findByVentaIdVenta(Integer idVenta);
}

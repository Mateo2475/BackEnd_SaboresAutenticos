package com.primer.api.appgpt.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.primer.api.appgpt.entity.VentaPago;

public interface VentaPagoRepository extends JpaRepository<VentaPago, Integer> {

    List<VentaPago> findByVentaIdVenta(Integer idVenta);
}

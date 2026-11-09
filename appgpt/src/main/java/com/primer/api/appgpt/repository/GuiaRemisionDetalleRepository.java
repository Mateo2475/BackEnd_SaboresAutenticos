package com.primer.api.appgpt.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.primer.api.appgpt.entity.GuiaRemisionDetalle;

public interface GuiaRemisionDetalleRepository extends JpaRepository<GuiaRemisionDetalle, Integer> {

    List<GuiaRemisionDetalle> findByGuiaRemisionIdGuia(Integer idGuia);
}

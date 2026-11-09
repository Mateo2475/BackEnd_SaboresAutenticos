package com.primer.api.appgpt.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.primer.api.appgpt.entity.GuiaRemision;

public interface GuiaRemisionRepository extends JpaRepository<GuiaRemision, Integer> {

    List<GuiaRemision> findAllByOrderByFechaIngresoDesc();
}

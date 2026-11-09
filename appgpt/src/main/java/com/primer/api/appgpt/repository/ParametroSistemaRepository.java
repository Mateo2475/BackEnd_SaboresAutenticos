package com.primer.api.appgpt.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.primer.api.appgpt.entity.ParametroSistema;

public interface ParametroSistemaRepository extends JpaRepository<ParametroSistema, String> {
}
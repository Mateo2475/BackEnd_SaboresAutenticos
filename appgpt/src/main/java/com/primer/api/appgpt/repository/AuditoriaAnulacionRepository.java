package com.primer.api.appgpt.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.primer.api.appgpt.entity.AuditoriaAnulacion;

public interface AuditoriaAnulacionRepository extends JpaRepository<AuditoriaAnulacion, Integer> {
}
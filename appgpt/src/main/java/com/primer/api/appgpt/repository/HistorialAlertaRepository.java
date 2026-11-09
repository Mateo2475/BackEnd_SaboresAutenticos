package com.primer.api.appgpt.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.primer.api.appgpt.entity.HistorialAlerta;

public interface HistorialAlertaRepository extends JpaRepository<HistorialAlerta, Integer> {
}
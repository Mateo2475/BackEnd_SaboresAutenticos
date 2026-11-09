package com.primer.api.appgpt.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.primer.api.appgpt.entity.Categoria;

public interface CategoriaRepository extends JpaRepository<Categoria, Integer> {

    Optional<Categoria> findByNombreIgnoreCase(String nombre);

    boolean existsByNombreIgnoreCase(String nombre);

    List<Categoria> findByIdCategoriaPadreOrderByNombreAsc(Integer idCategoriaPadre);

    List<Categoria> findByIdCategoriaPadreIsNullOrderByNombreAsc();

    Optional<Categoria> findByNombreIgnoreCaseAndIdCategoriaPadre(String nombre, Integer idCategoriaPadre);
}

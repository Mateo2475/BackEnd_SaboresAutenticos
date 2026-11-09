package com.primer.api.appgpt.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.primer.api.appgpt.entity.Usuario;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, String> {

    Optional<Usuario> findByUsername(String username);
    
    boolean existsByUsername(String username);
}
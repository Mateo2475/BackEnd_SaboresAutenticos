package com.primer.api.appgpt.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.primer.api.appgpt.entity.Authority;

public interface AuthorityRepository extends JpaRepository<Authority, Authority.AuthorityId> {

    List<Authority> findByUsername(String username);
    
}
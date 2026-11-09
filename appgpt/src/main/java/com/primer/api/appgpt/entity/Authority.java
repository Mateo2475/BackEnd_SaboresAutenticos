package com.primer.api.appgpt.entity;

import java.io.Serializable;

import java.util.Objects;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;

@Entity 
@Table (name = "authorities")
@IdClass (Authority.AuthorityId.class)
public class Authority {

    @Id 
    @Column (name = "username", length = 50, nullable = false)
    private String username;

    @Id 
    @Column(name = "authority", length = 50, nullable = false)
    private String authority;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getAuthority() {
        return authority;
    }

    public void setAuthority(String authority) {
        this.authority = authority;
    }

    public static class AuthorityId implements Serializable {
        private String username;
        private String authority;

        public AuthorityId() {}

        @Override 
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            AuthorityId that = (AuthorityId) o;
            return Objects.equals(username, that.username) &&
                   Objects.equals(authority, that.authority);
        }

        @Override
        public int hashCode() {
            return Objects.hash(username, authority);

        }     
    }    
}

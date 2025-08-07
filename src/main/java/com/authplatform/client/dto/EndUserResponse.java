package com.authplatform.client.dto;

import java.util.Set;

public class EndUserResponse {
    private Long id;
    private String fullName;
    private String email;
    private boolean emailVerified;
    private boolean locked;
    private Set<String> roles;

    public Long getId() {
        return id;
    }

    public String getFullName() {
        return fullName;
    }

    public String getEmail() {
        return email;
    }

    public boolean isEmailVerified() {
        return emailVerified;
    }

    public boolean isLocked() {
        return locked;
    }

    public Set<String> getRoles() {
        return roles;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setEmailVerified(boolean emailVerified) {
        this.emailVerified = emailVerified;
    }

    public void setLocked(boolean locked) {
        this.locked = locked;
    }

    public void setRoles(Set<String> roles) {
        this.roles = roles;
    }
}

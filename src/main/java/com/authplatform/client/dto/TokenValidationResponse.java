package com.authplatform.client.dto;

import java.util.Set;

public class TokenValidationResponse {
    private boolean valid;
    private String email;
    private String userId;
    private Set<String> roles;

    public boolean isValid() {
        return valid;
    }

    public String getEmail() {
        return email;
    }

    public String getUserId() {
        return userId;
    }

    public Set<String> getRoles() {
        return roles;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setRoles(Set<String> roles) {
        this.roles = roles;
    }
}

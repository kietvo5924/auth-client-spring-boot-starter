package com.authplatform.client.dto;

import java.util.Set;

public class UpdateEndUserRolesRequest {
    private Set<Long> roleIds;

    public Set<Long> getRoleIds() {
        return roleIds;
    }

    public void setRoleIds(Set<Long> roleIds) {
        this.roleIds = roleIds;
    }
}

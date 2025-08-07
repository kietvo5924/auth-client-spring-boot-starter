package com.authplatform.client.dto;

public class ProjectRoleResponse {
    private Long id;
    private String name;
    private int level;

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getLevel() {
        return level;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setLevel(int level) {
        this.level = level;
    }
}

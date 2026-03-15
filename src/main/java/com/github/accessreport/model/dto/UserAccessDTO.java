package com.github.accessreport.model.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_NULL)
public class UserAccessDTO {

    private String username;
    private String name;
    private String role;
    private List<RepositoryDTO> repositories;
    private Integer repositoryCount;

    public UserAccessDTO() {
    }

    public UserAccessDTO(String username, String name, String role) {
        this.username = username;
        this.name = name;
        this.role = role;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public List<RepositoryDTO> getRepositories() {
        return repositories;
    }

    public void setRepositories(List<RepositoryDTO> repositories) {
        this.repositories = repositories;
    }

    public Integer getRepositoryCount() {
        return repositoryCount;
    }

    public void setRepositoryCount(Integer repositoryCount) {
        this.repositoryCount = repositoryCount;
    }
}

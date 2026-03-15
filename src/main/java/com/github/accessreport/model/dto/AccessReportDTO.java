package com.github.accessreport.model.dto;

import java.time.ZonedDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_NULL)
public class AccessReportDTO {

    private String organization;
    private Integer totalRepositories;
    private Integer totalUsers;
    private ZonedDateTime generatedAt;
    private List<UserAccessDTO> users;

    public AccessReportDTO() {
        this.generatedAt = ZonedDateTime.now();
    }

    public AccessReportDTO(String organization, Integer totalRepositories, Integer totalUsers, List<UserAccessDTO> users) {
        this.organization = organization;
        this.totalRepositories = totalRepositories;
        this.totalUsers = totalUsers;
        this.users = users;
        this.generatedAt = ZonedDateTime.now();
    }

    public String getOrganization() {
        return organization;
    }

    public void setOrganization(String organization) {
        this.organization = organization;
    }

    public Integer getTotalRepositories() {
        return totalRepositories;
    }

    public void setTotalRepositories(Integer totalRepositories) {
        this.totalRepositories = totalRepositories;
    }

    public Integer getTotalUsers() {
        return totalUsers;
    }

    public void setTotalUsers(Integer totalUsers) {
        this.totalUsers = totalUsers;
    }

    public ZonedDateTime getGeneratedAt() {
        return generatedAt;
    }

    public void setGeneratedAt(ZonedDateTime generatedAt) {
        this.generatedAt = generatedAt;
    }

    public List<UserAccessDTO> getUsers() {
        return users;
    }

    public void setUsers(List<UserAccessDTO> users) {
        this.users = users;
    }
}

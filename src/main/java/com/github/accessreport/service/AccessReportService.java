package com.github.accessreport.service;

import com.github.accessreport.client.GitHubRestClient;
import com.github.accessreport.exception.GitHubServiceException;
import com.github.accessreport.model.dto.AccessReportDTO;
import com.github.accessreport.model.dto.RepositoryDTO;
import com.github.accessreport.model.dto.UserAccessDTO;
import com.github.accessreport.model.entity.GitHubRepository;
import com.github.accessreport.model.entity.GitHubUser;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.ZonedDateTime;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

@Service
public class AccessReportService {

    private final GitHubRestClient gitHubRestClient;
    private final ExecutorService executorService;

    public AccessReportService(GitHubRestClient gitHubRestClient) {
        this.gitHubRestClient = gitHubRestClient;
        this.executorService = Executors.newFixedThreadPool(10);
    }

    @Cacheable(value = "accessReports", key = "#org", unless = "#result == null")
    public AccessReportDTO generateAccessReport(String org) {
        if (org == null || org.trim().isEmpty()) {
            throw new IllegalArgumentException("Organization name cannot be empty");
        }

        if (!gitHubRestClient.validateOrgExists(org)) {
            throw new GitHubServiceException("Organization '" + org + "' not found");
        }

        List<GitHubRepository> repositories = gitHubRestClient.fetchRepositories(org);

        if (repositories == null || repositories.isEmpty()) {
            return new AccessReportDTO(org, 0, 0, ZonedDateTime.now(), new ArrayList<>());
        }

        Map<String, UserAccessDTO> userAccessMap = new ConcurrentHashMap<>();

        List<Future<?>> futures = repositories.stream()
                .map(repo -> executorService.submit(() -> {
                    try {
                        List<GitHubUser> collaborators = gitHubRestClient.fetchCollaborators(org, repo.getName());
                        if (collaborators != null) {
                            for (GitHubUser user : collaborators) {
                                userAccessMap.compute(user.getLogin(), (key, dto) -> {
                                    if (dto == null) {
                                        dto = new UserAccessDTO();
                                        dto.setUsername(user.getLogin());
                                        dto.setName(user.getName() != null ? user.getName() : user.getLogin());
                                        dto.setRole(determineRole(user, org));
                                        dto.setRepositories(new ArrayList<>());
                                    }
                                    RepositoryDTO repoDTO = new RepositoryDTO();
                                    repoDTO.setName(repo.getName());
                                    repoDTO.setFullName(repo.getFullName());
                                    repoDTO.setPermission(user.getPermission());
                                    repoDTO.setPrivate(repo.isPrivate());
                                    dto.getRepositories().add(repoDTO);
                                    dto.setRepositoryCount(dto.getRepositories().size());
                                    return dto;
                                });
                            }
                        }
                    } catch (Exception e) {
                        System.err.println("Error fetching collaborators for " + repo.getName() + ": " + e.getMessage());
                    }
                }))
                .collect(Collectors.toList());

        try {
            for (Future<?> future : futures) {
                future.get(30, TimeUnit.SECONDS);
            }
        } catch (Exception e) {
            throw new GitHubServiceException("Error processing repositories: " + e.getMessage());
        }

        List<UserAccessDTO> userAccessList = new ArrayList<>(userAccessMap.values());
        userAccessList.sort(Comparator.comparing(UserAccessDTO::getUsername));

        executorService.shutdown();

        return new AccessReportDTO(org, repositories.size(), userAccessList.size(), ZonedDateTime.now(), userAccessList);
    }

    private String determineRole(GitHubUser user, String org) {
        return user.getRole() != null ? user.getRole() : "member";
    }
}

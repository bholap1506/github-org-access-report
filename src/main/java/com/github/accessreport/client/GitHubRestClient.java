package com.github.accessreport.client;

import com.github.accessreport.exception.GitHubServiceException;
import com.github.accessreport.model.entity.GitHubRepository;
import com.github.accessreport.model.entity.GitHubUser;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Component
public class GitHubRestClient {

    private final RestTemplate restTemplate;
    private final String apiBaseUrl;
    private final String token;

    public GitHubRestClient(RestTemplate restTemplate,
                            @Value("${github.api.url}") String apiBaseUrl,
                            @Value("${github.token}") String token) {
        this.restTemplate = restTemplate;
        this.apiBaseUrl = apiBaseUrl;
        this.token = token;
    }

    public List<GitHubRepository> fetchRepositories(String org) {
        String url = apiBaseUrl + "/orgs/" + org + "/repos?per_page=100";
        GitHubRepository[] repos = getForArray(url, GitHubRepository[].class);
        return Arrays.asList(repos);
    }

    public List<GitHubUser> fetchCollaborators(String org, String repo) {
        String url = apiBaseUrl + "/repos/" + org + "/" + repo + "/collaborators?per_page=100";
        GitHubUser[] users = getForArray(url, GitHubUser[].class);
        return Arrays.asList(users);
    }

    public boolean validateOrgExists(String org) {
        String url = apiBaseUrl + "/orgs/" + org;
        try {
            ResponseEntity<Map> response = getForEntity(url, Map.class);
            return response.getStatusCode() == HttpStatus.OK;
        } catch (GitHubServiceException e) {
            return false;
        }
    }

    private <T> ResponseEntity<T> getForEntity(String url, Class<T> responseType) {
        HttpHeaders headers = createAuthHeaders();
        HttpEntity<String> entity = new HttpEntity<>(headers);
        try {
            ResponseEntity<T> response = restTemplate.exchange(url, HttpMethod.GET, entity, responseType);
            if (response.getStatusCode() == HttpStatus.UNAUTHORIZED) {
                throw new GitHubServiceException("Invalid or missing GitHub token");
            }
            if (response.getStatusCode() == HttpStatus.NOT_FOUND) {
                throw new GitHubServiceException("Organization or resource not found");
            }
            if (response.getStatusCode() == HttpStatus.FORBIDDEN) {
                throw new GitHubServiceException("Rate limit exceeded");
            }
            return response;
        } catch (Exception e) {
            throw new GitHubServiceException("Failed to fetch data from GitHub: " + e.getMessage());
        }
    }

    private <T> T[] getForArray(String url, Class<T[]> arrayType) {
        ResponseEntity<T[]> response = getForEntity(url, arrayType);
        return response.getBody();
    }

    private HttpHeaders createAuthHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);
        headers.set("Accept", "application/vnd.github+json");
        headers.set("X-GitHub-Api-Version", "2022-11-28");
        return headers;
    }
}

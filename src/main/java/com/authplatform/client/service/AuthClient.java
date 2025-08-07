package com.authplatform.client.service;

import com.authplatform.client.config.AuthClientProperties;
import com.authplatform.client.dto.*;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class AuthClient {

    private final RestTemplate restTemplate;
    private final AuthClientProperties properties;

    private Long resolvedProjectId = null;

    public AuthClient(AuthClientProperties properties) {
        this.properties = properties;
        this.restTemplate = new RestTemplate();
    }

    private HttpHeaders createAuthHeaders() {
        if (properties.getApiKey() == null || properties.getProjectSecret() == null) {
            throw new IllegalStateException("API Key and Secret are not configured for management APIs.");
        }
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-API-Key", properties.getApiKey());
        headers.set("X-API-Secret", properties.getProjectSecret());
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    // --- PHƯƠNG THỨC NỘI BỘ "THÔNG MINH" ---
    private Long resolveProjectId() {
        if (this.resolvedProjectId == null) {
            if (properties.getApiKey() == null) {
                throw new IllegalStateException("API Key is not configured.");
            }
            String url = String.format("%s/api/projects/by-api-key?key=%s", properties.getBaseUrl(), properties.getApiKey());

            HttpEntity<Void> entity = new HttpEntity<>(createAuthHeaders());
            ResponseEntity<ProjectResponse> response = restTemplate.exchange(url, HttpMethod.GET, entity, ProjectResponse.class);

            if (response.getBody() == null) {
                throw new IllegalStateException("Could not resolve projectId for the given API key.");
            }
            this.resolvedProjectId = response.getBody().getId();
        }
        return this.resolvedProjectId;
    }

    /// --- EndUser Management ---

    public List<EndUserResponse> getEndUsersForProject() {
        Long projectId = resolveProjectId();
        String url = String.format("%s/api/projects/%d/endusers", properties.getBaseUrl(), projectId);
        HttpEntity<Void> entity = new HttpEntity<>(createAuthHeaders());
        ResponseEntity<EndUserResponse[]> response = restTemplate.exchange(url, HttpMethod.GET, entity, EndUserResponse[].class);
        return response.getBody() != null ? Arrays.asList(response.getBody()) : Collections.emptyList();
    }

    public EndUserResponse updateEndUserDetails(Long endUserId, UpdateEndUserRequest request) {
        Long projectId = resolveProjectId();
        String url = String.format("%s/api/projects/%d/endusers/%d", properties.getBaseUrl(), projectId, endUserId);
        HttpEntity<UpdateEndUserRequest> entity = new HttpEntity<>(request, createAuthHeaders());
        ResponseEntity<EndUserResponse> response = restTemplate.exchange(url, HttpMethod.PUT, entity, EndUserResponse.class);
        return response.getBody();
    }

    public void lockEndUser(Long endUserId) {
        Long projectId = resolveProjectId();
        String url = String.format("%s/api/projects/%d/endusers/%d/lock", properties.getBaseUrl(), projectId, endUserId);
        HttpEntity<Void> entity = new HttpEntity<>(createAuthHeaders());
        restTemplate.exchange(url, HttpMethod.POST, entity, Void.class);
    }

    public void unlockEndUser(Long endUserId) {
        Long projectId = resolveProjectId();
        String url = String.format("%s/api/projects/%d/endusers/%d/unlock", properties.getBaseUrl(), projectId, endUserId);
        HttpEntity<Void> entity = new HttpEntity<>(createAuthHeaders());
        restTemplate.exchange(url, HttpMethod.POST, entity, Void.class);
    }

    // --- ProjectRole Management ---

    public List<ProjectRoleResponse> getProjectRoles() {
        Long projectId = resolveProjectId();
        String url = String.format("%s/api/projects/%d/roles", properties.getBaseUrl(), projectId);
        HttpEntity<Void> entity = new HttpEntity<>(createAuthHeaders());
        ResponseEntity<ProjectRoleResponse[]> response = restTemplate.exchange(url, HttpMethod.GET, entity, ProjectRoleResponse[].class);
        return response.getBody() != null ? Arrays.asList(response.getBody()) : Collections.emptyList();
    }

    public ProjectRoleResponse createProjectRole(ProjectRoleRequest request) {
        Long projectId = resolveProjectId();
        String url = String.format("%s/api/projects/%d/roles", properties.getBaseUrl(), projectId);
        HttpEntity<ProjectRoleRequest> entity = new HttpEntity<>(request, createAuthHeaders());
        return restTemplate.postForObject(url, entity, ProjectRoleResponse.class);
    }

    public ProjectRoleResponse updateProjectRole(Long roleId, ProjectRoleRequest request) {
        Long projectId = resolveProjectId();
        String url = String.format("%s/api/projects/%d/roles/%d", properties.getBaseUrl(), projectId, roleId);
        HttpEntity<ProjectRoleRequest> entity = new HttpEntity<>(request, createAuthHeaders());
        ResponseEntity<ProjectRoleResponse> response = restTemplate.exchange(url, HttpMethod.PUT, entity, ProjectRoleResponse.class);
        return response.getBody();
    }

    public void deleteProjectRole(Long roleId) {
        Long projectId = resolveProjectId();
        String url = String.format("%s/api/projects/%d/roles/%d", properties.getBaseUrl(), projectId, roleId);
        HttpEntity<Void> entity = new HttpEntity<>(createAuthHeaders());
        restTemplate.exchange(url, HttpMethod.DELETE, entity, Void.class);
    }

    // --- CÁC PHƯƠNG THỨC XÁC THỰC CÔNG KHAI CHO END-USER ---

    public ApiResponse registerEndUser(EndUserRegisterRequest request) {
        String url = String.format("%s/api/p/%s/auth/register", properties.getBaseUrl(), properties.getApiKey());
        return restTemplate.postForObject(url, request, ApiResponse.class);
    }

    public AuthResponse loginEndUser(EndUserLoginRequest request) {
        String url = String.format("%s/api/p/%s/auth/login", properties.getBaseUrl(), properties.getApiKey());
        return restTemplate.postForObject(url, request, AuthResponse.class);
    }

    public ApiResponse forgotPassword(ForgotPasswordRequest request) {
        String url = String.format("%s/api/p/%s/auth/forgot-password", properties.getBaseUrl(), properties.getApiKey());
        return restTemplate.postForObject(url, request, ApiResponse.class);
    }

    public ApiResponse resetPassword(ResetPasswordRequest request) {
        String url = String.format("%s/api/p/%s/auth/reset-password", properties.getBaseUrl(), properties.getApiKey());
        return restTemplate.postForObject(url, request, ApiResponse.class);
    }

    // --- API CÁ NHÂN CỦA END-USER

    private HttpHeaders createEndUserAuthHeaders(String endUserToken) {
        if (endUserToken == null || endUserToken.isEmpty()) {
            throw new IllegalArgumentException("EndUser token cannot be null or empty.");
        }
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(endUserToken);
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    public EndUserResponse getMyProfile(String endUserToken) {
        String url = String.format("%s/api/eu/me", properties.getBaseUrl());
        HttpEntity<Void> entity = new HttpEntity<>(createEndUserAuthHeaders(endUserToken));
        ResponseEntity<EndUserResponse> response = restTemplate.exchange(url, HttpMethod.GET, entity, EndUserResponse.class);
        return response.getBody();
    }

    public EndUserResponse updateMyProfile(String endUserToken, UpdateMyProfileRequest request) {
        String url = String.format("%s/api/eu/me", properties.getBaseUrl());
        HttpEntity<UpdateMyProfileRequest> entity = new HttpEntity<>(request, createEndUserAuthHeaders(endUserToken));
        ResponseEntity<EndUserResponse> response = restTemplate.exchange(url, HttpMethod.PUT, entity, EndUserResponse.class);
        return response.getBody();
    }

    public ApiResponse changeMyPassword(String endUserToken, ChangePasswordRequest request) {
        String url = String.format("%s/api/eu/me/password", properties.getBaseUrl());
        HttpEntity<ChangePasswordRequest> entity = new HttpEntity<>(request, createEndUserAuthHeaders(endUserToken));
        ResponseEntity<ApiResponse> response = restTemplate.exchange(url, HttpMethod.PUT, entity, ApiResponse.class);
        return response.getBody();
    }

}

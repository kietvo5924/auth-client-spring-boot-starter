package com.authplatform.client.service;

import com.authplatform.client.config.AuthClientProperties;
import com.authplatform.client.dto.*;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * SDK Client để tương tác với tất cả các API của Auth Service Platform.
 * Cung cấp các phương thức tiện ích để che giấu sự phức tạp của việc gọi HTTP.
 */
public class AuthClient {

    private final RestTemplate restTemplate;
    private final AuthClientProperties properties;

    private Long resolvedProjectId = null;

    public AuthClient(AuthClientProperties properties) {
        this.properties = properties;
        this.restTemplate = new RestTemplate();
    }

    // ===================================================================
    // CÁC PHƯƠNG THỨC XÁC THỰC CÔNG KHAI CHO END-USER
    // ===================================================================

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

    // ===================================================================
    // API CÁ NHÂN CỦA END-USER (ĐÃ ĐĂNG NHẬP)
    // ===================================================================

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

    // ===================================================================
    // CÁC PHƯƠNG THỨC QUẢN LÝ (BỞI OWNER / END-USER ADMIN)
    // ===================================================================

    private Long resolveProjectId() {
        if (this.resolvedProjectId == null) {
            if (properties.getApiKey() == null || properties.getApiKey().isEmpty()) {
                throw new IllegalStateException("API Key is not configured. Please set 'auth.client.api-key'.");
            }
            // Gọi đến API công khai mới, không cần xác thực
            String url = String.format("%s/api/public/projects/resolve?apiKey=%s", properties.getBaseUrl(), properties.getApiKey());
            ProjectIdResponse response = restTemplate.getForObject(url, ProjectIdResponse.class);

            if (response == null || response.getProjectId() == null) {
                throw new IllegalStateException("Could not resolve projectId for the given API key.");
            }
            this.resolvedProjectId = response.getProjectId();
        }
        return this.resolvedProjectId;
    }

    public List<EndUserResponse> getEndUsersForProject(String adminToken) {
        Long projectId = resolveProjectId();
        String url = String.format("%s/api/projects/%d/endusers", properties.getBaseUrl(), projectId);
        HttpEntity<Void> entity = new HttpEntity<>(createEndUserAuthHeaders(adminToken));
        ResponseEntity<EndUserResponse[]> response = restTemplate.exchange(url, HttpMethod.GET, entity, EndUserResponse[].class);
        return response.getBody() != null ? Arrays.asList(response.getBody()) : Collections.emptyList();
    }

    public EndUserResponse updateEndUserDetails(String adminToken, Long endUserId, UpdateEndUserRequest request) {
        Long projectId = resolveProjectId();
        String url = String.format("%s/api/projects/%d/endusers/%d", properties.getBaseUrl(), projectId, endUserId);
        HttpEntity<UpdateEndUserRequest> entity = new HttpEntity<>(request, createEndUserAuthHeaders(adminToken));
        ResponseEntity<EndUserResponse> response = restTemplate.exchange(url, HttpMethod.PUT, entity, EndUserResponse.class);
        return response.getBody();
    }

    public void lockEndUser(String adminToken, Long endUserId) {
        Long projectId = resolveProjectId();
        String url = String.format("%s/api/projects/%d/endusers/%d/lock", properties.getBaseUrl(), projectId, endUserId);
        HttpEntity<Void> entity = new HttpEntity<>(createEndUserAuthHeaders(adminToken));
        restTemplate.exchange(url, HttpMethod.POST, entity, Void.class);
    }

    public void unlockEndUser(String adminToken, Long endUserId) {
        Long projectId = resolveProjectId();
        String url = String.format("%s/api/projects/%d/endusers/%d/unlock", properties.getBaseUrl(), projectId, endUserId);
        HttpEntity<Void> entity = new HttpEntity<>(createEndUserAuthHeaders(adminToken));
        restTemplate.exchange(url, HttpMethod.POST, entity, Void.class);
    }

    public EndUserResponse updateUserRoles(String adminToken, Long endUserId, UpdateEndUserRolesRequest request) {
        Long projectId = resolveProjectId();
        String url = String.format("%s/api/projects/%d/endusers/%d/roles", properties.getBaseUrl(), projectId, endUserId);
        HttpEntity<UpdateEndUserRolesRequest> entity = new HttpEntity<>(request, createEndUserAuthHeaders(adminToken));
        ResponseEntity<EndUserResponse> response = restTemplate.exchange(url, HttpMethod.PUT, entity, EndUserResponse.class);
        return response.getBody();
    }

    public EndUserResponse addRolesToUser(String adminToken, Long endUserId, UpdateEndUserRolesRequest request) {
        Long projectId = resolveProjectId();
        String url = String.format("%s/api/projects/%d/endusers/%d/roles", properties.getBaseUrl(), projectId, endUserId);
        HttpEntity<UpdateEndUserRolesRequest> entity = new HttpEntity<>(request, createEndUserAuthHeaders(adminToken));
        ResponseEntity<EndUserResponse> response = restTemplate.exchange(url, HttpMethod.POST, entity, EndUserResponse.class);
        return response.getBody();
    }

    public void removeRoleFromUser(String adminToken, Long endUserId, Long roleId) {
        Long projectId = resolveProjectId();
        String url = String.format("%s/api/projects/%d/endusers/%d/roles/%d", properties.getBaseUrl(), projectId, endUserId, roleId);
        HttpEntity<Void> entity = new HttpEntity<>(createEndUserAuthHeaders(adminToken));
        restTemplate.exchange(url, HttpMethod.DELETE, entity, Void.class);
    }

    // --- ProjectRole Management ---

    public List<ProjectRoleResponse> getProjectRoles(String adminToken) {
        Long projectId = resolveProjectId();
        String url = String.format("%s/api/projects/%d/roles", properties.getBaseUrl(), projectId);
        HttpEntity<Void> entity = new HttpEntity<>(createEndUserAuthHeaders(adminToken));
        ResponseEntity<ProjectRoleResponse[]> response = restTemplate.exchange(url, HttpMethod.GET, entity, ProjectRoleResponse[].class);
        return response.getBody() != null ? Arrays.asList(response.getBody()) : Collections.emptyList();
    }

    public ProjectRoleResponse createProjectRole(String adminToken, ProjectRoleRequest request) {
        Long projectId = resolveProjectId();
        String url = String.format("%s/api/projects/%d/roles", properties.getBaseUrl(), projectId);
        HttpEntity<ProjectRoleRequest> entity = new HttpEntity<>(request, createEndUserAuthHeaders(adminToken));
        return restTemplate.postForObject(url, entity, ProjectRoleResponse.class);
    }

    public ProjectRoleResponse updateProjectRole(String adminToken, Long roleId, ProjectRoleRequest request) {
        Long projectId = resolveProjectId();
        String url = String.format("%s/api/projects/%d/roles/%d", properties.getBaseUrl(), projectId, roleId);
        HttpEntity<ProjectRoleRequest> entity = new HttpEntity<>(request, createEndUserAuthHeaders(adminToken));
        ResponseEntity<ProjectRoleResponse> response = restTemplate.exchange(url, HttpMethod.PUT, entity, ProjectRoleResponse.class);
        return response.getBody();
    }

    public void deleteProjectRole(String adminToken, Long roleId) {
        Long projectId = resolveProjectId();
        String url = String.format("%s/api/projects/%d/roles/%d", properties.getBaseUrl(), projectId, roleId);
        HttpEntity<Void> entity = new HttpEntity<>(createEndUserAuthHeaders(adminToken));
        restTemplate.exchange(url, HttpMethod.DELETE, entity, Void.class);
    }
}
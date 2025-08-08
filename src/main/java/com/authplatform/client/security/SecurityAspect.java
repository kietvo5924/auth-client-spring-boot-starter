package com.authplatform.client.security;

import com.authplatform.client.config.AuthClientProperties;
import com.authplatform.client.dto.TokenValidationRequest;
import com.authplatform.client.dto.TokenValidationResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Arrays;
import java.util.Collections;
import java.util.Set;

@Aspect
public class SecurityAspect {

    private final AuthClientProperties properties;
    private final RestTemplate restTemplate;

    public SecurityAspect(AuthClientProperties properties) {
        this.properties = properties;
        this.restTemplate = new RestTemplate();
    }

    /**
     * Lấy token từ request, gọi API /validate-token, và trả về response đã được xác thực.
     * Ném ra lỗi nếu token thiếu, không hợp lệ, hoặc hết hạn.
     */
    private TokenValidationResponse getValidationResponse() {
        // 1. Lấy request hiện tại để đọc header
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        String authHeader = request.getHeader("Authorization");

        // 2. Kiểm tra và lấy token
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new AccessDeniedException("Missing or invalid Authorization header.");
        }
        String token = authHeader.substring(7);

        // 3. Chuẩn bị và gọi đến API /validate-token của dịch vụ chính
        String validationUrl = String.format("%s/api/p/%s/auth/validate-token", properties.getBaseUrl(), properties.getApiKey());
        TokenValidationRequest validationRequest = new TokenValidationRequest();
        validationRequest.setToken(token);

        try {
            TokenValidationResponse response = restTemplate.postForObject(validationUrl, validationRequest, TokenValidationResponse.class);

            // 4. Kiểm tra kết quả trả về
            if (response == null || !response.isValid()) {
                throw new AccessDeniedException("Token validation failed.");
            }

            // 5. Lưu thông tin user vào request để controller có thể dùng nếu cần
            request.setAttribute("validatedUser", response);
            request.setAttribute("validatedUserEmail", response.getEmail());
            request.setAttribute("validatedUserId", response.getUserId());

            return response;
        } catch (HttpClientErrorException.Unauthorized e) {
            throw new AccessDeniedException("Token is invalid or expired.");
        }
    }

    @Before("@annotation(annotation)")
    public void checkRole(RequiresProjectRole annotation) {
        TokenValidationResponse response = getValidationResponse();
        Set<String> userRoles = response.getRoles() != null ? response.getRoles() : Collections.emptySet();
        boolean hasRole = Arrays.stream(annotation.value()).anyMatch(userRoles::contains);
        if (!hasRole) {
            throw new AccessDeniedException("User does not have the required roles: " + Arrays.toString(annotation.value()));
        }
    }

    @Before("@annotation(annotation)")
    public void checkLevel(RequiresProjectLevel annotation) {
        TokenValidationResponse response = getValidationResponse();
        int requiredLevel = annotation.value();
        int userLevel = response.getMaxRoleLevel();
        if (userLevel < requiredLevel) {
            throw new AccessDeniedException("User does not have the required level. Required: " + requiredLevel + ", Found: " + userLevel);
        }
    }
}

class AccessDeniedException extends RuntimeException {
    public AccessDeniedException(String message) { super(message); }
}

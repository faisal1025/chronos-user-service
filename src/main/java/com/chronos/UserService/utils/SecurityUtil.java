package com.chronos.UserService.utils;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class SecurityUtil {

    /**
     * Get the current authenticated user's ID from the Security context
     */
    public Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.isAuthenticated()) {
            // Try to get userId from details if available
            Object details = authentication.getDetails();
            if (details instanceof Map) {
                Map<String, Object> detailsMap = (Map<String, Object>) details;
                Object userId = detailsMap.get("userId");
                return parseLongValue(userId);
            }

            // Try to get from principal (username might be userId)
            Object principal = authentication.getPrincipal();
            if (principal instanceof String) {
                return parseLongValue(principal);
            }
        }
        return null;
    }

    /**
     * Get the current authenticated user's email from the Security context
     */
    public String getCurrentUserEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.isAuthenticated()) {
            // Try to get email from details if available
            Object details = authentication.getDetails();
            if (details instanceof Map) {
                Map<String, Object> detailsMap = (Map<String, Object>) details;
                Object email = detailsMap.get("email");
                if (email instanceof String) {
                    return (String) email;
                }
            }

            // Use principal as email (if it looks like an email)
            Object principal = authentication.getPrincipal();
            if (principal instanceof String) {
                String principalStr = (String) principal;
                if (principalStr.contains("@")) {
                    return principalStr;
                }
            }
        }
        return null;
    }

    /**
     * Get the current authenticated user's role from the Security context
     */
    public String getCurrentUserRole() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.isAuthenticated()) {
            // Try to get role from details if available
            Object details = authentication.getDetails();
            if (details instanceof Map) {
                Map<String, Object> detailsMap = (Map<String, Object>) details;
                Object role = detailsMap.get("role");
                if (role instanceof String) {
                    return (String) role;
                }
            }

            // Try to extract from authorities
            return authentication.getAuthorities().stream()
                    .map(auth -> auth.getAuthority())
                    .filter(auth -> !auth.startsWith("ROLE_"))
                    .findFirst()
                    .orElse("USER");
        }
        return null;
    }

    /**
     * Check if the current user is authenticated
     */
    public boolean isAuthenticated() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.isAuthenticated();
    }

    /**
     * Verify if the current user owns the resource (matches userId)
     */
    public boolean isResourceOwner(Long resourceOwnerId) {
        Long currentUserId = getCurrentUserId();
        return currentUserId != null && currentUserId.equals(resourceOwnerId);
    }

    /**
     * Helper method to safely parse Long value from Object
     */
    private Long parseLongValue(Object value) {
        if (value == null) {
            return null;
        }

        if (value instanceof Long) {
            return (Long) value;
        } else if (value instanceof Integer) {
            return ((Integer) value).longValue();
        } else if (value instanceof String) {
            try {
                return Long.parseLong((String) value);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }
}

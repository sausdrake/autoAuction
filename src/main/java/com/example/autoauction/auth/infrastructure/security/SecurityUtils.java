// Файл: auth/infrastructure/security/SecurityUtils.java

package com.example.autoauction.auth.infrastructure.security;

import com.example.autoauction.auth.domain.UserPrincipal;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class SecurityUtils {

    /**
     * Получить текущего аутентифицированного пользователя
     */
    public static UserPrincipal getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException("Пользователь не аутентифицирован");
        }

        Object principal = authentication.getPrincipal();

        if (principal instanceof UserPrincipal) {
            return (UserPrincipal) principal;
        }

        throw new IllegalStateException("Принципал пользователя не является UserPrincipal");
    }

    /**
     * Получить ID текущего пользователя
     */
    public static Long getCurrentUserId() {
        return getCurrentUser().getUserId();
    }

    /**
     * Получить username текущего пользователя
     */
    public static String getCurrentUsername() {
        return getCurrentUser().getUsername();
    }

    /**
     * Проверить, имеет ли текущий пользователь роль
     */
    public static boolean hasRole(String role) {
        return getCurrentUser().getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals(role));
    }

    /**
     * Проверить, является ли текущий пользователь администратором
     */
    public static boolean isAdmin() {
        return hasRole("ROLE_ADMIN");
    }
}
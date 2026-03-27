package com.example.autoauction;

import com.example.autoauction.auth.domain.UserPrincipal;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public final class TestSecurityUtils {

    private TestSecurityUtils() {}

    public static void setCurrentUser(Long userId, String username, String... roles) {
        // Преобразуем строковые роли в GrantedAuthority
        List<SimpleGrantedAuthority> authorities = Arrays.stream(roles)
                .map(role -> {
                    // Добавляем префикс ROLE_ если его нет
                    String roleName = role.startsWith("ROLE_") ? role : "ROLE_" + role;
                    return new SimpleGrantedAuthority(roleName);
                })
                .collect(Collectors.toList());

        UserPrincipal userPrincipal = new UserPrincipal(
                username,
                "password",
                authorities,
                userId
        );

        Authentication authentication = new UsernamePasswordAuthenticationToken(
                userPrincipal,
                null,
                userPrincipal.getAuthorities()
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    public static void setAdminUser() {
        setCurrentUser(1L, "admin", "ADMIN");
    }

    public static void setDiagnosticUser() {
        setCurrentUser(2L, "diagnostic", "DIAGNOSTIC");
    }

    public static void setRegularUser() {
        setCurrentUser(3L, "user", "USER");
    }

    public static void clearAuthentication() {
        SecurityContextHolder.clearContext();
    }
}
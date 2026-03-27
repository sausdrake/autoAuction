// Обнови файл: admin/web/TestSecurityConfig.java

package com.example.autoauction.admin.web;

import com.example.autoauction.auth.domain.UserPrincipal;
import com.example.autoauction.auth.infrastructure.security.SecurityUtils;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;

import java.util.List;

@TestConfiguration
public class TestSecurityConfig {

    @Bean
    @Primary
    public UserDetailsService userDetailsService() {
        // Создаем пользователя с ролью ADMIN
        UserDetails admin = User.builder()
                .username("admin")
                .password(passwordEncoder().encode("admin123"))
                .roles("ADMIN")
                .build();

        // Создаем пользователя с ролью USER
        UserDetails user = User.builder()
                .username("user")
                .password(passwordEncoder().encode("user123"))
                .roles("USER")
                .build();

        // Создаем пользователя с ролью DIAGNOSTIC
        UserDetails diagnostic = User.builder()
                .username("diagnostic")
                .password(passwordEncoder().encode("diagnostic123"))
                .roles("DIAGNOSTIC")
                .build();

        return new InMemoryUserDetailsManager(admin, user, diagnostic);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // Если в контроллере используется SecurityUtils, раскомментируй:
    /*
    @Bean
    @Primary
    public SecurityUtils securityUtils() {
        return new SecurityUtils();
    }
    */
}
package com.example.autoauction.user.domain;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Set;

@Schema(description = "Пользователь системы")
public class User {

    @Schema(description = "Уникальный идентификатор", example = "1", accessMode = Schema.AccessMode.READ_ONLY)
    private final Long id;

    @Schema(description = "Имя пользователя", example = "john_doe", required = true, minLength = 3, maxLength = 50)
    private final String username;

    @Schema(description = "Email пользователя", example = "john@example.com", required = true)
    private final String email;

    @Schema(description = "Хеш пароля", example = "$2a$10$XURPShQNCsLjp1ESc2laoObo9QZDhxz73hJPaEv7/cBha4pk0AgP.",
            accessMode = Schema.AccessMode.WRITE_ONLY)
    private final String passwordHash;

    @Schema(description = "Активен ли пользователь", example = "true", defaultValue = "true")
    private final boolean active;

    @Schema(description = "Роли пользователя")
    private final Set<Role> roles;

    // конструктор и геттеры остаются без изменений
    public User(Long id, String username, String email, String passwordHash, boolean active, Set<Role> roles) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.passwordHash = passwordHash;
        this.active = active;
        this.roles = roles;
    }

    public Long getId() { return id; }
    public String getUsername() { return username; }
    public String getEmail() { return email; }
    public String getPasswordHash() { return passwordHash; }
    public boolean isActive() { return active; }
    public Set<Role> getRoles() { return roles; }
}
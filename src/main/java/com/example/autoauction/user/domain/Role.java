package com.example.autoauction.user.domain;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Роль пользователя")
public class Role {

    @Schema(description = "ID роли", example = "1", accessMode = Schema.AccessMode.READ_ONLY)
    private final Long id;

    @Schema(description = "Название роли", example = "ROLE_USER", required = true)
    private final String name;

    @Schema(description = "Описание роли", example = "Обычный пользователь")
    private final String description;

    public Role(Long id, String name, String description) {
        this.id = id;
        this.name = name;
        this.description = description;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
}
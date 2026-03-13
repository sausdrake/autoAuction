package com.example.autoauction.user.web;

import com.example.autoauction.user.application.UserService;
import com.example.autoauction.user.domain.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/users")
@Tag(name = "Пользователи", description = "API для управления пользователями")
public class UserController {
    private static final Logger log = LoggerFactory.getLogger(UserController.class);

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
        log.info("UserController initialized and ready.");
    }

    @GetMapping
    @Operation(
            summary = "Получить всех пользователей",
            description = "Возвращает список всех зарегистрированных пользователей"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Успешно получен список пользователей"),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера")
    })
    public List<UserResponse> getUsers() {
        return userService.listUsers().stream()
                .map(UserResponse::fromDomain)
                .toList();
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Получить пользователя по ID",
            description = "Возвращает информацию о конкретном пользователе"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Пользователь найден"),
            @ApiResponse(responseCode = "404", description = "Пользователь не найден"),
            @ApiResponse(responseCode = "400", description = "Некорректный ID")
    })
    public ResponseEntity<UserResponse> getUser(
            @Parameter(description = "ID пользователя", example = "1", required = true)
            @PathVariable Long id
    ) {
        return userService.getUser(id)
                .map(UserResponse::fromDomain)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    @Operation(
            summary = "Создать нового пользователя",
            description = "Регистрирует нового пользователя в системе"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Пользователь успешно создан",
                    content = @Content(schema = @Schema(implementation = UserResponse.class))),
            @ApiResponse(responseCode = "400", description = "Некорректные данные (username, email, password)"),
            @ApiResponse(responseCode = "409", description = "Пользователь с таким email или username уже существует")
    })
    public ResponseEntity<UserResponse> createUser(
            @Valid @RequestBody
            @Parameter(description = "Данные для создания пользователя", required = true)
            CreateUserRequest request
    ) {
        log.info("Received POST request for user creation with username: {}", request.username());

        User toCreate = new User(
                null,
                request.username(),
                request.email(),
                request.password(), // В реальном приложении здесь должен быть хеш пароля
                true,
                Set.of()
        );

        User created = userService.createUser(toCreate);
        return ResponseEntity.status(HttpStatus.CREATED).body(UserResponse.fromDomain(created));
    }

    @GetMapping("/info")
    @Operation(
            summary = "Информация об API пользователей",
            description = "Возвращает информацию о доступных эндпоинтах"
    )
    public ResponseEntity<ApiInfo> getApiInfo() {
        return ResponseEntity.ok(new ApiInfo(
                "User API",
                "v1.0",
                List.of("GET /api/users", "GET /api/users/{id}", "POST /api/users")
        ));
    }

    // DTO records с Swagger аннотациями
    public record CreateUserRequest(
            @Schema(description = "Имя пользователя", example = "john_doe", required = true, minLength = 3, maxLength = 50)
            @NotBlank @Size(min = 3, max = 50) String username,

            @Schema(description = "Email пользователя", example = "john@example.com", required = true)
            @NotBlank @Email String email,

            @Schema(description = "Пароль", example = "password123", required = true, minLength = 6, maxLength = 255)
            @NotBlank @Size(min = 6, max = 255) String password
    ) {}

    public record UserResponse(
            @Schema(description = "ID пользователя", example = "1")
            Long id,

            @Schema(description = "Имя пользователя", example = "john_doe")
            String username,

            @Schema(description = "Email пользователя", example = "john@example.com")
            String email,

            @Schema(description = "Активен ли пользователь", example = "true")
            boolean active
    ) {
        public static UserResponse fromDomain(User user) {
            return new UserResponse(
                    user.getId(),
                    user.getUsername(),
                    user.getEmail(),
                    user.isActive()
            );
        }
    }

    public record ApiInfo(
            String name,
            String version,
            List<String> endpoints
    ) {}
}
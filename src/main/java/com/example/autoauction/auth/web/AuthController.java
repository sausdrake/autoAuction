package com.example.autoauction.auth.web;

import com.example.autoauction.auth.application.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@Tag(name = "Аутентификация", description = "API для регистрации и входа в систему")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    @Operation(summary = "Регистрация нового пользователя")
    public ResponseEntity<AuthService.AuthResponse> register(
            @Valid @RequestBody RegisterRequest request
    ) {
        AuthService.AuthResponse response = authService.register(
                new AuthService.RegisterRequest(
                        request.username(),
                        request.email(),
                        request.password()
                )
        );
        return ResponseEntity.ok(response);
    }

    @PostMapping("/login")
    @Operation(summary = "Вход в систему")
    public ResponseEntity<AuthService.AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        System.out.println("=== LOGIN ATTEMPT ===");
        System.out.println("Username: " + request.username());
        System.out.println("Password: " + request.password());

        AuthService.AuthResponse response = authService.authenticate(
                new AuthService.LoginRequest(
                        request.username(),
                        request.password()
                )
        );

        System.out.println("=== LOGIN SUCCESS ===");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/create-hash")
    @Operation(summary = "Создать хеш пароля (временный)")
    public String createHash(@RequestParam String password) {
        return authService.createPasswordHash(password);
    }

    public record RegisterRequest(
            @NotBlank @Size(min = 3, max = 50) String username,
            @NotBlank @Email String email,
            @NotBlank @Size(min = 6, max = 255) String password
    ) {}

    public record LoginRequest(
            @NotBlank String username,
            @NotBlank String password
    ) {}
}
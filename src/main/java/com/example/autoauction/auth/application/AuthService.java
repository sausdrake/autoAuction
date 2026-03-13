package com.example.autoauction.auth.application;

import com.example.autoauction.auth.infrastructure.security.JwtService;
import com.example.autoauction.user.domain.User;
import com.example.autoauction.user.domain.port.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Set;

@Slf4j
@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;

    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            AuthenticationManager authenticationManager,
            UserDetailsService userDetailsService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
        this.userDetailsService = userDetailsService;
    }

    public AuthResponse register(RegisterRequest request) {
        // Проверяем существование
        if (userRepository.findByUsername(request.username()).isPresent()) {
            throw new RuntimeException("Username already exists");
        }
        if (userRepository.findByEmail(request.email()).isPresent()) {
            throw new RuntimeException("Email already exists");
        }

        // Создаем пользователя с захешированным паролем
        User user = new User(
                null,
                request.username(),
                request.email(),
                passwordEncoder.encode(request.password()),
                true,
                Set.of()
        );

        User savedUser = userRepository.save(user);
        UserDetails userDetails = userDetailsService.loadUserByUsername(savedUser.getUsername());

        String jwtToken = jwtService.generateToken(userDetails);
        String refreshToken = jwtService.generateRefreshToken(userDetails);

        return new AuthResponse(jwtToken, refreshToken);
    }

    public AuthResponse authenticate(LoginRequest request) {
        System.out.println("=== AUTH SERVICE: Starting authentication ===");
        System.out.println("Username: " + request.username());

        try {
            // Аутентификация через AuthenticationManager
            System.out.println("Calling authenticationManager.authenticate()");
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.username(),
                            request.password()
                    )
            );
            System.out.println("authenticationManager.authenticate() SUCCESS");

            // Загрузка UserDetails
            System.out.println("Loading UserDetails for: " + request.username());
            UserDetails userDetails = userDetailsService.loadUserByUsername(request.username());
            System.out.println("UserDetails loaded successfully");
            System.out.println("Username: " + userDetails.getUsername());
            System.out.println("Authorities: " + userDetails.getAuthorities());

            // Генерация токена
            System.out.println("Generating JWT token");
            String jwtToken = jwtService.generateToken(userDetails);
            String refreshToken = jwtService.generateRefreshToken(userDetails);
            System.out.println("Tokens generated successfully");

            return new AuthResponse(jwtToken, refreshToken);

        } catch (Exception e) {
            System.out.println("=== AUTH SERVICE ERROR ===");
            System.out.println("Exception type: " + e.getClass().getName());
            System.out.println("Exception message: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    // Временный метод для создания хеша
    public String createPasswordHash(String rawPassword) {
        return passwordEncoder.encode(rawPassword);
    }

    // ========== ВНУТРЕННИЕ КЛАССЫ (RECORDS) ==========

    public record RegisterRequest(String username, String email, String password) {}

    public record LoginRequest(String username, String password) {}

    public record AuthResponse(String accessToken, String refreshToken) {}
}
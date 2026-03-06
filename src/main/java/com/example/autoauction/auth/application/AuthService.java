package com.example.autoauction.auth.application;

import com.example.autoauction.auth.infrastructure.security.JwtService;
import com.example.autoauction.user.domain.User;
import com.example.autoauction.user.domain.port.UserRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Set;

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
        System.out.println("Password: " + request.password());
        // ВРЕМЕННЫЙ ТЕСТ: жёстко зададим пароль
        System.out.println("=== HARDCODED TEST ===");
        boolean hardcodedTest = passwordEncoder.matches("admin123", "$2a$10$XURPShQNCsLjp1ESc2laoObo9QZDhxz73hJPaEv7/cBha4pk0AgP.");
        System.out.println("Hardcoded test (admin123 vs known hash): " + hardcodedTest);

        try {
            // ДОПОЛНИТЕЛЬНАЯ ПРОВЕРКА: напрямую проверяем пароль
            System.out.println("=== PASSWORD ENCODER CHECK ===");
            System.out.println("Encoder class: " + passwordEncoder.getClass().getName());

            User user = userRepository.findByUsername(request.username())
                    .orElseThrow(() -> new BadCredentialsException("User not found"));

            System.out.println("Stored hash: " + user.getPasswordHash());
            boolean matches = passwordEncoder.matches(request.password(), user.getPasswordHash());
            System.out.println("Direct passwordEncoder.matches(): " + matches);

            if (!matches) {
                throw new BadCredentialsException("Password does not match");
            }
            System.out.println("Direct password check: SUCCESS");

            // Шаг 1: Аутентификация через AuthenticationManager
            System.out.println("Step 1: Calling authenticationManager.authenticate()");
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.username(),
                            request.password()
                    )
            );
            System.out.println("Step 1: authenticationManager.authenticate() SUCCESS");

            // Шаг 2: Загрузка UserDetails
            System.out.println("Step 2: Loading UserDetails for: " + request.username());
            UserDetails userDetails = userDetailsService.loadUserByUsername(request.username());
            System.out.println("Step 2: UserDetails loaded successfully");
            System.out.println("Username: " + userDetails.getUsername());
            System.out.println("Authorities: " + userDetails.getAuthorities());

            // Шаг 3: Генерация токена
            System.out.println("Step 3: Generating JWT token");
            String jwtToken = jwtService.generateToken(userDetails);
            String refreshToken = jwtService.generateRefreshToken(userDetails);
            System.out.println("Step 3: Tokens generated successfully");

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
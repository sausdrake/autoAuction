package com.example.autoauction.auth.application;

import com.example.autoauction.auth.domain.RefreshToken;
import com.example.autoauction.auth.domain.port.RefreshTokenRepository;
import com.example.autoauction.auth.infrastructure.security.JwtService;
import com.example.autoauction.deposit.application.DepositService;
import com.example.autoauction.user.domain.Role;
import com.example.autoauction.user.domain.User;
import com.example.autoauction.user.domain.port.RoleRepository;
import com.example.autoauction.user.domain.port.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Set;

@Slf4j
@Service
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final DepositService depositService;  // ← Добавляем
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;

    public AuthService(
            UserRepository userRepository,
            RoleRepository roleRepository,
            RefreshTokenRepository refreshTokenRepository,
            DepositService depositService,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            AuthenticationManager authenticationManager,
            UserDetailsService userDetailsService) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.depositService = depositService;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
        this.userDetailsService = userDetailsService;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        log.info("Registering new user: {}", request.username());

        if (userRepository.findByUsername(request.username()).isPresent()) {
            log.warn("Registration failed - username already exists: {}", request.username());
            throw new RuntimeException("Username already exists");
        }
        if (userRepository.findByEmail(request.email()).isPresent()) {
            log.warn("Registration failed - email already exists: {}", request.email());
            throw new RuntimeException("Email already exists");
        }

        Role userRole = roleRepository.findByName("ROLE_USER")
                .orElseThrow(() -> {
                    log.error("Default role ROLE_USER not found in database");
                    return new RuntimeException("Default role ROLE_USER not found");
                });

        User user = new User(
                null,
                request.username(),
                request.email(),
                passwordEncoder.encode(request.password()),
                true,
                Set.of(userRole)
        );

        User savedUser = userRepository.save(user);
        log.info("User created successfully with ID: {}", savedUser.getId());

        // ← АВТОМАТИЧЕСКИ СОЗДАЕМ ДЕПОЗИТ С НУЛЕВЫМ БАЛАНСОМ
        try {
            depositService.createDeposit(savedUser.getId(), BigDecimal.ZERO);
            log.info("Deposit automatically created for user: {}", savedUser.getId());
        } catch (Exception e) {
            log.error("Failed to create deposit for user {}: {}", savedUser.getId(), e.getMessage());
            // Не блокируем регистрацию, если депозит не создался
        }

        UserDetails userDetails = userDetailsService.loadUserByUsername(savedUser.getUsername());
        String jwtToken = jwtService.generateToken(userDetails);
        String refreshToken = generateAndSaveRefreshToken(savedUser);

        log.debug("Tokens generated for user: {}", savedUser.getUsername());
        return new AuthResponse(jwtToken, refreshToken);
    }

    @Transactional
    public AuthResponse authenticate(LoginRequest request) {
        log.info("Authenticating user: {}", request.username());

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.username(),
                            request.password()
                    )
            );
            log.debug("Authentication successful for user: {}", request.username());

            UserDetails userDetails = userDetailsService.loadUserByUsername(request.username());

            User user = userRepository.findByUsername(request.username())
                    .orElseThrow(() -> {
                        log.error("User not found after authentication: {}", request.username());
                        return new RuntimeException("User not found");
                    });

            String jwtToken = jwtService.generateToken(userDetails);
            String refreshToken = generateAndSaveRefreshToken(user);

            log.info("User logged in successfully: {}", request.username());
            return new AuthResponse(jwtToken, refreshToken);

        } catch (BadCredentialsException e) {
            log.warn("Authentication failed - bad credentials for user: {}", request.username());
            throw new BadCredentialsException("Invalid username or password");
        } catch (Exception e) {
            log.error("Authentication error for user: {}", request.username(), e);
            throw e;
        }
    }

    @Transactional
    public AuthResponse refreshToken(RefreshTokenRequest request) {
        log.debug("Refreshing token");

        RefreshToken refreshToken = refreshTokenRepository.findByToken(request.refreshToken())
                .orElseThrow(() -> {
                    log.warn("Invalid refresh token provided");
                    return new RuntimeException("Invalid refresh token");
                });

        if (!refreshToken.isValid()) {
            log.warn("Refresh token is expired or revoked for user: {}",
                    refreshToken.getUser().getUsername());
            throw new RuntimeException("Refresh token is expired or revoked");
        }

        User user = refreshToken.getUser();
        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getUsername());

        String newJwtToken = jwtService.generateToken(userDetails);

        // Rotate refresh token
        refreshTokenRepository.revokeAllUserTokens(user);
        String newRefreshToken = generateAndSaveRefreshToken(user);

        log.info("Tokens refreshed successfully for user: {}", user.getUsername());
        return new AuthResponse(newJwtToken, newRefreshToken);
    }

    @Transactional
    public void logout(LogoutRequest request) {
        log.info("Logging out user");

        RefreshToken refreshToken = refreshTokenRepository.findByToken(request.refreshToken())
                .orElseThrow(() -> {
                    log.warn("Invalid refresh token provided during logout");
                    return new RuntimeException("Invalid refresh token");
                });

        String username = refreshToken.getUser().getUsername();
        refreshTokenRepository.revokeAllUserTokens(refreshToken.getUser());
        log.info("User logged out successfully: {}", username);
    }

    private String generateAndSaveRefreshToken(User user) {
        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getUsername());
        String refreshTokenString = jwtService.generateRefreshToken(userDetails);

        RefreshToken refreshToken = new RefreshToken(
                null,
                refreshTokenString,
                user,
                Instant.now().plusMillis(jwtService.getRefreshExpiration()),
                false,
                Instant.now()
        );

        refreshTokenRepository.save(refreshToken);
        log.debug("Refresh token saved for user: {}", user.getUsername());
        return refreshTokenString;
    }

    public String createPasswordHash(String rawPassword) {
        return passwordEncoder.encode(rawPassword);
    }

    // ========== ВНУТРЕННИЕ КЛАССЫ (RECORDS) ==========

    public record RegisterRequest(String username, String email, String password) {}
    public record LoginRequest(String username, String password) {}
    public record RefreshTokenRequest(String refreshToken) {}
    public record LogoutRequest(String refreshToken) {}
    public record AuthResponse(String accessToken, String refreshToken) {}
}
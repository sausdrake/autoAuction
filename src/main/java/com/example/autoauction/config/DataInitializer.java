// Обнови файл: config/DataInitializer.java

package com.example.autoauction.config;

import com.example.autoauction.user.domain.Role;
import com.example.autoauction.user.domain.User;
import com.example.autoauction.user.domain.port.RoleRepository;
import com.example.autoauction.user.domain.port.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.Set;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository,
                           RoleRepository roleRepository,
                           PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        // 1. Создаем базовые роли, если их нет
        createRoleIfNotExists("ROLE_USER", "Обычный пользователь");
        createRoleIfNotExists("ROLE_DIAGNOSTIC", "Диагностик");
        createRoleIfNotExists("ROLE_ADMIN", "Администратор");

        // 2. Получаем роли для пользователей
        Role userRole = roleRepository.findByName("ROLE_USER")
                .orElseThrow(() -> new RuntimeException("ROLE_USER not found"));

        Role adminRole = roleRepository.findByName("ROLE_ADMIN")
                .orElseThrow(() -> new RuntimeException("ROLE_ADMIN not found"));

        // 3. Создаем администратора, если его нет
        if (userRepository.findByUsername("admin").isEmpty()) {
            User admin = new User(
                    null,
                    "admin",
                    "admin@example.com",
                    passwordEncoder.encode("admin123"),
                    true,
                    Set.of(adminRole)  // ← Теперь с ролью ADMIN
            );
            userRepository.save(admin);
            System.out.println("Admin user created with ADMIN role!");
        }

        // 4. Создаем тестового диагностика (опционально)
        if (userRepository.findByUsername("diagnostic").isEmpty()) {
            User diagnostic = new User(
                    null,
                    "diagnostic",
                    "diagnostic@example.com",
                    passwordEncoder.encode("diagnostic123"),
                    true,
                    Set.of(createRoleIfNotExists("ROLE_DIAGNOSTIC", "Диагностик"))  // ← Создаем роль если нет
            );
            userRepository.save(diagnostic);
            System.out.println("Diagnostic user created!");
        }

        // 5. Создаем тестового обычного пользователя (опционально)
        if (userRepository.findByUsername("user").isEmpty()) {
            User regularUser = new User(
                    null,
                    "user",
                    "user@example.com",
                    passwordEncoder.encode("user123"),
                    true,
                    Set.of(userRole)  // ← Только роль USER
            );
            userRepository.save(regularUser);
            System.out.println("Regular user created!");
        }
    }

    private Role createRoleIfNotExists(String roleName, String description) {
        Optional<Role> existingRole = roleRepository.findByName(roleName);
        if (existingRole.isPresent()) {
            return existingRole.get();
        }

        Role newRole = new Role(null, roleName, description);
        return roleRepository.save(newRole);
    }
}
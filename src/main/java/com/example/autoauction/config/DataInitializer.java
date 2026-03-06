package com.example.autoauction.config;

import com.example.autoauction.user.domain.User;
import com.example.autoauction.user.domain.port.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {
        // Создаем админа, если его нет
        if (userRepository.findByUsername("admin").isEmpty()) {
            User admin = new User(
                    null,
                    "admin",
                    "admin@example.com",
                    passwordEncoder.encode("admin123"),
                    true,
                    Set.of() // роли пока опустим для теста
            );
            userRepository.save(admin);
            System.out.println("Admin user created!");
        }
    }
}
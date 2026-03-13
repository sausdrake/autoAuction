package com.example.autoauction.user.infrastructure.persistence;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class UserRepositoryTest {

    @Autowired
    private SpringDataUserJpaRepository userRepository;

    @Test
    void findByUsername_ShouldReturnUser() {
        // given
        JpaUserEntity user = new JpaUserEntity();
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.setPasswordHash("hash");
        user.setActive(true);
        userRepository.save(user);

        // when
        Optional<JpaUserEntity> found = userRepository.findByUsername("testuser");

        // then
        assertTrue(found.isPresent());
        assertEquals("testuser", found.get().getUsername());
    }

    @Test
    void existsByEmail_ShouldReturnTrue_WhenExists() {
        // given
        JpaUserEntity user = new JpaUserEntity();
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.setPasswordHash("hash");
        user.setActive(true);
        userRepository.save(user);

        // when
        boolean exists = userRepository.existsByEmail("test@example.com");

        // then
        assertTrue(exists);
    }
}
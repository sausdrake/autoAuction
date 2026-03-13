package com.example.autoauction.user.application;

import com.example.autoauction.user.domain.User;
import com.example.autoauction.user.domain.port.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User(
                1L,
                "testuser",
                "test@example.com",
                "encodedPassword",
                true,
                Set.of()
        );
    }

    @Test
    void createUser_Success() {
        // given
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // when
        User created = userService.createUser(testUser);

        // then
        assertNotNull(created);
        assertEquals("testuser", created.getUsername());
        verify(userRepository, times(1)).save(testUser);
    }

    @Test
    void getUser_Exists() {
        // given
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        // when
        Optional<User> found = userService.getUser(1L);

        // then
        assertTrue(found.isPresent());
        assertEquals("testuser", found.get().getUsername());
    }

    @Test
    void getUser_NotFound() {
        // given
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        // when
        Optional<User> found = userService.getUser(99L);

        // then
        assertFalse(found.isPresent());
    }

    @Test
    void findByUsername_Exists() {
        // given
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        // when
        Optional<User> found = userService.findByUsername("testuser");

        // then
        assertTrue(found.isPresent());
        assertEquals("testuser", found.get().getUsername());
    }

    @Test
    void findByEmail_Exists() {
        // given
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));

        // when
        Optional<User> found = userService.findByEmail("test@example.com");

        // then
        assertTrue(found.isPresent());
        assertEquals("test@example.com", found.get().getEmail());
    }

    @Test
    void listUsers() {
        // given
        List<User> users = Arrays.asList(
                testUser,
                new User(2L, "user2", "user2@example.com", "pass", true, Set.of())
        );
        when(userRepository.findAll()).thenReturn(users);

        // when
        List<User> found = userService.listUsers();

        // then
        assertEquals(2, found.size());
    }

    @Test
    void deleteUser() {
        // given
        doNothing().when(userRepository).deleteById(1L);

        // when
        userService.deleteUser(1L);

        // then
        verify(userRepository, times(1)).deleteById(1L);
    }
}
package com.example.autoauction.user.web;

import com.example.autoauction.user.application.UserService;
import com.example.autoauction.user.domain.User;
import com.example.autoauction.user.domain.Role;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.Optional;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Slf4j
@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    private User testUser;
    private UserController.CreateUserRequest createRequest;

    @BeforeEach
    void setUp() {
        log.info("========== НАСТРОЙКА ТЕСТОВ UserController ==========");

        Role role = new Role(1L, "ROLE_USER", "Обычный пользователь");

        testUser = new User(
                1L,
                "testuser",
                "test@example.com",
                "encodedPassword",
                true,
                Set.of(role)
        );
        log.debug("Создан тестовый пользователь: username=testuser, id=1");

        createRequest = new UserController.CreateUserRequest(
                "newuser",
                "new@example.com",
                "password123"
        );
        log.debug("Создан запрос на создание пользователя: username=newuser");
        log.info("========== НАСТРОЙКА ЗАВЕРШЕНА ==========");
    }

    @Test
    @WithMockUser
    void getUsers_ShouldReturnList() throws Exception {
        log.info("========== ТЕСТ: getUsers_ShouldReturnList ==========");

        when(userService.listUsers()).thenReturn(Arrays.asList(testUser));
        log.debug("Mock возвращает список с 1 пользователем");

        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].username").value("testuser"))
                .andExpect(jsonPath("$[0].email").value("test@example.com"))
                .andDo(result -> log.debug("Ответ: {}", result.getResponse().getContentAsString()));

        log.info("========== ТЕСТ ПРОЙДЕН УСПЕШНО ==========");
    }

    @Test
    @WithMockUser
    void getUser_ExistingId_ShouldReturnUser() throws Exception {
        log.info("========== ТЕСТ: getUser_ExistingId_ShouldReturnUser ==========");

        when(userService.getUser(1L)).thenReturn(Optional.of(testUser));
        log.debug("Mock возвращает пользователя с ID 1");

        mockMvc.perform(get("/api/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("testuser"))
                .andDo(result -> log.debug("Ответ: {}", result.getResponse().getContentAsString()));

        log.info("========== ТЕСТ ПРОЙДЕН УСПЕШНО ==========");
    }

    @Test
    @WithMockUser
    void getUser_NonExistingId_ShouldReturnNotFound() throws Exception {
        log.info("========== ТЕСТ: getUser_NonExistingId_ShouldReturnNotFound ==========");

        when(userService.getUser(99L)).thenReturn(Optional.empty());
        log.debug("Mock возвращает пустой Optional для ID 99");

        mockMvc.perform(get("/api/users/99"))
                .andExpect(status().isNotFound())
                .andDo(result -> log.debug("Ответ: статус {}", result.getResponse().getStatus()));

        log.info("========== ТЕСТ ПРОЙДЕН УСПЕШНО ==========");
    }

    @Test
    @WithMockUser
    void createUser_ValidRequest_ShouldReturnCreated() throws Exception {
        log.info("========== ТЕСТ: createUser_ValidRequest_ShouldReturnCreated ==========");

        when(userService.createUser(any(User.class))).thenReturn(testUser);
        log.debug("Mock возвращает созданного пользователя");

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("testuser"))
                .andDo(result -> log.debug("Ответ: {}", result.getResponse().getContentAsString()));

        log.info("========== ТЕСТ ПРОЙДЕН УСПЕШНО ==========");
    }
}
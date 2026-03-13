package com.example.autoauction.admin.web;

import com.example.autoauction.admin.application.AdminAuctionService;
import com.example.autoauction.auction.domain.AuctionStatus;
import com.example.autoauction.auction.web.dto.AuctionCreateRequest;
import com.example.autoauction.auction.web.dto.AuctionResponse;
import com.example.autoauction.auth.infrastructure.security.JwtAuthenticationFilter;
import com.example.autoauction.auth.infrastructure.security.JwtService;
import com.example.autoauction.user.domain.port.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
        controllers = AdminAuctionController.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = JwtAuthenticationFilter.class
        )
)
@Import(TestSecurityConfig.class) // Добавляем тестовую конфигурацию безопасности
class AdminAuctionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AdminAuctionService adminAuctionService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private AuctionCreateRequest createRequest;
    private AuctionResponse auctionResponse;
    private final Long adminId = 1L;

    @BeforeEach
    void setUp() {
        OffsetDateTime startTime = OffsetDateTime.now().plusDays(1);
        OffsetDateTime endTime = startTime.plusDays(7);

        createRequest = new AuctionCreateRequest(
                1L,
                new BigDecimal("1000000"),
                new BigDecimal("1200000"),
                new BigDecimal("1500000"),
                new BigDecimal("10000"),
                startTime,
                endTime
        );

        auctionResponse = new AuctionResponse(
                1L,
                1L,
                "BMW M5 2023 (VIN: WBSDE91070CZ12345)",
                new BigDecimal("1000000"),
                new BigDecimal("1000000"),
                new BigDecimal("1200000"),
                new BigDecimal("1500000"),
                new BigDecimal("10000"),
                startTime,
                endTime,
                AuctionStatus.CREATED,
                0,
                null,
                null,
                adminId,
                OffsetDateTime.now()
        );
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createAuction_ShouldReturnCreated() throws Exception {
        // given
        when(adminAuctionService.createAuction(any(AuctionCreateRequest.class), eq(adminId)))
                .thenReturn(auctionResponse);

        // when/then
        mockMvc.perform(post("/api/admin/auctions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAllAuctions_ShouldReturnList() throws Exception {
        // given
        when(adminAuctionService.getAllAuctions()).thenReturn(List.of(auctionResponse));

        // when/then
        mockMvc.perform(get("/api/admin/auctions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAuction_ExistingId_ShouldReturnAuction() throws Exception {
        // given
        when(adminAuctionService.getAuction(1L)).thenReturn(auctionResponse);

        // when/then
        mockMvc.perform(get("/api/admin/auctions/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void startAuction_ShouldReturnOk() throws Exception {
        // given
        when(adminAuctionService.startAuction(1L, adminId)).thenReturn(auctionResponse);

        // when/then
        mockMvc.perform(post("/api/admin/auctions/1/start"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void cancelAuction_ShouldReturnOk() throws Exception {
        // given
        when(adminAuctionService.cancelAuction(1L, adminId)).thenReturn(auctionResponse);

        // when/then
        mockMvc.perform(post("/api/admin/auctions/1/cancel"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }
}
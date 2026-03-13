package com.example.autoauction.admin.application;

import com.example.autoauction.auction.domain.Auction;
import com.example.autoauction.auction.domain.AuctionStatus;
import com.example.autoauction.auction.domain.port.AuctionRepository;
import com.example.autoauction.auction.web.dto.AuctionCreateRequest;
import com.example.autoauction.auction.web.dto.AuctionResponse;
import com.example.autoauction.vehicle.application.VehicleService;
import com.example.autoauction.vehicle.domain.FuelType;
import com.example.autoauction.vehicle.domain.VehicleStatus;
import com.example.autoauction.vehicle.domain.VehicleType;
import com.example.autoauction.vehicle.web.dto.VehicleResponse;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@Slf4j
@ExtendWith(MockitoExtension.class)
class AdminAuctionServiceTest {

    @Mock
    private AuctionRepository auctionRepository;

    @Mock
    private VehicleService vehicleService;

    @InjectMocks
    private AdminAuctionService adminAuctionService;

    private AuctionCreateRequest validRequest;
    private VehicleResponse approvedVehicle;
    private Auction auction;
    private final Long adminId = 1L;
    private final Long diagnosticId = 16L;
    private final String diagnosticName = "diagnostic";

    @BeforeEach
    void setUp() {
        log.debug("========== НАСТРОЙКА ТЕСТОВ ==========");

        OffsetDateTime startTime = OffsetDateTime.now().plusDays(1);
        OffsetDateTime endTime = startTime.plusDays(7);
        OffsetDateTime now = OffsetDateTime.now();

        validRequest = new AuctionCreateRequest(
                1L,
                new BigDecimal("1000000"),
                new BigDecimal("1200000"),
                new BigDecimal("1500000"),
                new BigDecimal("10000"),
                startTime,
                endTime
        );
        log.debug("Создан AuctionCreateRequest: vehicleId=1, startingPrice=1000000");

        approvedVehicle = new VehicleResponse(
                1L,
                "BMW",
                "M5",
                2023,
                "WBSDE91070CZ12345",
                "A123BC",
                VehicleType.SEDAN,
                "black",
                10000,
                4.4,
                FuelType.PETROL,
                "auto",
                "Test car",
                VehicleStatus.APPROVED,
                diagnosticId,
                diagnosticName,
                adminId,
                "admin",
                null,
                now,
                now,
                now.minusDays(1),
                now
        );
        log.debug("Создан VehicleResponse: BMW M5, статус APPROVED");

        auction = new Auction(
                1L,
                new BigDecimal("1000000"),
                new BigDecimal("1200000"),
                new BigDecimal("1500000"),
                new BigDecimal("10000"),
                startTime,
                endTime,
                adminId
        );
        auction.setId(1L);
        log.debug("Создан Auction с ID: 1");
        log.debug("========== НАСТРОЙКА ЗАВЕРШЕНА ==========");
    }

    @Test
    void createAuction_Success() {
        log.info("========== ТЕСТ: createAuction_Success ==========");
        log.debug("Создаем аукцион с vehicleId: 1, adminId: {}", adminId);

        // given
        when(vehicleService.getVehicleDetails(1L)).thenReturn(approvedVehicle);
        when(auctionRepository.existsByVehicleIdAndStatus(1L, AuctionStatus.ACTIVE)).thenReturn(false);
        when(auctionRepository.existsByVehicleIdAndStatus(1L, AuctionStatus.CREATED)).thenReturn(false);
        when(auctionRepository.existsByVehicleIdAndStatus(1L, AuctionStatus.SOLD)).thenReturn(false);
        when(auctionRepository.save(any(Auction.class))).thenReturn(auction);

        log.debug("Mock настроены");

        // when
        log.debug("Вызываем adminAuctionService.createAuction()");
        AuctionResponse response = adminAuctionService.createAuction(validRequest, adminId);

        // then
        log.debug("Проверяем результат: response.vehicleId() = {}", response.vehicleId());
        assertNotNull(response);
        assertEquals(1L, response.vehicleId());
        assertEquals(new BigDecimal("1000000"), response.startingPrice());

        verify(auctionRepository, times(1)).save(any(Auction.class));
        log.debug("Проверка verify пройдена");
        log.info("========== ТЕСТ ПРОЙДЕН УСПЕШНО ==========");
    }

    @Test
    void createAuction_VehicleNotFound_ThrowsException() {
        log.info("========== ТЕСТ: createAuction_VehicleNotFound_ThrowsException ==========");

        // given
        when(vehicleService.getVehicleDetails(1L)).thenThrow(new IllegalArgumentException("Автомобиль с ID 1 не найден"));

        // when & then
        assertThrows(IllegalArgumentException.class, () ->
                adminAuctionService.createAuction(validRequest, adminId)
        );
        verify(auctionRepository, never()).save(any(Auction.class));

        log.debug("Исключение успешно поймано");
        log.info("========== ТЕСТ ПРОЙДЕН УСПЕШНО ==========");
    }

    @Test
    void createAuction_VehicleNotApproved_ThrowsException() {
        log.info("========== ТЕСТ: createAuction_VehicleNotApproved_ThrowsException ==========");

        // given
        OffsetDateTime now = OffsetDateTime.now();
        VehicleResponse notApprovedVehicle = new VehicleResponse(
                1L, "BMW", "M5", 2023, "WBSDE91070CZ12345",
                "A123BC", VehicleType.SEDAN, "black", 10000, 4.4,
                FuelType.PETROL, "auto", "Test car",
                VehicleStatus.DRAFT, diagnosticId, diagnosticName,
                null, null, null, now, now, null, null
        );
        when(vehicleService.getVehicleDetails(1L)).thenReturn(notApprovedVehicle);

        // when & then
        assertThrows(IllegalArgumentException.class, () ->
                adminAuctionService.createAuction(validRequest, adminId)
        );
        verify(auctionRepository, never()).save(any(Auction.class));

        log.debug("Исключение успешно поймано");
        log.info("========== ТЕСТ ПРОЙДЕН УСПЕШНО ==========");
    }

    @Test
    void createAuction_ActiveAuctionExists_ThrowsException() {
        log.info("========== ТЕСТ: createAuction_ActiveAuctionExists_ThrowsException ==========");

        // given
        when(vehicleService.getVehicleDetails(1L)).thenReturn(approvedVehicle);
        when(auctionRepository.existsByVehicleIdAndStatus(1L, AuctionStatus.ACTIVE)).thenReturn(true);

        // when & then
        assertThrows(IllegalArgumentException.class, () ->
                adminAuctionService.createAuction(validRequest, adminId)
        );
        verify(auctionRepository, never()).save(any(Auction.class));

        log.debug("Исключение успешно поймано");
        log.info("========== ТЕСТ ПРОЙДЕН УСПЕШНО ==========");
    }

    @Test
    void getAllAuctions_ShouldReturnList() {
        log.info("========== ТЕСТ: getAllAuctions_ShouldReturnList ==========");

        // given
        when(auctionRepository.findAll()).thenReturn(List.of(auction));

        // when
        List<AuctionResponse> responses = adminAuctionService.getAllAuctions();

        // then
        assertEquals(1, responses.size());
        assertEquals(1L, responses.get(0).id());

        log.debug("Получено аукционов: {}", responses.size());
        log.info("========== ТЕСТ ПРОЙДЕН УСПЕШНО ==========");
    }

    @Test
    void getAuction_ExistingId_ShouldReturnAuction() {
        log.info("========== ТЕСТ: getAuction_ExistingId_ShouldReturnAuction ==========");

        // given
        when(auctionRepository.findById(1L)).thenReturn(Optional.of(auction));

        // when
        AuctionResponse response = adminAuctionService.getAuction(1L);

        // then
        assertNotNull(response);
        assertEquals(1L, response.id());

        log.debug("Аукцион получен, ID: {}", response.id());
        log.info("========== ТЕСТ ПРОЙДЕН УСПЕШНО ==========");
    }

    @Test
    void getAuction_NonExistingId_ThrowsException() {
        log.info("========== ТЕСТ: getAuction_NonExistingId_ThrowsException ==========");

        // given
        when(auctionRepository.findById(99L)).thenReturn(Optional.empty());

        // when & then
        assertThrows(IllegalArgumentException.class, () ->
                adminAuctionService.getAuction(99L)
        );

        log.debug("Исключение успешно поймано");
        log.info("========== ТЕСТ ПРОЙДЕН УСПЕШНО ==========");
    }

    @Test
    void startAuction_Success() {
        log.info("========== ТЕСТ: startAuction_Success ==========");

        // given
        when(auctionRepository.findById(1L)).thenReturn(Optional.of(auction));
        when(auctionRepository.save(any(Auction.class))).thenReturn(auction);

        // when
        AuctionResponse response = adminAuctionService.startAuction(1L, adminId);

        // then
        assertNotNull(response);
        verify(auctionRepository, times(1)).save(auction);

        log.debug("Аукцион запущен, статус: {}", response.status());
        log.info("========== ТЕСТ ПРОЙДЕН УСПЕШНО ==========");
    }

    @Test
    void cancelAuction_Success() {
        log.info("========== ТЕСТ: cancelAuction_Success ==========");

        // given
        when(auctionRepository.findById(1L)).thenReturn(Optional.of(auction));
        when(auctionRepository.save(any(Auction.class))).thenReturn(auction);

        // when
        AuctionResponse response = adminAuctionService.cancelAuction(1L, adminId);

        // then
        assertNotNull(response);
        verify(auctionRepository, times(1)).save(auction);

        log.debug("Аукцион отменен, статус: {}", response.status());
        log.info("========== ТЕСТ ПРОЙДЕН УСПЕШНО ==========");
    }

    @Test
    void getAuctionsByStatus_ShouldReturnList() {
        log.info("========== ТЕСТ: getAuctionsByStatus_ShouldReturnList ==========");

        // given
        when(auctionRepository.findByStatus(AuctionStatus.CREATED)).thenReturn(List.of(auction));

        // when
        List<AuctionResponse> responses = adminAuctionService.getAuctionsByStatus("CREATED");

        // then
        assertEquals(1, responses.size());
        log.debug("Получено аукционов со статусом CREATED: {}", responses.size());
        log.info("========== ТЕСТ ПРОЙДЕН УСПЕШНО ==========");
    }

    @Test
    void getAuctionsByVehicleId_ShouldReturnList() {
        log.info("========== ТЕСТ: getAuctionsByVehicleId_ShouldReturnList ==========");

        // given
        when(auctionRepository.findByVehicleId(1L)).thenReturn(List.of(auction));

        // when
        List<AuctionResponse> responses = adminAuctionService.getAuctionsByVehicleId(1L);

        // then
        assertEquals(1, responses.size());
        assertEquals(1L, responses.get(0).vehicleId());

        log.debug("Получено аукционов для vehicleId=1: {}", responses.size());
        log.info("========== ТЕСТ ПРОЙДЕН УСПЕШНО ==========");
    }
}
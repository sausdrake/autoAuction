package com.example.autoauction.vehicle.application;

import com.example.autoauction.vehicle.domain.FuelType;
import com.example.autoauction.vehicle.domain.Vehicle;
import com.example.autoauction.vehicle.domain.VehicleStatus;
import com.example.autoauction.vehicle.domain.VehicleType;
import com.example.autoauction.vehicle.domain.port.VehicleRepository;
import com.example.autoauction.vehicle.web.dto.VehicleCreateRequest;
import com.example.autoauction.vehicle.web.dto.VehicleUpdateRequest;
import com.example.autoauction.vehicle.web.dto.VehicleResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VehicleServiceTest {

    @Mock
    private VehicleRepository vehicleRepository;

    @InjectMocks
    private VehicleService vehicleService;

    private VehicleCreateRequest createRequest;
    private VehicleUpdateRequest updateRequest;
    private Vehicle vehicle;
    private final Long diagnosticId = 16L;
    private final String diagnosticName = "diagnostic";
    private final Long adminId = 1L;
    private final String adminName = "admin";

    @BeforeEach
    void setUp() {
        createRequest = new VehicleCreateRequest(
                "BMW",
                "M5",
                2023,
                "WBSDE91070CZ12345",
                VehicleType.SEDAN,
                "A123BC",
                "black",
                10000,
                4.4,
                FuelType.PETROL,
                "auto",
                "Test car"
        );

        updateRequest = new VehicleUpdateRequest(
                "B456CD",
                "white",
                15000,
                4.4,
                FuelType.PETROL,
                "auto",
                "Updated description"
        );

        vehicle = new Vehicle(
                "BMW",
                "M5",
                2023,
                "WBSDE91070CZ12345",
                VehicleType.SEDAN,
                diagnosticId,
                diagnosticName
        );
        vehicle.setId(1L);
        vehicle.setLicensePlate("A123BC");
        vehicle.setColor("black");
        vehicle.setMileage(10000);
        vehicle.setEngineCapacity(4.4);
        vehicle.setFuelType(FuelType.PETROL);
        vehicle.setTransmission("auto");
        vehicle.setDescription("Test car");
    }

    @Test
    void createDraft_Success() {
        // given
        when(vehicleRepository.existsByVin(createRequest.vin())).thenReturn(false);
        when(vehicleRepository.save(any(Vehicle.class))).thenReturn(vehicle);

        // when
        VehicleResponse response = vehicleService.createDraft(createRequest, diagnosticId, diagnosticName);

        // then
        assertNotNull(response);
        assertEquals("BMW", response.brand());
        assertEquals("M5", response.model());
        assertEquals("WBSDE91070CZ12345", response.vin());
        assertEquals(VehicleStatus.DRAFT, response.status());
        assertEquals(diagnosticId, response.diagnosticId());
        assertEquals(diagnosticName, response.diagnosticName());
        verify(vehicleRepository, times(1)).save(any(Vehicle.class));
    }

    @Test
    void createDraft_VinExists_ThrowsException() {
        // given
        when(vehicleRepository.existsByVin(createRequest.vin())).thenReturn(true);

        // when & then
        assertThrows(IllegalArgumentException.class, () ->
                vehicleService.createDraft(createRequest, diagnosticId, diagnosticName)
        );
        verify(vehicleRepository, never()).save(any(Vehicle.class));
    }

    @Test
    void getMyVehicles_WithStatus_ShouldReturnFilteredList() {
        // given
        when(vehicleRepository.findByDiagnosticIdAndStatus(diagnosticId, VehicleStatus.DRAFT))
                .thenReturn(List.of(vehicle));

        // when
        List<VehicleResponse> responses = vehicleService.getMyVehicles(diagnosticId, VehicleStatus.DRAFT);

        // then
        assertEquals(1, responses.size());
        assertEquals("BMW", responses.get(0).brand());
        verify(vehicleRepository, times(1))
                .findByDiagnosticIdAndStatus(diagnosticId, VehicleStatus.DRAFT);
    }

    @Test
    void getMyVehicles_WithoutStatus_ShouldReturnAll() {
        // given
        when(vehicleRepository.findByDiagnosticId(diagnosticId)).thenReturn(List.of(vehicle));

        // when
        List<VehicleResponse> responses = vehicleService.getMyVehicles(diagnosticId, null);

        // then
        assertEquals(1, responses.size());
        verify(vehicleRepository, times(1)).findByDiagnosticId(diagnosticId);
    }

    @Test
    void getVehicle_Success() {
        // given
        when(vehicleRepository.findById(1L)).thenReturn(Optional.of(vehicle));

        // when
        VehicleResponse response = vehicleService.getVehicle(1L, diagnosticId);

        // then
        assertNotNull(response);
        assertEquals(1L, response.id());
    }

    @Test
    void getVehicle_NotFound_ThrowsException() {
        // given
        when(vehicleRepository.findById(99L)).thenReturn(Optional.empty());

        // when & then
        assertThrows(IllegalArgumentException.class, () ->
                vehicleService.getVehicle(99L, diagnosticId)
        );
    }

    @Test
    void getVehicle_WrongOwner_ThrowsException() {
        // given
        when(vehicleRepository.findById(1L)).thenReturn(Optional.of(vehicle));

        // when & then
        assertThrows(SecurityException.class, () ->
                vehicleService.getVehicle(1L, 99L)
        );
    }

    @Test
    void updateDraft_Success() {
        // given
        when(vehicleRepository.findById(1L)).thenReturn(Optional.of(vehicle));
        when(vehicleRepository.save(any(Vehicle.class))).thenReturn(vehicle);

        // when
        VehicleResponse response = vehicleService.updateDraft(1L, updateRequest, diagnosticId);

        // then
        assertNotNull(response);
        verify(vehicleRepository, times(1)).save(vehicle);
    }

    @Test
    void updateDraft_WhenNotDraft_ThrowsException() {
        // given
        vehicle.submitForReview(); // статус становится PENDING_REVIEW
        when(vehicleRepository.findById(1L)).thenReturn(Optional.of(vehicle));

        // when & then
        assertThrows(IllegalStateException.class, () ->
                vehicleService.updateDraft(1L, updateRequest, diagnosticId)
        );
    }

    @Test
    void submitForReview_Success() {
        // given
        when(vehicleRepository.findById(1L)).thenReturn(Optional.of(vehicle));
        when(vehicleRepository.save(any(Vehicle.class))).thenReturn(vehicle);

        // when
        VehicleResponse response = vehicleService.submitForReview(1L, diagnosticId);

        // then
        assertNotNull(response);
        assertEquals(VehicleStatus.PENDING_REVIEW, response.status());
        verify(vehicleRepository, times(1)).save(vehicle);
    }

    @Test
    void deleteDraft_Success() {
        // given
        when(vehicleRepository.findById(1L)).thenReturn(Optional.of(vehicle));
        doNothing().when(vehicleRepository).deleteById(1L);

        // when
        vehicleService.deleteDraft(1L, diagnosticId);

        // then
        verify(vehicleRepository, times(1)).deleteById(1L);
    }

    @Test
    void deleteDraft_WhenNotDraft_ThrowsException() {
        // given
        vehicle.submitForReview();
        when(vehicleRepository.findById(1L)).thenReturn(Optional.of(vehicle));

        // when & then
        assertThrows(IllegalStateException.class, () ->
                vehicleService.deleteDraft(1L, diagnosticId)
        );
    }

    @Test
    void getVehiclesByStatus_ShouldReturnList() {
        // given
        when(vehicleRepository.findByStatus(VehicleStatus.APPROVED)).thenReturn(List.of(vehicle));

        // when
        List<VehicleResponse> responses = vehicleService.getVehiclesByStatus(VehicleStatus.APPROVED);

        // then
        assertEquals(1, responses.size());
    }

    @Test
    void getVehicleDetails_Success() {
        // given
        when(vehicleRepository.findById(1L)).thenReturn(Optional.of(vehicle));

        // when
        VehicleResponse response = vehicleService.getVehicleDetails(1L);

        // then
        assertNotNull(response);
        assertEquals(1L, response.id());
    }

    @Test
    void approveVehicle_Success() {
        // given
        vehicle.submitForReview();
        when(vehicleRepository.findById(1L)).thenReturn(Optional.of(vehicle));
        when(vehicleRepository.save(any(Vehicle.class))).thenReturn(vehicle);

        // when
        VehicleResponse response = vehicleService.approveVehicle(1L, adminId, adminName);

        // then
        assertNotNull(response);
        assertEquals(VehicleStatus.APPROVED, response.status());
        assertEquals(adminId, response.adminId());
        assertEquals(adminName, response.adminName());
        verify(vehicleRepository, times(1)).save(vehicle);
    }

    @Test
    void rejectVehicle_Success() {
        // given
        vehicle.submitForReview();
        String reason = "Не хватает фотографий";
        when(vehicleRepository.findById(1L)).thenReturn(Optional.of(vehicle));
        when(vehicleRepository.save(any(Vehicle.class))).thenReturn(vehicle);

        // when
        VehicleResponse response = vehicleService.rejectVehicle(1L, adminId, adminName, reason);

        // then
        assertNotNull(response);
        assertEquals(VehicleStatus.NEEDS_FIXES, response.status());
        assertEquals(reason, response.rejectionReason());
        verify(vehicleRepository, times(1)).save(vehicle);
    }
}
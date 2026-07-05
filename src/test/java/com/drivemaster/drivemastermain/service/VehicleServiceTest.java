package com.drivemaster.drivemastermain.service;

import com.drivemaster.drivemastermain.dao.DealerProfileDao;
import com.drivemaster.drivemastermain.dao.VehicleDao;
import com.drivemaster.drivemastermain.dao.VehicleFeatureDao;
import com.drivemaster.drivemastermain.dao.tx.TransactionManager;
import com.drivemaster.drivemastermain.domain.DealerProfile;
import com.drivemaster.drivemastermain.domain.ListingType;
import com.drivemaster.drivemastermain.domain.Vehicle;
import com.drivemaster.drivemastermain.dto.VehicleCreateRequest;
import com.drivemaster.drivemastermain.dto.VehicleUpdateRequest;
import com.drivemaster.drivemastermain.exception.AccessDeniedOwnershipException;
import com.drivemaster.drivemastermain.exception.DealerNotApprovedException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class VehicleServiceTest {

    @Mock
    private VehicleDao vehicleDao;
    @Mock
    private VehicleFeatureDao vehicleFeatureDao;
    @Mock
    private DealerProfileDao dealerProfileDao;
    @Mock
    private TransactionManager transactionManager;

    private VehicleService vehicleService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        vehicleService = new VehicleService(vehicleDao, vehicleFeatureDao, dealerProfileDao, transactionManager);
    }

    @Test
    void createRejectsUnapprovedDealer() {
        when(dealerProfileDao.findByUserId(1L)).thenReturn(Optional.of(
                DealerProfile.builder().userId(1L).businessName("Bob Motors").build()));

        VehicleCreateRequest request = new VehicleCreateRequest("Civic", "Honda", "Civic", 2022, 100,
                "PETROL", BigDecimal.valueOf(20000), ListingType.SALE, null);

        assertThatThrownBy(() -> vehicleService.create(1L, request))
                .isInstanceOf(DealerNotApprovedException.class);

        verify(transactionManager, never()).executeInTransaction(any());
    }

    @Test
    void updateRejectsNonOwnerDealer() {
        Vehicle existing = Vehicle.builder()
                .id(10L).dealerId(1L).name("Civic").make("Honda").model("Civic")
                .year(2022).mileage(100).fuelType("PETROL").price(BigDecimal.valueOf(20000))
                .listingType(ListingType.SALE).createdAt(LocalDateTime.now()).build();
        when(vehicleDao.findById(10L)).thenReturn(Optional.of(existing));

        VehicleUpdateRequest request = new VehicleUpdateRequest("Civic", "Honda", "Civic", 2022, 100,
                "PETROL", BigDecimal.valueOf(21000), ListingType.SALE, null);

        assertThatThrownBy(() -> vehicleService.update(2L, 10L, request))
                .isInstanceOf(AccessDeniedOwnershipException.class);
    }
}

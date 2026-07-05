package com.drivemaster.drivemastermain.service.pricing;

import com.drivemaster.drivemastermain.domain.ListingType;
import com.drivemaster.drivemastermain.domain.Vehicle;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class DailyRateStrategyTest {

    @Test
    void calculatesPricePerDay() {
        Vehicle vehicle = Vehicle.builder()
                .dealerId(1L).name("Civic").make("Honda").model("Civic").year(2022)
                .mileage(100).fuelType("PETROL").price(BigDecimal.valueOf(50))
                .listingType(ListingType.RENT).build();

        BigDecimal price = new DailyRateStrategy()
                .calculatePrice(vehicle, LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 4));

        assertThat(price).isEqualByComparingTo("150");
    }
}

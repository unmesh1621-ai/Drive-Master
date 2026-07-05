package com.drivemaster.drivemastermain.service.pricing;

import com.drivemaster.drivemastermain.domain.ListingType;
import com.drivemaster.drivemastermain.domain.Vehicle;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class WeeklyDiscountStrategyTest {

    @Test
    void appliesDiscountForFullWeeksPlusRemainderDays() {
        Vehicle vehicle = Vehicle.builder()
                .dealerId(1L).name("Civic").make("Honda").model("Civic").year(2022)
                .mileage(100).fuelType("PETROL").price(BigDecimal.valueOf(100))
                .listingType(ListingType.RENT).build();

        // 9 days = 1 full week (100*7*0.9 = 630) + 2 remainder days (200) = 830
        BigDecimal price = new WeeklyDiscountStrategy()
                .calculatePrice(vehicle, LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 10));

        assertThat(price).isEqualByComparingTo("830.00");
    }
}

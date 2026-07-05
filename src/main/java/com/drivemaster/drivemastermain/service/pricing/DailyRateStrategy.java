package com.drivemaster.drivemastermain.service.pricing;

import com.drivemaster.drivemastermain.domain.Vehicle;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Component
public class DailyRateStrategy implements RentalPricingStrategy {
    @Override
    public BigDecimal calculatePrice(Vehicle vehicle, LocalDate start, LocalDate end) {
        long days = Math.max(1, ChronoUnit.DAYS.between(start, end));
        return vehicle.getPrice().multiply(BigDecimal.valueOf(days));
    }
}

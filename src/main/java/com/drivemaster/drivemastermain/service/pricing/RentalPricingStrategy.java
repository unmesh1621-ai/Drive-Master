package com.drivemaster.drivemastermain.service.pricing;

import com.drivemaster.drivemastermain.domain.Vehicle;

import java.math.BigDecimal;
import java.time.LocalDate;

public interface RentalPricingStrategy {
    BigDecimal calculatePrice(Vehicle vehicle, LocalDate start, LocalDate end);
}

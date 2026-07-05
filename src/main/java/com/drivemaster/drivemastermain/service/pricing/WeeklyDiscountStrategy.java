package com.drivemaster.drivemastermain.service.pricing;

import com.drivemaster.drivemastermain.domain.Vehicle;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Component
public class WeeklyDiscountStrategy implements RentalPricingStrategy {

    private static final BigDecimal WEEKLY_DISCOUNT_RATE = BigDecimal.valueOf(0.10);
    private static final int DAYS_PER_WEEK = 7;

    @Override
    public BigDecimal calculatePrice(Vehicle vehicle, LocalDate start, LocalDate end) {
        long totalDays = Math.max(1, ChronoUnit.DAYS.between(start, end));
        long fullWeeks = totalDays / DAYS_PER_WEEK;
        long remainderDays = totalDays % DAYS_PER_WEEK;

        BigDecimal dailyRate = vehicle.getPrice();
        BigDecimal discountedWeeklyRate = dailyRate
                .multiply(BigDecimal.valueOf(DAYS_PER_WEEK))
                .multiply(BigDecimal.ONE.subtract(WEEKLY_DISCOUNT_RATE));

        BigDecimal total = discountedWeeklyRate.multiply(BigDecimal.valueOf(fullWeeks))
                .add(dailyRate.multiply(BigDecimal.valueOf(remainderDays)));

        return total.setScale(2, RoundingMode.HALF_UP);
    }
}

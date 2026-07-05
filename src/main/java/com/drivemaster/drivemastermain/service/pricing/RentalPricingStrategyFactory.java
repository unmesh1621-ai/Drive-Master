package com.drivemaster.drivemastermain.service.pricing;

import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Component
public class RentalPricingStrategyFactory {

    private static final long WEEKLY_DISCOUNT_THRESHOLD_DAYS = 7;

    private final DailyRateStrategy dailyRateStrategy;
    private final WeeklyDiscountStrategy weeklyDiscountStrategy;

    public RentalPricingStrategyFactory(DailyRateStrategy dailyRateStrategy,
                                         WeeklyDiscountStrategy weeklyDiscountStrategy) {
        this.dailyRateStrategy = dailyRateStrategy;
        this.weeklyDiscountStrategy = weeklyDiscountStrategy;
    }

    public RentalPricingStrategy resolve(LocalDate start, LocalDate end) {
        long totalDays = Math.max(1, ChronoUnit.DAYS.between(start, end));
        return totalDays >= WEEKLY_DISCOUNT_THRESHOLD_DAYS ? weeklyDiscountStrategy : dailyRateStrategy;
    }
}

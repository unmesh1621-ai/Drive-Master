package com.drivemaster.drivemastermain.service.pricing;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class RentalPricingStrategyFactoryTest {

    private final DailyRateStrategy dailyRateStrategy = new DailyRateStrategy();
    private final WeeklyDiscountStrategy weeklyDiscountStrategy = new WeeklyDiscountStrategy();
    private final RentalPricingStrategyFactory factory =
            new RentalPricingStrategyFactory(dailyRateStrategy, weeklyDiscountStrategy);

    @Test
    void resolvesDailyStrategyUnderOneWeek() {
        RentalPricingStrategy strategy = factory.resolve(LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 4));
        assertThat(strategy).isSameAs(dailyRateStrategy);
    }

    @Test
    void resolvesWeeklyStrategyAtOneWeekOrMore() {
        RentalPricingStrategy strategy = factory.resolve(LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 8));
        assertThat(strategy).isSameAs(weeklyDiscountStrategy);
    }
}

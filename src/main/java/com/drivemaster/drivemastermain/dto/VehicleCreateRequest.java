package com.drivemaster.drivemastermain.dto;

import com.drivemaster.drivemastermain.domain.ListingType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record VehicleCreateRequest(
        @NotBlank String name,
        @NotBlank String make,
        @NotBlank String model,
        @Min(1900) int year,
        @Min(0) int mileage,
        @NotBlank String fuelType,
        @NotNull @DecimalMin(value = "0.01") BigDecimal price,
        @NotNull ListingType listingType,
        VehicleFeaturesRequest features
) {
}

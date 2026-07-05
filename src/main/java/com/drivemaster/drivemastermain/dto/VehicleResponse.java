package com.drivemaster.drivemastermain.dto;

import com.drivemaster.drivemastermain.domain.ListingType;
import com.drivemaster.drivemastermain.domain.Vehicle;
import com.drivemaster.drivemastermain.domain.VehicleStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record VehicleResponse(
        Long id,
        Long dealerId,
        String dealerBusinessName,
        String name,
        String make,
        String model,
        int year,
        int mileage,
        String fuelType,
        BigDecimal price,
        ListingType listingType,
        VehicleStatus status,
        LocalDateTime createdAt,
        VehicleFeaturesResponse features
) {
    public static VehicleResponse from(Vehicle vehicle, String dealerBusinessName) {
        return from(vehicle, dealerBusinessName, null);
    }

    public static VehicleResponse from(Vehicle vehicle, String dealerBusinessName, VehicleFeaturesResponse features) {
        return new VehicleResponse(
                vehicle.getId(),
                vehicle.getDealerId(),
                dealerBusinessName,
                vehicle.getName(),
                vehicle.getMake(),
                vehicle.getModel(),
                vehicle.getYear(),
                vehicle.getMileage(),
                vehicle.getFuelType(),
                vehicle.getPrice(),
                vehicle.getListingType(),
                vehicle.getStatus(),
                vehicle.getCreatedAt(),
                features
        );
    }
}

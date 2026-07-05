package com.drivemaster.drivemastermain.dto;

public record VehicleFeaturesRequest(
        String color,
        String engineCapacity,
        String transmission,
        String horsepower,
        Integer seatingCapacity,
        String safetyRating,
        boolean hasGps,
        boolean hasBluetooth,
        boolean hasSunroof
) {
}

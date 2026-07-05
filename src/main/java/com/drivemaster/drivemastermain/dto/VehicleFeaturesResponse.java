package com.drivemaster.drivemastermain.dto;

import com.drivemaster.drivemastermain.domain.VehicleFeatures;

public record VehicleFeaturesResponse(
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
    public static VehicleFeaturesResponse from(VehicleFeatures f) {
        return new VehicleFeaturesResponse(f.getColor(), f.getEngineCapacity(), f.getTransmission(),
                f.getHorsepower(), f.getSeatingCapacity(), f.getSafetyRating(),
                f.isHasGps(), f.isHasBluetooth(), f.isHasSunroof());
    }
}

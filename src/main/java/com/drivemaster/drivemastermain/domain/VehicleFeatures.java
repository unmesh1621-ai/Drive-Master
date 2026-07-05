package com.drivemaster.drivemastermain.domain;

import java.util.Objects;

public final class VehicleFeatures {
    private final Long vehicleId;
    private final String color;
    private final String engineCapacity;
    private final String transmission;
    private final String horsepower;
    private final Integer seatingCapacity;
    private final String safetyRating;
    private final boolean hasGps;
    private final boolean hasBluetooth;
    private final boolean hasSunroof;

    private VehicleFeatures(Builder builder) {
        this.vehicleId = builder.vehicleId;
        this.color = builder.color;
        this.engineCapacity = builder.engineCapacity;
        this.transmission = builder.transmission;
        this.horsepower = builder.horsepower;
        this.seatingCapacity = builder.seatingCapacity;
        this.safetyRating = builder.safetyRating;
        this.hasGps = builder.hasGps;
        this.hasBluetooth = builder.hasBluetooth;
        this.hasSunroof = builder.hasSunroof;
    }

    public Long getVehicleId() { return vehicleId; }
    public String getColor() { return color; }
    public String getEngineCapacity() { return engineCapacity; }
    public String getTransmission() { return transmission; }
    public String getHorsepower() { return horsepower; }
    public Integer getSeatingCapacity() { return seatingCapacity; }
    public String getSafetyRating() { return safetyRating; }
    public boolean isHasGps() { return hasGps; }
    public boolean isHasBluetooth() { return hasBluetooth; }
    public boolean isHasSunroof() { return hasSunroof; }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private Long vehicleId;
        private String color;
        private String engineCapacity;
        private String transmission;
        private String horsepower;
        private Integer seatingCapacity;
        private String safetyRating;
        private boolean hasGps;
        private boolean hasBluetooth;
        private boolean hasSunroof;

        public Builder vehicleId(Long vehicleId) { this.vehicleId = vehicleId; return this; }
        public Builder color(String color) { this.color = color; return this; }
        public Builder engineCapacity(String engineCapacity) { this.engineCapacity = engineCapacity; return this; }
        public Builder transmission(String transmission) { this.transmission = transmission; return this; }
        public Builder horsepower(String horsepower) { this.horsepower = horsepower; return this; }
        public Builder seatingCapacity(Integer seatingCapacity) { this.seatingCapacity = seatingCapacity; return this; }
        public Builder safetyRating(String safetyRating) { this.safetyRating = safetyRating; return this; }
        public Builder hasGps(boolean hasGps) { this.hasGps = hasGps; return this; }
        public Builder hasBluetooth(boolean hasBluetooth) { this.hasBluetooth = hasBluetooth; return this; }
        public Builder hasSunroof(boolean hasSunroof) { this.hasSunroof = hasSunroof; return this; }

        public VehicleFeatures build() {
            Objects.requireNonNull(vehicleId, "vehicleId is required");
            return new VehicleFeatures(this);
        }
    }
}

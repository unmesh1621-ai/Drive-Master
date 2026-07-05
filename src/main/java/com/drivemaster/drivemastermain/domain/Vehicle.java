package com.drivemaster.drivemastermain.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.Year;
import java.util.Objects;

public final class Vehicle {
    private final Long id;
    private final Long dealerId;
    private final String name;
    private final String make;
    private final String model;
    private final int year;
    private final int mileage;
    private final String fuelType;
    private final BigDecimal price;
    private final ListingType listingType;
    private final VehicleStatus status;
    private final LocalDateTime createdAt;

    private Vehicle(Builder builder) {
        this.id = builder.id;
        this.dealerId = builder.dealerId;
        this.name = builder.name;
        this.make = builder.make;
        this.model = builder.model;
        this.year = builder.year;
        this.mileage = builder.mileage;
        this.fuelType = builder.fuelType;
        this.price = builder.price;
        this.listingType = builder.listingType;
        this.status = builder.status;
        this.createdAt = builder.createdAt;
    }

    public Long getId() { return id; }
    public Long getDealerId() { return dealerId; }
    public String getName() { return name; }
    public String getMake() { return make; }
    public String getModel() { return model; }
    public int getYear() { return year; }
    public int getMileage() { return mileage; }
    public String getFuelType() { return fuelType; }
    public BigDecimal getPrice() { return price; }
    public ListingType getListingType() { return listingType; }
    public VehicleStatus getStatus() { return status; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    public static Builder builder() {
        return new Builder();
    }

    public Builder toBuilder() {
        return new Builder()
                .id(id)
                .dealerId(dealerId)
                .name(name)
                .make(make)
                .model(model)
                .year(year)
                .mileage(mileage)
                .fuelType(fuelType)
                .price(price)
                .listingType(listingType)
                .status(status)
                .createdAt(createdAt);
    }

    public static final class Builder {
        private Long id;
        private Long dealerId;
        private String name;
        private String make;
        private String model;
        private int year;
        private int mileage;
        private String fuelType;
        private BigDecimal price;
        private ListingType listingType;
        private VehicleStatus status = VehicleStatus.ACTIVE;
        private LocalDateTime createdAt;

        public Builder id(Long id) { this.id = id; return this; }
        public Builder dealerId(Long dealerId) { this.dealerId = dealerId; return this; }
        public Builder name(String name) { this.name = name; return this; }
        public Builder make(String make) { this.make = make; return this; }
        public Builder model(String model) { this.model = model; return this; }
        public Builder year(int year) { this.year = year; return this; }
        public Builder mileage(int mileage) { this.mileage = mileage; return this; }
        public Builder fuelType(String fuelType) { this.fuelType = fuelType; return this; }
        public Builder price(BigDecimal price) { this.price = price; return this; }
        public Builder listingType(ListingType listingType) { this.listingType = listingType; return this; }
        public Builder status(VehicleStatus status) { this.status = status; return this; }
        public Builder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }

        public Vehicle build() {
            Objects.requireNonNull(dealerId, "dealerId is required");
            Objects.requireNonNull(name, "name is required");
            Objects.requireNonNull(make, "make is required");
            Objects.requireNonNull(model, "model is required");
            Objects.requireNonNull(fuelType, "fuelType is required");
            Objects.requireNonNull(price, "price is required");
            Objects.requireNonNull(listingType, "listingType is required");
            if (price.signum() <= 0) {
                throw new IllegalArgumentException("price must be positive");
            }
            int currentYear = Year.now().getValue();
            if (year < 1900 || year > currentYear + 1) {
                throw new IllegalArgumentException("year is not plausible: " + year);
            }
            if (mileage < 0) {
                throw new IllegalArgumentException("mileage must not be negative");
            }
            if (createdAt == null) {
                createdAt = LocalDateTime.now();
            }
            return new Vehicle(this);
        }
    }
}

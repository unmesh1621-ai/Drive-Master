package com.drivemaster.drivemastermain.domain;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

public final class Order {
    private final Long id;
    private final Long userId;
    private final Long vehicleId;
    private final OrderType type;
    private final LocalDate rentalStart;
    private final LocalDate rentalEnd;
    private final BigDecimal totalPrice;
    private final OrderStatus status;
    private final LocalDateTime createdAt;

    private Order(Builder builder) {
        this.id = builder.id;
        this.userId = builder.userId;
        this.vehicleId = builder.vehicleId;
        this.type = builder.type;
        this.rentalStart = builder.rentalStart;
        this.rentalEnd = builder.rentalEnd;
        this.totalPrice = builder.totalPrice;
        this.status = builder.status;
        this.createdAt = builder.createdAt;
    }

    public Long getId() { return id; }
    public Long getUserId() { return userId; }
    public Long getVehicleId() { return vehicleId; }
    public OrderType getType() { return type; }
    public LocalDate getRentalStart() { return rentalStart; }
    public LocalDate getRentalEnd() { return rentalEnd; }
    public BigDecimal getTotalPrice() { return totalPrice; }
    public OrderStatus getStatus() { return status; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    public static Builder builder() {
        return new Builder();
    }

    public Builder toBuilder() {
        return new Builder()
                .id(id)
                .userId(userId)
                .vehicleId(vehicleId)
                .type(type)
                .rentalStart(rentalStart)
                .rentalEnd(rentalEnd)
                .totalPrice(totalPrice)
                .status(status)
                .createdAt(createdAt);
    }

    public static final class Builder {
        private Long id;
        private Long userId;
        private Long vehicleId;
        private OrderType type;
        private LocalDate rentalStart;
        private LocalDate rentalEnd;
        private BigDecimal totalPrice;
        private OrderStatus status = OrderStatus.PENDING;
        private LocalDateTime createdAt;

        public Builder id(Long id) { this.id = id; return this; }
        public Builder userId(Long userId) { this.userId = userId; return this; }
        public Builder vehicleId(Long vehicleId) { this.vehicleId = vehicleId; return this; }
        public Builder type(OrderType type) { this.type = type; return this; }
        public Builder rentalStart(LocalDate rentalStart) { this.rentalStart = rentalStart; return this; }
        public Builder rentalEnd(LocalDate rentalEnd) { this.rentalEnd = rentalEnd; return this; }
        public Builder totalPrice(BigDecimal totalPrice) { this.totalPrice = totalPrice; return this; }
        public Builder status(OrderStatus status) { this.status = status; return this; }
        public Builder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }

        public Order build() {
            Objects.requireNonNull(userId, "userId is required");
            Objects.requireNonNull(vehicleId, "vehicleId is required");
            Objects.requireNonNull(type, "type is required");
            Objects.requireNonNull(totalPrice, "totalPrice is required");
            boolean rentalFieldsSet = rentalStart != null && rentalEnd != null;
            boolean rentalFieldsAbsent = rentalStart == null && rentalEnd == null;
            if (!rentalFieldsSet && !rentalFieldsAbsent) {
                throw new IllegalArgumentException("rentalStart and rentalEnd must both be set or both be null");
            }
            if (type == OrderType.RENTAL && !rentalFieldsSet) {
                throw new IllegalArgumentException("rentalStart/rentalEnd are required for RENTAL orders");
            }
            if (type == OrderType.PURCHASE && rentalFieldsSet) {
                throw new IllegalArgumentException("rentalStart/rentalEnd must not be set for PURCHASE orders");
            }
            if (rentalFieldsSet && !rentalEnd.isAfter(rentalStart)) {
                throw new IllegalArgumentException("rentalEnd must be after rentalStart");
            }
            if (createdAt == null) {
                createdAt = LocalDateTime.now();
            }
            return new Order(this);
        }
    }
}

package com.drivemaster.drivemastermain.domain;

import java.time.LocalDateTime;
import java.util.Objects;

public final class Rating {
    private final Long id;
    private final Long orderId;
    private final Long userId;
    private final Long dealerId;
    private final int stars;
    private final String comment;
    private final LocalDateTime createdAt;

    private Rating(Builder builder) {
        this.id = builder.id;
        this.orderId = builder.orderId;
        this.userId = builder.userId;
        this.dealerId = builder.dealerId;
        this.stars = builder.stars;
        this.comment = builder.comment;
        this.createdAt = builder.createdAt;
    }

    public Long getId() { return id; }
    public Long getOrderId() { return orderId; }
    public Long getUserId() { return userId; }
    public Long getDealerId() { return dealerId; }
    public int getStars() { return stars; }
    public String getComment() { return comment; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    public static Builder builder() {
        return new Builder();
    }

    public Builder toBuilder() {
        return new Builder()
                .id(id).orderId(orderId).userId(userId).dealerId(dealerId)
                .stars(stars).comment(comment).createdAt(createdAt);
    }

    public static final class Builder {
        private Long id;
        private Long orderId;
        private Long userId;
        private Long dealerId;
        private int stars;
        private String comment;
        private LocalDateTime createdAt;

        public Builder id(Long id) { this.id = id; return this; }
        public Builder orderId(Long orderId) { this.orderId = orderId; return this; }
        public Builder userId(Long userId) { this.userId = userId; return this; }
        public Builder dealerId(Long dealerId) { this.dealerId = dealerId; return this; }
        public Builder stars(int stars) { this.stars = stars; return this; }
        public Builder comment(String comment) { this.comment = comment; return this; }
        public Builder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }

        public Rating build() {
            Objects.requireNonNull(orderId, "orderId is required");
            Objects.requireNonNull(userId, "userId is required");
            Objects.requireNonNull(dealerId, "dealerId is required");
            if (stars < 1 || stars > 5) {
                throw new IllegalArgumentException("stars must be between 1 and 5");
            }
            if (createdAt == null) {
                createdAt = LocalDateTime.now();
            }
            return new Rating(this);
        }
    }
}

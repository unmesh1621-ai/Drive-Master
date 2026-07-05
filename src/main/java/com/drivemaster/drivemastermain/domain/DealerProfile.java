package com.drivemaster.drivemastermain.domain;

import java.time.LocalDateTime;
import java.util.Objects;

public final class DealerProfile {
    private final Long id;
    private final Long userId;
    private final String businessName;
    private final LocalDateTime approvedAt;

    private DealerProfile(Builder builder) {
        this.id = builder.id;
        this.userId = builder.userId;
        this.businessName = builder.businessName;
        this.approvedAt = builder.approvedAt;
    }

    public Long getId() { return id; }
    public Long getUserId() { return userId; }
    public String getBusinessName() { return businessName; }
    public LocalDateTime getApprovedAt() { return approvedAt; }
    public boolean isApproved() { return approvedAt != null; }

    public static Builder builder() {
        return new Builder();
    }

    public Builder toBuilder() {
        return new Builder()
                .id(id)
                .userId(userId)
                .businessName(businessName)
                .approvedAt(approvedAt);
    }

    public static final class Builder {
        private Long id;
        private Long userId;
        private String businessName;
        private LocalDateTime approvedAt;

        public Builder id(Long id) { this.id = id; return this; }
        public Builder userId(Long userId) { this.userId = userId; return this; }
        public Builder businessName(String businessName) { this.businessName = businessName; return this; }
        public Builder approvedAt(LocalDateTime approvedAt) { this.approvedAt = approvedAt; return this; }

        public DealerProfile build() {
            Objects.requireNonNull(userId, "userId is required");
            Objects.requireNonNull(businessName, "businessName is required");
            if (businessName.isBlank()) {
                throw new IllegalArgumentException("businessName must not be blank");
            }
            return new DealerProfile(this);
        }
    }
}

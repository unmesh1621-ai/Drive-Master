package com.drivemaster.drivemastermain.dto;

import com.drivemaster.drivemastermain.domain.Rating;

import java.time.LocalDateTime;

public record RatingResponse(Long id, Long orderId, Long dealerId, int stars, String comment, LocalDateTime createdAt) {
    public static RatingResponse from(Rating rating) {
        return new RatingResponse(rating.getId(), rating.getOrderId(), rating.getDealerId(),
                rating.getStars(), rating.getComment(), rating.getCreatedAt());
    }
}

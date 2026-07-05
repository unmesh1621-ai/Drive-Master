package com.drivemaster.drivemastermain.dto;

import com.drivemaster.drivemastermain.domain.DealerProfile;

import java.time.LocalDateTime;

public record DealerProfileResponse(Long userId, String businessName, boolean approved, LocalDateTime approvedAt,
                                     double averageRating) {
    public static DealerProfileResponse from(DealerProfile profile, double averageRating) {
        return new DealerProfileResponse(profile.getUserId(), profile.getBusinessName(),
                profile.isApproved(), profile.getApprovedAt(), averageRating);
    }
}

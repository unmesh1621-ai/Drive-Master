package com.drivemaster.drivemastermain.dto;

import com.drivemaster.drivemastermain.domain.Role;
import com.drivemaster.drivemastermain.domain.User;
import com.drivemaster.drivemastermain.domain.UserStatus;

public record UserSummaryResponse(Long id, String name, String email, Role role, UserStatus status) {
    public static UserSummaryResponse from(User user) {
        return new UserSummaryResponse(user.getId(), user.getName(), user.getEmail(), user.getRole(), user.getStatus());
    }
}

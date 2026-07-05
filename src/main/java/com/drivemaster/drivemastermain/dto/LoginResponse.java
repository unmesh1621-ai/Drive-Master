package com.drivemaster.drivemastermain.dto;

import com.drivemaster.drivemastermain.domain.Role;

import java.time.Instant;

public record LoginResponse(String token, Role role, Instant expiresAt) {
}

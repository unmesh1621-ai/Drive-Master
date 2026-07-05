package com.drivemaster.drivemastermain.dto;

import com.drivemaster.drivemastermain.domain.OrderType;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record OrderCreateRequest(
        @NotNull Long vehicleId,
        @NotNull OrderType type,
        LocalDate rentalStart,
        LocalDate rentalEnd
) {
}

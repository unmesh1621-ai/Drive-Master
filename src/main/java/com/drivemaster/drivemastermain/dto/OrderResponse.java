package com.drivemaster.drivemastermain.dto;

import com.drivemaster.drivemastermain.domain.Order;
import com.drivemaster.drivemastermain.domain.OrderStatus;
import com.drivemaster.drivemastermain.domain.OrderType;
import com.drivemaster.drivemastermain.domain.Vehicle;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record OrderResponse(
        Long id,
        Long vehicleId,
        String vehicleName,
        OrderType type,
        LocalDate rentalStart,
        LocalDate rentalEnd,
        BigDecimal totalPrice,
        OrderStatus status,
        LocalDateTime createdAt
) {
    public static OrderResponse from(Order order, Vehicle vehicle) {
        return new OrderResponse(
                order.getId(),
                order.getVehicleId(),
                vehicle != null ? vehicle.getName() : null,
                order.getType(),
                order.getRentalStart(),
                order.getRentalEnd(),
                order.getTotalPrice(),
                order.getStatus(),
                order.getCreatedAt()
        );
    }
}

package com.drivemaster.drivemastermain.dto;

public record AdminStatsResponse(long totalUsers, long totalDealers, long approvedDealers,
                                  long activeVehicles, long totalOrders, long pendingOrders) {
}

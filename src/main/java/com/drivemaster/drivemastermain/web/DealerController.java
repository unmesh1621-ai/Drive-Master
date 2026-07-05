package com.drivemaster.drivemastermain.web;

import com.drivemaster.drivemastermain.dto.DealerProfileResponse;
import com.drivemaster.drivemastermain.dto.OrderResponse;
import com.drivemaster.drivemastermain.dto.VehicleResponse;
import com.drivemaster.drivemastermain.security.SecurityUtils;
import com.drivemaster.drivemastermain.service.DealerProfileService;
import com.drivemaster.drivemastermain.service.OrderService;
import com.drivemaster.drivemastermain.service.VehicleService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/dealer")
public class DealerController {

    private final DealerProfileService dealerProfileService;
    private final VehicleService vehicleService;
    private final OrderService orderService;

    public DealerController(DealerProfileService dealerProfileService, VehicleService vehicleService,
                             OrderService orderService) {
        this.dealerProfileService = dealerProfileService;
        this.vehicleService = vehicleService;
        this.orderService = orderService;
    }

    @GetMapping("/profile")
    public DealerProfileResponse myProfile() {
        return dealerProfileService.getMyProfile(SecurityUtils.currentUserId());
    }

    @GetMapping("/vehicles")
    public List<VehicleResponse> myVehicles() {
        return vehicleService.getByDealer(SecurityUtils.currentUserId());
    }

    @GetMapping("/orders")
    public List<OrderResponse> myOrders() {
        return orderService.getOrdersForDealer(SecurityUtils.currentUserId());
    }

    @PatchMapping("/orders/{id}/confirm")
    public OrderResponse confirmOrder(@PathVariable Long id) {
        return orderService.confirmOrder(SecurityUtils.currentUserId(), id);
    }

    @PatchMapping("/orders/{id}/reject")
    public OrderResponse rejectOrder(@PathVariable Long id) {
        return orderService.rejectOrder(SecurityUtils.currentUserId(), id);
    }

    @PatchMapping("/orders/{id}/fulfill")
    public OrderResponse fulfillOrder(@PathVariable Long id) {
        return orderService.fulfillOrder(SecurityUtils.currentUserId(), id);
    }
}

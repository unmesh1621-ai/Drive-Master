package com.drivemaster.drivemastermain.web;

import com.drivemaster.drivemastermain.dto.OrderCreateRequest;
import com.drivemaster.drivemastermain.dto.OrderResponse;
import com.drivemaster.drivemastermain.dto.RatingRequest;
import com.drivemaster.drivemastermain.dto.RatingResponse;
import com.drivemaster.drivemastermain.security.SecurityUtils;
import com.drivemaster.drivemastermain.service.OrderService;
import com.drivemaster.drivemastermain.service.RatingService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;
    private final RatingService ratingService;

    public OrderController(OrderService orderService, RatingService ratingService) {
        this.orderService = orderService;
        this.ratingService = ratingService;
    }

    @PostMapping
    public ResponseEntity<OrderResponse> placeOrder(@Valid @RequestBody OrderCreateRequest request) {
        OrderResponse response = orderService.placeOrder(SecurityUtils.currentUserId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/me")
    public List<OrderResponse> myOrders() {
        return orderService.getOrdersForUser(SecurityUtils.currentUserId());
    }

    @PostMapping("/{id}/rating")
    public ResponseEntity<RatingResponse> rateOrder(@PathVariable Long id, @Valid @RequestBody RatingRequest request) {
        RatingResponse response = ratingService.rateOrder(SecurityUtils.currentUserId(), id, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}

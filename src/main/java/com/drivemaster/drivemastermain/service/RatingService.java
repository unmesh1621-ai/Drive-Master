package com.drivemaster.drivemastermain.service;

import com.drivemaster.drivemastermain.dao.OrderDao;
import com.drivemaster.drivemastermain.dao.RatingDao;
import com.drivemaster.drivemastermain.dao.VehicleDao;
import com.drivemaster.drivemastermain.domain.Order;
import com.drivemaster.drivemastermain.domain.OrderStatus;
import com.drivemaster.drivemastermain.domain.Rating;
import com.drivemaster.drivemastermain.domain.Vehicle;
import com.drivemaster.drivemastermain.dto.RatingRequest;
import com.drivemaster.drivemastermain.dto.RatingResponse;
import com.drivemaster.drivemastermain.exception.AccessDeniedOwnershipException;
import com.drivemaster.drivemastermain.exception.ResourceNotFoundException;
import com.drivemaster.drivemastermain.exception.ValidationException;
import org.springframework.stereotype.Service;

@Service
public class RatingService {

    private final RatingDao ratingDao;
    private final OrderDao orderDao;
    private final VehicleDao vehicleDao;

    public RatingService(RatingDao ratingDao, OrderDao orderDao, VehicleDao vehicleDao) {
        this.ratingDao = ratingDao;
        this.orderDao = orderDao;
        this.vehicleDao = vehicleDao;
    }

    public RatingResponse rateOrder(Long userId, Long orderId, RatingRequest request) {
        Order order = orderDao.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + orderId));
        if (!order.getUserId().equals(userId)) {
            throw new AccessDeniedOwnershipException("Order does not belong to this user");
        }
        if (order.getStatus() != OrderStatus.FULFILLED) {
            throw new ValidationException("Only fulfilled orders can be rated");
        }
        if (ratingDao.findByOrderId(orderId).isPresent()) {
            throw new ValidationException("This order has already been rated");
        }
        Vehicle vehicle = vehicleDao.findById(order.getVehicleId())
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found: " + order.getVehicleId()));

        Rating rating = Rating.builder()
                .orderId(orderId)
                .userId(userId)
                .dealerId(vehicle.getDealerId())
                .stars(request.stars())
                .comment(request.comment())
                .build();

        Rating saved = ratingDao.insert(rating);
        return RatingResponse.from(saved);
    }
}

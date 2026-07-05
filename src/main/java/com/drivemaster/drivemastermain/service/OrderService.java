package com.drivemaster.drivemastermain.service;

import com.drivemaster.drivemastermain.dao.OrderDao;
import com.drivemaster.drivemastermain.dao.VehicleDao;
import com.drivemaster.drivemastermain.dao.tx.TransactionManager;
import com.drivemaster.drivemastermain.domain.Order;
import com.drivemaster.drivemastermain.domain.OrderStatus;
import com.drivemaster.drivemastermain.domain.OrderType;
import com.drivemaster.drivemastermain.domain.Vehicle;
import com.drivemaster.drivemastermain.domain.VehicleStatus;
import com.drivemaster.drivemastermain.dto.OrderCreateRequest;
import com.drivemaster.drivemastermain.dto.OrderResponse;
import com.drivemaster.drivemastermain.event.OrderEvent;
import com.drivemaster.drivemastermain.event.OrderEventPublisher;
import com.drivemaster.drivemastermain.exception.AccessDeniedOwnershipException;
import com.drivemaster.drivemastermain.exception.ResourceNotFoundException;
import com.drivemaster.drivemastermain.exception.ValidationException;
import com.drivemaster.drivemastermain.notification.NotificationService;
import com.drivemaster.drivemastermain.service.pricing.RentalPricingStrategy;
import com.drivemaster.drivemastermain.service.pricing.RentalPricingStrategyFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
public class OrderService {

    private final OrderDao orderDao;
    private final VehicleDao vehicleDao;
    private final TransactionManager transactionManager;
    private final RentalPricingStrategyFactory pricingStrategyFactory;
    private final OrderEventPublisher orderEventPublisher;
    private final NotificationService notificationService;

    public OrderService(OrderDao orderDao, VehicleDao vehicleDao, TransactionManager transactionManager,
                         RentalPricingStrategyFactory pricingStrategyFactory,
                         OrderEventPublisher orderEventPublisher, NotificationService notificationService) {
        this.orderDao = orderDao;
        this.vehicleDao = vehicleDao;
        this.transactionManager = transactionManager;
        this.pricingStrategyFactory = pricingStrategyFactory;
        this.orderEventPublisher = orderEventPublisher;
        this.notificationService = notificationService;
    }

    public OrderResponse placeOrder(Long userId, OrderCreateRequest request) {
        Vehicle vehicle = vehicleDao.findById(request.vehicleId())
                .filter(v -> v.getStatus() == VehicleStatus.ACTIVE)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found: " + request.vehicleId()));

        if (!vehicle.getListingType().supports(request.type())) {
            throw new ValidationException("This vehicle is not available for " + request.type());
        }

        BigDecimal totalPrice;
        LocalDate rentalStart = null;
        LocalDate rentalEnd = null;

        if (request.type() == OrderType.RENTAL) {
            rentalStart = request.rentalStart();
            rentalEnd = request.rentalEnd();
            if (rentalStart == null || rentalEnd == null) {
                throw new ValidationException("rentalStart and rentalEnd are required for a RENTAL order");
            }
            if (!rentalEnd.isAfter(rentalStart)) {
                throw new ValidationException("rentalEnd must be after rentalStart");
            }
            if (rentalStart.isBefore(LocalDate.now())) {
                throw new ValidationException("rentalStart must not be in the past");
            }
            if (orderDao.existsOverlappingRental(vehicle.getId(), rentalStart, rentalEnd)) {
                throw new ValidationException("This vehicle is already booked for an overlapping date range");
            }
            RentalPricingStrategy strategy = pricingStrategyFactory.resolve(rentalStart, rentalEnd);
            totalPrice = strategy.calculatePrice(vehicle, rentalStart, rentalEnd);
        } else {
            totalPrice = vehicle.getPrice();
        }

        Order order = Order.builder()
                .userId(userId)
                .vehicleId(vehicle.getId())
                .type(request.type())
                .rentalStart(rentalStart)
                .rentalEnd(rentalEnd)
                .totalPrice(totalPrice)
                .build();

        Order saved = transactionManager.executeInTransaction(conn -> {
            Order inserted = orderDao.insert(conn, order);
            if (request.type() == OrderType.PURCHASE) {
                vehicleDao.updateStatus(conn, vehicle.getId(), VehicleStatus.REMOVED);
            }
            return inserted;
        });

        orderEventPublisher.publish(new OrderEvent(saved));
        return OrderResponse.from(saved, vehicle);
    }

    public List<OrderResponse> getOrdersForUser(Long userId) {
        return orderDao.findByUserId(userId).stream()
                .map(order -> OrderResponse.from(order, vehicleDao.findById(order.getVehicleId()).orElse(null)))
                .toList();
    }

    public List<OrderResponse> getOrdersForDealer(Long dealerId) {
        return orderDao.findByDealerId(dealerId).stream()
                .map(order -> OrderResponse.from(order, vehicleDao.findById(order.getVehicleId()).orElse(null)))
                .toList();
    }

    public OrderResponse confirmOrder(Long dealerId, Long orderId) {
        return transitionOrder(dealerId, orderId, OrderStatus.PENDING, OrderStatus.CONFIRMED);
    }

    public OrderResponse rejectOrder(Long dealerId, Long orderId) {
        return transitionOrder(dealerId, orderId, OrderStatus.PENDING, OrderStatus.CANCELLED);
    }

    public OrderResponse fulfillOrder(Long dealerId, Long orderId) {
        return transitionOrder(dealerId, orderId, OrderStatus.CONFIRMED, OrderStatus.FULFILLED);
    }

    private OrderResponse transitionOrder(Long dealerId, Long orderId, OrderStatus expectedCurrent, OrderStatus next) {
        Order order = orderDao.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + orderId));
        Vehicle vehicle = vehicleDao.findById(order.getVehicleId())
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found: " + order.getVehicleId()));
        if (!vehicle.getDealerId().equals(dealerId)) {
            throw new AccessDeniedOwnershipException("Order does not belong to this dealer");
        }
        if (order.getStatus() != expectedCurrent) {
            throw new ValidationException("Order must be " + expectedCurrent + " to transition to " + next
                    + " (current status: " + order.getStatus() + ")");
        }
        orderDao.updateStatus(orderId, next);
        Order updated = order.toBuilder().status(next).build();
        notificationService.notifyUserOrderStatusChanged(updated);
        return OrderResponse.from(updated, vehicle);
    }
}

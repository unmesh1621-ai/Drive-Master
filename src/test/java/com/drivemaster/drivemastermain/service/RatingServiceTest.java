package com.drivemaster.drivemastermain.service;

import com.drivemaster.drivemastermain.dao.OrderDao;
import com.drivemaster.drivemastermain.dao.RatingDao;
import com.drivemaster.drivemastermain.dao.VehicleDao;
import com.drivemaster.drivemastermain.domain.ListingType;
import com.drivemaster.drivemastermain.domain.Order;
import com.drivemaster.drivemastermain.domain.OrderStatus;
import com.drivemaster.drivemastermain.domain.OrderType;
import com.drivemaster.drivemastermain.domain.Rating;
import com.drivemaster.drivemastermain.domain.Vehicle;
import com.drivemaster.drivemastermain.domain.VehicleStatus;
import com.drivemaster.drivemastermain.dto.RatingRequest;
import com.drivemaster.drivemastermain.exception.AccessDeniedOwnershipException;
import com.drivemaster.drivemastermain.exception.ValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

class RatingServiceTest {

    @Mock
    private RatingDao ratingDao;
    @Mock
    private OrderDao orderDao;
    @Mock
    private VehicleDao vehicleDao;

    private RatingService ratingService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        ratingService = new RatingService(ratingDao, orderDao, vehicleDao);
    }

    private Order order(Long id, Long userId, OrderStatus status) {
        return Order.builder().id(id).userId(userId).vehicleId(1L).type(OrderType.PURCHASE)
                .totalPrice(BigDecimal.valueOf(100)).status(status).createdAt(LocalDateTime.now()).build();
    }

    @Test
    void rejectsRatingSomeoneElsesOrder() {
        when(orderDao.findById(1L)).thenReturn(Optional.of(order(1L, 5L, OrderStatus.FULFILLED)));

        assertThatThrownBy(() -> ratingService.rateOrder(99L, 1L, new RatingRequest(5, "Great!")))
                .isInstanceOf(AccessDeniedOwnershipException.class);
    }

    @Test
    void rejectsRatingBeforeFulfillment() {
        when(orderDao.findById(1L)).thenReturn(Optional.of(order(1L, 5L, OrderStatus.PENDING)));

        assertThatThrownBy(() -> ratingService.rateOrder(5L, 1L, new RatingRequest(5, "Great!")))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void rejectsDuplicateRating() {
        when(orderDao.findById(1L)).thenReturn(Optional.of(order(1L, 5L, OrderStatus.FULFILLED)));
        when(ratingDao.findByOrderId(1L)).thenReturn(Optional.of(
                Rating.builder().id(1L).orderId(1L).userId(5L).dealerId(9L).stars(4).build()));

        assertThatThrownBy(() -> ratingService.rateOrder(5L, 1L, new RatingRequest(5, "Great!")))
                .isInstanceOf(ValidationException.class);
    }
}

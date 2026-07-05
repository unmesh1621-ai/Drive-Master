package com.drivemaster.drivemastermain.service;

import com.drivemaster.drivemastermain.dao.OrderDao;
import com.drivemaster.drivemastermain.dao.VehicleDao;
import com.drivemaster.drivemastermain.dao.tx.TransactionCallback;
import com.drivemaster.drivemastermain.dao.tx.TransactionManager;
import com.drivemaster.drivemastermain.domain.ListingType;
import com.drivemaster.drivemastermain.domain.Order;
import com.drivemaster.drivemastermain.domain.OrderType;
import com.drivemaster.drivemastermain.domain.Vehicle;
import com.drivemaster.drivemastermain.domain.VehicleStatus;
import com.drivemaster.drivemastermain.dto.OrderCreateRequest;
import com.drivemaster.drivemastermain.event.OrderEventPublisher;
import com.drivemaster.drivemastermain.exception.ResourceNotFoundException;
import com.drivemaster.drivemastermain.exception.ValidationException;
import com.drivemaster.drivemastermain.notification.NotificationService;
import com.drivemaster.drivemastermain.service.pricing.RentalPricingStrategyFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class OrderServiceTest {

    @Mock
    private OrderDao orderDao;
    @Mock
    private VehicleDao vehicleDao;
    @Mock
    private TransactionManager transactionManager;
    @Mock
    private RentalPricingStrategyFactory pricingStrategyFactory;
    @Mock
    private OrderEventPublisher orderEventPublisher;
    @Mock
    private NotificationService notificationService;

    private OrderService orderService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        orderService = new OrderService(orderDao, vehicleDao, transactionManager, pricingStrategyFactory,
                orderEventPublisher, notificationService);
    }

    private Vehicle sampleVehicle(ListingType listingType) {
        return Vehicle.builder()
                .id(1L).dealerId(9L).name("Civic").make("Honda").model("Civic")
                .year(2022).mileage(100).fuelType("PETROL").price(BigDecimal.valueOf(100))
                .listingType(listingType).status(VehicleStatus.ACTIVE).createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    void placeOrderRejectsMissingVehicle() {
        when(vehicleDao.findById(1L)).thenReturn(Optional.empty());

        OrderCreateRequest request = new OrderCreateRequest(1L, OrderType.PURCHASE, null, null);

        assertThatThrownBy(() -> orderService.placeOrder(2L, request))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void placeOrderRejectsRentalEndBeforeStart() {
        when(vehicleDao.findById(1L)).thenReturn(Optional.of(sampleVehicle(ListingType.RENT)));

        OrderCreateRequest request = new OrderCreateRequest(1L, OrderType.RENTAL,
                LocalDate.now().plusDays(5), LocalDate.now().plusDays(1));

        assertThatThrownBy(() -> orderService.placeOrder(2L, request))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void placeOrderRejectsOverlappingRental() {
        when(vehicleDao.findById(1L)).thenReturn(Optional.of(sampleVehicle(ListingType.RENT)));
        when(orderDao.existsOverlappingRental(eq(1L), any(), any())).thenReturn(true);

        OrderCreateRequest request = new OrderCreateRequest(1L, OrderType.RENTAL,
                LocalDate.now().plusDays(1), LocalDate.now().plusDays(3));

        assertThatThrownBy(() -> orderService.placeOrder(2L, request))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void placeOrderMarksVehicleSoldOnPurchaseAndPublishesEvent() {
        Vehicle vehicle = sampleVehicle(ListingType.SALE);
        when(vehicleDao.findById(1L)).thenReturn(Optional.of(vehicle));
        when(transactionManager.executeInTransaction(any())).thenAnswer(invocation -> {
            TransactionCallback<?> callback = invocation.getArgument(0);
            return callback.doInTransaction(null);
        });
        when(orderDao.insert(any(), any(Order.class)))
                .thenAnswer(invocation -> ((Order) invocation.getArgument(1)).toBuilder().id(100L).build());

        OrderCreateRequest request = new OrderCreateRequest(1L, OrderType.PURCHASE, null, null);

        orderService.placeOrder(2L, request);

        verify(vehicleDao).updateStatus(any(), eq(1L), eq(VehicleStatus.REMOVED));
        verify(orderEventPublisher).publish(any());
    }
}

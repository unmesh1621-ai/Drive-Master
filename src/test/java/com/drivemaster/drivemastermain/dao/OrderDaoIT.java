package com.drivemaster.drivemastermain.dao;

import com.drivemaster.drivemastermain.domain.ListingType;
import com.drivemaster.drivemastermain.domain.Order;
import com.drivemaster.drivemastermain.domain.OrderStatus;
import com.drivemaster.drivemastermain.domain.OrderType;
import com.drivemaster.drivemastermain.domain.Role;
import com.drivemaster.drivemastermain.domain.User;
import com.drivemaster.drivemastermain.domain.UserStatus;
import com.drivemaster.drivemastermain.domain.Vehicle;
import com.drivemaster.drivemastermain.domain.VehicleStatus;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class OrderDaoIT extends AbstractDaoIT {

    private final UserDao userDao = new UserDao(connectionPool);
    private final VehicleDao vehicleDao = new VehicleDao(connectionPool);
    private final OrderDao orderDao = new OrderDao(connectionPool);

    @Test
    void detectsOverlappingRentalsForSameVehicle() {
        Long dealerId = userDao.insert(User.builder().name("Dealer").email("dealer3@example.com")
                .passwordHash("hash").role(Role.DEALER).status(UserStatus.ACTIVE).build()).getId();
        Long userId = userDao.insert(User.builder().name("Renter").email("renter@example.com")
                .passwordHash("hash").role(Role.USER).status(UserStatus.ACTIVE).build()).getId();
        Long vehicleId = vehicleDao.insert(Vehicle.builder().dealerId(dealerId).name("Wrangler").make("Jeep")
                .model("Wrangler").year(2020).mileage(500).fuelType("PETROL").price(BigDecimal.valueOf(80))
                .listingType(ListingType.RENT).status(VehicleStatus.ACTIVE).build()).getId();

        orderDao.insert(Order.builder().userId(userId).vehicleId(vehicleId).type(OrderType.RENTAL)
                .rentalStart(LocalDate.of(2026, 8, 1)).rentalEnd(LocalDate.of(2026, 8, 10))
                .totalPrice(BigDecimal.valueOf(720)).status(OrderStatus.PENDING).build());

        boolean overlaps = orderDao.existsOverlappingRental(vehicleId,
                LocalDate.of(2026, 8, 5), LocalDate.of(2026, 8, 15));
        boolean noOverlap = orderDao.existsOverlappingRental(vehicleId,
                LocalDate.of(2026, 9, 1), LocalDate.of(2026, 9, 5));

        assertThat(overlaps).isTrue();
        assertThat(noOverlap).isFalse();
    }

    @Test
    void findsOrdersByDealerAcrossVehicles() {
        Long dealerId = userDao.insert(User.builder().name("Dealer").email("dealer4@example.com")
                .passwordHash("hash").role(Role.DEALER).status(UserStatus.ACTIVE).build()).getId();
        Long userId = userDao.insert(User.builder().name("Buyer").email("buyer@example.com")
                .passwordHash("hash").role(Role.USER).status(UserStatus.ACTIVE).build()).getId();
        Long vehicleId = vehicleDao.insert(Vehicle.builder().dealerId(dealerId).name("Civic").make("Honda")
                .model("Civic").year(2022).mileage(100).fuelType("PETROL").price(BigDecimal.valueOf(20000))
                .listingType(ListingType.SALE).status(VehicleStatus.ACTIVE).build()).getId();

        orderDao.insert(Order.builder().userId(userId).vehicleId(vehicleId).type(OrderType.PURCHASE)
                .totalPrice(BigDecimal.valueOf(20000)).status(OrderStatus.PENDING).build());

        assertThat(orderDao.findByDealerId(dealerId)).hasSize(1);
        assertThat(orderDao.countByStatus(OrderStatus.PENDING)).isEqualTo(1);
    }
}

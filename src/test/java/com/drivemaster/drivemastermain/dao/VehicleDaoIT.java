package com.drivemaster.drivemastermain.dao;

import com.drivemaster.drivemastermain.dao.support.PageResult;
import com.drivemaster.drivemastermain.dao.support.VehicleSearchCriteria;
import com.drivemaster.drivemastermain.domain.ListingType;
import com.drivemaster.drivemastermain.domain.Role;
import com.drivemaster.drivemastermain.domain.User;
import com.drivemaster.drivemastermain.domain.UserStatus;
import com.drivemaster.drivemastermain.domain.Vehicle;
import com.drivemaster.drivemastermain.domain.VehicleStatus;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class VehicleDaoIT extends AbstractDaoIT {

    private final UserDao userDao = new UserDao(connectionPool);
    private final VehicleDao vehicleDao = new VehicleDao(connectionPool);

    private Long insertDealer(String email) {
        return userDao.insert(User.builder()
                .name("Dealer")
                .email(email)
                .passwordHash("hash")
                .role(Role.DEALER)
                .status(UserStatus.ACTIVE)
                .build()).getId();
    }

    @Test
    void searchFiltersByMakeAndPaginates() {
        Long dealerId = insertDealer("dealer1@example.com");
        vehicleDao.insert(Vehicle.builder().dealerId(dealerId).name("Civic").make("Honda").model("Civic")
                .year(2022).mileage(100).fuelType("PETROL").price(BigDecimal.valueOf(20000))
                .listingType(ListingType.SALE).status(VehicleStatus.ACTIVE).build());
        vehicleDao.insert(Vehicle.builder().dealerId(dealerId).name("Corolla").make("Toyota").model("Corolla")
                .year(2021).mileage(200).fuelType("PETROL").price(BigDecimal.valueOf(18000))
                .listingType(ListingType.SALE).status(VehicleStatus.ACTIVE).build());

        VehicleSearchCriteria criteria = new VehicleSearchCriteria();
        criteria.setMake("Honda");
        criteria.setPage(0);
        criteria.setSize(10);

        PageResult<Vehicle> result = vehicleDao.search(criteria);

        assertThat(result.totalElements()).isEqualTo(1);
        assertThat(result.content()).extracting(Vehicle::getMake).containsExactly("Honda");
    }

    @Test
    void updateStatusMarksVehicleRemoved() {
        Long dealerId = insertDealer("dealer2@example.com");
        Vehicle saved = vehicleDao.insert(Vehicle.builder().dealerId(dealerId).name("Model3").make("Tesla")
                .model("Model 3").year(2023).mileage(10).fuelType("ELECTRIC").price(BigDecimal.valueOf(40000))
                .listingType(ListingType.SALE).status(VehicleStatus.ACTIVE).build());

        var conn = connectionPool.borrowConnection();
        try {
            vehicleDao.updateStatus(conn, saved.getId(), VehicleStatus.REMOVED);
        } finally {
            try {
                conn.close();
            } catch (Exception ignored) {
            }
        }

        assertThat(vehicleDao.findById(saved.getId()).orElseThrow().getStatus()).isEqualTo(VehicleStatus.REMOVED);
    }
}

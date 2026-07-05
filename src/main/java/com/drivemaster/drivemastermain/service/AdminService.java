package com.drivemaster.drivemastermain.service;

import com.drivemaster.drivemastermain.dao.DealerProfileDao;
import com.drivemaster.drivemastermain.dao.OrderDao;
import com.drivemaster.drivemastermain.dao.RatingDao;
import com.drivemaster.drivemastermain.dao.UserDao;
import com.drivemaster.drivemastermain.dao.VehicleDao;
import com.drivemaster.drivemastermain.dao.support.PageResult;
import com.drivemaster.drivemastermain.dao.tx.TransactionManager;
import com.drivemaster.drivemastermain.domain.DealerProfile;
import com.drivemaster.drivemastermain.domain.OrderStatus;
import com.drivemaster.drivemastermain.domain.Role;
import com.drivemaster.drivemastermain.domain.User;
import com.drivemaster.drivemastermain.domain.UserStatus;
import com.drivemaster.drivemastermain.domain.Vehicle;
import com.drivemaster.drivemastermain.domain.VehicleStatus;
import com.drivemaster.drivemastermain.dto.AdminStatsResponse;
import com.drivemaster.drivemastermain.dto.DealerProfileResponse;
import com.drivemaster.drivemastermain.dto.PageResponse;
import com.drivemaster.drivemastermain.dto.UserSummaryResponse;
import com.drivemaster.drivemastermain.dto.VehicleResponse;
import com.drivemaster.drivemastermain.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AdminService {

    private final UserDao userDao;
    private final DealerProfileDao dealerProfileDao;
    private final VehicleDao vehicleDao;
    private final OrderDao orderDao;
    private final RatingDao ratingDao;
    private final TransactionManager transactionManager;

    public AdminService(UserDao userDao, DealerProfileDao dealerProfileDao, VehicleDao vehicleDao,
                         OrderDao orderDao, RatingDao ratingDao, TransactionManager transactionManager) {
        this.userDao = userDao;
        this.dealerProfileDao = dealerProfileDao;
        this.vehicleDao = vehicleDao;
        this.orderDao = orderDao;
        this.ratingDao = ratingDao;
        this.transactionManager = transactionManager;
    }

    public List<DealerProfileResponse> listPendingDealers() {
        return dealerProfileDao.findPending().stream()
                .map(profile -> DealerProfileResponse.from(profile, ratingDao.averageForDealer(profile.getUserId())))
                .toList();
    }

    public void approveDealer(Long userId) {
        DealerProfile profile = dealerProfileDao.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Dealer profile not found for user: " + userId));
        User user = userDao.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));

        transactionManager.executeInTransaction(conn -> {
            dealerProfileDao.update(conn, profile.toBuilder().approvedAt(LocalDateTime.now()).build());
            userDao.update(conn, user.toBuilder().status(UserStatus.ACTIVE).build());
            return null;
        });
    }

    public void rejectDealer(Long userId) {
        dealerProfileDao.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Dealer profile not found for user: " + userId));
        User user = userDao.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));
        userDao.update(user.toBuilder().status(UserStatus.DISABLED).build());
    }

    public PageResponse<UserSummaryResponse> listUsers(int page, int size) {
        List<UserSummaryResponse> content = userDao.findAllPaged(page, size).stream()
                .map(UserSummaryResponse::from)
                .toList();
        return PageResponse.of(content, page, size, userDao.countAllUsers());
    }

    public void disableUser(Long userId) {
        User user = userDao.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));
        userDao.update(user.toBuilder().status(UserStatus.DISABLED).build());
    }

    public PageResponse<VehicleResponse> listVehiclesAdmin(int page, int size) {
        PageResult<Vehicle> result = vehicleDao.findAllPaged(page, size);
        List<VehicleResponse> content = result.content().stream()
                .map(v -> VehicleResponse.from(v, dealerProfileDao.findByUserId(v.getDealerId())
                        .map(DealerProfile::getBusinessName).orElse(null)))
                .toList();
        return PageResponse.of(content, page, size, result.totalElements());
    }

    public void removeVehicle(Long vehicleId) {
        Vehicle vehicle = vehicleDao.findById(vehicleId)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found: " + vehicleId));
        vehicleDao.update(vehicle.toBuilder().status(VehicleStatus.REMOVED).build());
    }

    public AdminStatsResponse getStats() {
        return new AdminStatsResponse(
                userDao.countByRole(Role.USER),
                userDao.countByRole(Role.DEALER),
                dealerProfileDao.countApproved(),
                vehicleDao.countActive(),
                orderDao.countAll(),
                orderDao.countByStatus(OrderStatus.PENDING)
        );
    }
}

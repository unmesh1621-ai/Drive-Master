package com.drivemaster.drivemastermain.service;

import com.drivemaster.drivemastermain.dao.DealerProfileDao;
import com.drivemaster.drivemastermain.dao.VehicleDao;
import com.drivemaster.drivemastermain.dao.VehicleFeatureDao;
import com.drivemaster.drivemastermain.dao.support.PageResult;
import com.drivemaster.drivemastermain.dao.support.VehicleSearchCriteria;
import com.drivemaster.drivemastermain.dao.tx.TransactionManager;
import com.drivemaster.drivemastermain.domain.DealerProfile;
import com.drivemaster.drivemastermain.domain.Vehicle;
import com.drivemaster.drivemastermain.domain.VehicleFeatures;
import com.drivemaster.drivemastermain.domain.VehicleStatus;
import com.drivemaster.drivemastermain.dto.PageResponse;
import com.drivemaster.drivemastermain.dto.VehicleCreateRequest;
import com.drivemaster.drivemastermain.dto.VehicleFeaturesRequest;
import com.drivemaster.drivemastermain.dto.VehicleFeaturesResponse;
import com.drivemaster.drivemastermain.dto.VehicleResponse;
import com.drivemaster.drivemastermain.dto.VehicleSearchRequest;
import com.drivemaster.drivemastermain.dto.VehicleUpdateRequest;
import com.drivemaster.drivemastermain.exception.AccessDeniedOwnershipException;
import com.drivemaster.drivemastermain.exception.DealerNotApprovedException;
import com.drivemaster.drivemastermain.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class VehicleService {

    private final VehicleDao vehicleDao;
    private final VehicleFeatureDao vehicleFeatureDao;
    private final DealerProfileDao dealerProfileDao;
    private final TransactionManager transactionManager;

    public VehicleService(VehicleDao vehicleDao, VehicleFeatureDao vehicleFeatureDao,
                           DealerProfileDao dealerProfileDao, TransactionManager transactionManager) {
        this.vehicleDao = vehicleDao;
        this.vehicleFeatureDao = vehicleFeatureDao;
        this.dealerProfileDao = dealerProfileDao;
        this.transactionManager = transactionManager;
    }

    public PageResponse<VehicleResponse> search(VehicleSearchRequest request) {
        VehicleSearchCriteria criteria = new VehicleSearchCriteria();
        criteria.setKeyword(request.getKeyword());
        criteria.setMake(request.getMake());
        criteria.setModel(request.getModel());
        criteria.setMinPrice(request.getMinPrice());
        criteria.setMaxPrice(request.getMaxPrice());
        criteria.setListingType(request.getListingType());
        criteria.setPage(request.getPage());
        criteria.setSize(request.getSize());

        PageResult<Vehicle> result = vehicleDao.search(criteria);
        List<VehicleResponse> content = result.content().stream()
                .map(this::toResponse)
                .toList();
        return PageResponse.of(content, criteria.getPage(), criteria.getSize(), result.totalElements());
    }

    public List<VehicleResponse> getByDealer(Long dealerId) {
        return vehicleDao.findByDealerId(dealerId).stream()
                .map(this::toResponse)
                .toList();
    }

    public VehicleResponse getById(Long id) {
        Vehicle vehicle = vehicleDao.findById(id)
                .filter(v -> v.getStatus() == VehicleStatus.ACTIVE)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found: " + id));
        return toResponse(vehicle);
    }

    public VehicleResponse create(Long dealerId, VehicleCreateRequest request) {
        requireApprovedDealer(dealerId);
        Vehicle vehicle = Vehicle.builder()
                .dealerId(dealerId)
                .name(request.name())
                .make(request.make())
                .model(request.model())
                .year(request.year())
                .mileage(request.mileage())
                .fuelType(request.fuelType())
                .price(request.price())
                .listingType(request.listingType())
                .status(VehicleStatus.ACTIVE)
                .build();

        Vehicle saved = transactionManager.executeInTransaction(conn -> {
            Vehicle inserted = vehicleDao.insert(conn, vehicle);
            if (request.features() != null) {
                vehicleFeatureDao.upsert(conn, toFeatures(inserted.getId(), request.features()));
            }
            return inserted;
        });
        return toResponse(saved);
    }

    public VehicleResponse update(Long dealerId, Long vehicleId, VehicleUpdateRequest request) {
        Vehicle existing = vehicleDao.findById(vehicleId)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found: " + vehicleId));
        requireOwnership(existing, dealerId);

        Vehicle updated = existing.toBuilder()
                .name(request.name())
                .make(request.make())
                .model(request.model())
                .year(request.year())
                .mileage(request.mileage())
                .fuelType(request.fuelType())
                .price(request.price())
                .listingType(request.listingType())
                .build();

        transactionManager.executeInTransaction(conn -> {
            vehicleDao.update(conn, updated);
            if (request.features() != null) {
                vehicleFeatureDao.upsert(conn, toFeatures(vehicleId, request.features()));
            }
            return null;
        });
        return toResponse(updated);
    }

    public void delete(Long dealerId, Long vehicleId) {
        Vehicle existing = vehicleDao.findById(vehicleId)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found: " + vehicleId));
        requireOwnership(existing, dealerId);
        vehicleDao.update(existing.toBuilder().status(VehicleStatus.REMOVED).build());
    }

    private void requireApprovedDealer(Long dealerId) {
        DealerProfile profile = dealerProfileDao.findByUserId(dealerId)
                .orElseThrow(() -> new DealerNotApprovedException("No dealer profile found for user " + dealerId));
        if (!profile.isApproved()) {
            throw new DealerNotApprovedException("Dealer account is pending admin approval");
        }
    }

    private void requireOwnership(Vehicle vehicle, Long dealerId) {
        if (!vehicle.getDealerId().equals(dealerId)) {
            throw new AccessDeniedOwnershipException("Vehicle does not belong to this dealer");
        }
    }

    private VehicleFeatures toFeatures(Long vehicleId, VehicleFeaturesRequest request) {
        return VehicleFeatures.builder()
                .vehicleId(vehicleId)
                .color(request.color())
                .engineCapacity(request.engineCapacity())
                .transmission(request.transmission())
                .horsepower(request.horsepower())
                .seatingCapacity(request.seatingCapacity())
                .safetyRating(request.safetyRating())
                .hasGps(request.hasGps())
                .hasBluetooth(request.hasBluetooth())
                .hasSunroof(request.hasSunroof())
                .build();
    }

    private VehicleResponse toResponse(Vehicle vehicle) {
        String dealerName = dealerProfileDao.findByUserId(vehicle.getDealerId())
                .map(DealerProfile::getBusinessName)
                .orElse(null);
        VehicleFeaturesResponse features = vehicleFeatureDao.findByVehicleId(vehicle.getId())
                .map(VehicleFeaturesResponse::from)
                .orElse(null);
        return VehicleResponse.from(vehicle, dealerName, features);
    }
}

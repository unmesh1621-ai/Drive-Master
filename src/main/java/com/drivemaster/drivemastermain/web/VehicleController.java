package com.drivemaster.drivemastermain.web;

import com.drivemaster.drivemastermain.dto.PageResponse;
import com.drivemaster.drivemastermain.dto.VehicleCreateRequest;
import com.drivemaster.drivemastermain.dto.VehicleResponse;
import com.drivemaster.drivemastermain.dto.VehicleSearchRequest;
import com.drivemaster.drivemastermain.dto.VehicleUpdateRequest;
import com.drivemaster.drivemastermain.security.SecurityUtils;
import com.drivemaster.drivemastermain.service.VehicleService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/vehicles")
public class VehicleController {

    private final VehicleService vehicleService;

    public VehicleController(VehicleService vehicleService) {
        this.vehicleService = vehicleService;
    }

    @GetMapping
    public PageResponse<VehicleResponse> search(VehicleSearchRequest request) {
        return vehicleService.search(request);
    }

    @GetMapping("/{id}")
    public VehicleResponse getById(@PathVariable Long id) {
        return vehicleService.getById(id);
    }

    @PostMapping
    public ResponseEntity<VehicleResponse> create(@Valid @RequestBody VehicleCreateRequest request) {
        VehicleResponse created = vehicleService.create(SecurityUtils.currentUserId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public VehicleResponse update(@PathVariable Long id, @Valid @RequestBody VehicleUpdateRequest request) {
        return vehicleService.update(SecurityUtils.currentUserId(), id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        vehicleService.delete(SecurityUtils.currentUserId(), id);
        return ResponseEntity.noContent().build();
    }
}

package com.drivemaster.drivemastermain.web;

import com.drivemaster.drivemastermain.dto.AdminStatsResponse;
import com.drivemaster.drivemastermain.dto.DealerProfileResponse;
import com.drivemaster.drivemastermain.dto.PageResponse;
import com.drivemaster.drivemastermain.dto.UserSummaryResponse;
import com.drivemaster.drivemastermain.dto.VehicleResponse;
import com.drivemaster.drivemastermain.service.AdminService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping("/dealers/pending")
    public List<DealerProfileResponse> pendingDealers() {
        return adminService.listPendingDealers();
    }

    @PostMapping("/dealers/{id}/approve")
    public ResponseEntity<Void> approveDealer(@PathVariable Long id) {
        adminService.approveDealer(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/dealers/{id}/reject")
    public ResponseEntity<Void> rejectDealer(@PathVariable Long id) {
        adminService.rejectDealer(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/users")
    public PageResponse<UserSummaryResponse> listUsers(@RequestParam(defaultValue = "0") int page,
                                                        @RequestParam(defaultValue = "20") int size) {
        return adminService.listUsers(page, size);
    }

    @PatchMapping("/users/{id}/disable")
    public ResponseEntity<Void> disableUser(@PathVariable Long id) {
        adminService.disableUser(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/vehicles")
    public PageResponse<VehicleResponse> listVehicles(@RequestParam(defaultValue = "0") int page,
                                                       @RequestParam(defaultValue = "20") int size) {
        return adminService.listVehiclesAdmin(page, size);
    }

    @DeleteMapping("/vehicles/{id}")
    public ResponseEntity<Void> removeVehicle(@PathVariable Long id) {
        adminService.removeVehicle(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/stats")
    public AdminStatsResponse stats() {
        return adminService.getStats();
    }
}

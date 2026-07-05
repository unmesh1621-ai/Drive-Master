package com.drivemaster.drivemastermain.web;

import com.drivemaster.drivemastermain.dto.LoginRequest;
import com.drivemaster.drivemastermain.dto.LoginResponse;
import com.drivemaster.drivemastermain.dto.RegisterDealerRequest;
import com.drivemaster.drivemastermain.dto.RegisterUserRequest;
import com.drivemaster.drivemastermain.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<Void> register(@Valid @RequestBody RegisterUserRequest request) {
        authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping("/register/dealer")
    public ResponseEntity<Void> registerDealer(@Valid @RequestBody RegisterDealerRequest request) {
        authService.registerDealer(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }
}

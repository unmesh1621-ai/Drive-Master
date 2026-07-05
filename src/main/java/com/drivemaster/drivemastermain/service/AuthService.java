package com.drivemaster.drivemastermain.service;

import com.drivemaster.drivemastermain.config.AppProperties;
import com.drivemaster.drivemastermain.dao.DealerProfileDao;
import com.drivemaster.drivemastermain.dao.UserDao;
import com.drivemaster.drivemastermain.dao.tx.TransactionManager;
import com.drivemaster.drivemastermain.domain.DealerProfile;
import com.drivemaster.drivemastermain.domain.Role;
import com.drivemaster.drivemastermain.domain.User;
import com.drivemaster.drivemastermain.domain.UserStatus;
import com.drivemaster.drivemastermain.dto.LoginRequest;
import com.drivemaster.drivemastermain.dto.LoginResponse;
import com.drivemaster.drivemastermain.dto.RegisterDealerRequest;
import com.drivemaster.drivemastermain.dto.RegisterUserRequest;
import com.drivemaster.drivemastermain.exception.DuplicateResourceException;
import com.drivemaster.drivemastermain.security.CustomUserDetails;
import com.drivemaster.drivemastermain.security.JwtService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Service
public class AuthService {

    private final UserDao userDao;
    private final DealerProfileDao dealerProfileDao;
    private final TransactionManager transactionManager;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final long jwtExpirationMs;

    public AuthService(UserDao userDao, DealerProfileDao dealerProfileDao, TransactionManager transactionManager,
                        PasswordEncoder passwordEncoder, AuthenticationManager authenticationManager,
                        JwtService jwtService, AppProperties props) {
        this.userDao = userDao;
        this.dealerProfileDao = dealerProfileDao;
        this.transactionManager = transactionManager;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.jwtExpirationMs = props.getJwt().getExpirationMs();
    }

    public void register(RegisterUserRequest request) {
        if (userDao.existsByEmail(request.email())) {
            throw new DuplicateResourceException("Email already registered: " + request.email());
        }
        User user = User.builder()
                .name(request.name())
                .email(request.email())
                .passwordHash(passwordEncoder.encode(request.password()))
                .role(Role.USER)
                .status(UserStatus.ACTIVE)
                .build();
        userDao.insert(user);
    }

    public void registerDealer(RegisterDealerRequest request) {
        if (userDao.existsByEmail(request.email())) {
            throw new DuplicateResourceException("Email already registered: " + request.email());
        }
        transactionManager.executeInTransaction(conn -> {
            User user = User.builder()
                    .name(request.name())
                    .email(request.email())
                    .passwordHash(passwordEncoder.encode(request.password()))
                    .role(Role.DEALER)
                    .status(UserStatus.PENDING)
                    .build();
            User saved = userDao.insert(conn, user);

            DealerProfile profile = DealerProfile.builder()
                    .userId(saved.getId())
                    .businessName(request.businessName())
                    .build();
            dealerProfileDao.insert(conn, profile);
            return saved;
        });
    }

    public LoginResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password()));
        CustomUserDetails principal = (CustomUserDetails) authentication.getPrincipal();
        String token = jwtService.generateToken(principal);
        Instant expiresAt = Instant.now().plus(jwtExpirationMs, ChronoUnit.MILLIS);
        return new LoginResponse(token, principal.getUser().getRole(), expiresAt);
    }
}

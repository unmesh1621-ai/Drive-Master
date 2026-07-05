package com.drivemaster.drivemastermain.service;

import com.drivemaster.drivemastermain.config.AppProperties;
import com.drivemaster.drivemastermain.dao.DealerProfileDao;
import com.drivemaster.drivemastermain.dao.UserDao;
import com.drivemaster.drivemastermain.dao.tx.TransactionCallback;
import com.drivemaster.drivemastermain.dao.tx.TransactionManager;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AuthServiceTest {

    @Mock
    private UserDao userDao;
    @Mock
    private DealerProfileDao dealerProfileDao;
    @Mock
    private TransactionManager transactionManager;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private JwtService jwtService;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        AppProperties props = new AppProperties();
        props.getJwt().setExpirationMs(3_600_000L);
        authService = new AuthService(userDao, dealerProfileDao, transactionManager, passwordEncoder,
                authenticationManager, jwtService, props);
    }

    @Test
    void registerRejectsDuplicateEmail() {
        when(userDao.existsByEmail("taken@example.com")).thenReturn(true);

        RegisterUserRequest request = new RegisterUserRequest("Alice", "taken@example.com", "password123");

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(DuplicateResourceException.class);

        verify(userDao, never()).insert(any(User.class));
    }

    @Test
    void registerDealerInsertsUserAndProfileInOneTransaction() {
        when(userDao.existsByEmail("dealer@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("hashed");
        when(userDao.insert(any(), any(User.class)))
                .thenAnswer(invocation -> ((User) invocation.getArgument(1)).toBuilder().id(1L).build());
        when(transactionManager.executeInTransaction(any())).thenAnswer(invocation -> {
            TransactionCallback<?> callback = invocation.getArgument(0);
            return callback.doInTransaction(null);
        });

        RegisterDealerRequest request = new RegisterDealerRequest("Bob", "dealer@example.com", "password123", "Bob Motors");

        authService.registerDealer(request);

        verify(transactionManager).executeInTransaction(any());
        verify(userDao).insert(any(), any(User.class));
        verify(dealerProfileDao).insert(any(), any());
    }

    @Test
    void loginGeneratesTokenForAuthenticatedUser() {
        User user = User.builder()
                .id(5L).name("Carol").email("carol@example.com").passwordHash("hash")
                .role(Role.USER).status(UserStatus.ACTIVE).build();
        CustomUserDetails principal = new CustomUserDetails(user);
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(principal);
        when(authenticationManager.authenticate(any())).thenReturn(authentication);
        when(jwtService.generateToken(principal)).thenReturn("jwt-token");

        LoginResponse response = authService.login(new LoginRequest("carol@example.com", "password123"));

        assertThat(response.token()).isEqualTo("jwt-token");
        assertThat(response.role()).isEqualTo(Role.USER);
    }
}

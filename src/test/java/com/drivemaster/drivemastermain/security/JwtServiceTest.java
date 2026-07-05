package com.drivemaster.drivemastermain.security;

import com.drivemaster.drivemastermain.config.AppProperties;
import com.drivemaster.drivemastermain.domain.Role;
import com.drivemaster.drivemastermain.domain.User;
import com.drivemaster.drivemastermain.domain.UserStatus;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class JwtServiceTest {

    private AppProperties props(long expirationMs) {
        AppProperties props = new AppProperties();
        props.getJwt().setSecret("test-secret-key-must-be-at-least-32-bytes-long!!");
        props.getJwt().setExpirationMs(expirationMs);
        return props;
    }

    private CustomUserDetails principal() {
        User user = User.builder()
                .id(1L).name("Alice").email("alice@example.com").passwordHash("hash")
                .role(Role.USER).status(UserStatus.ACTIVE).build();
        return new CustomUserDetails(user);
    }

    @Test
    void generatesAndValidatesToken() {
        JwtService jwtService = new JwtService(props(3_600_000L));
        CustomUserDetails principal = principal();

        String token = jwtService.generateToken(principal);

        assertThat(jwtService.extractUsername(token)).isEqualTo("alice@example.com");
        assertThat(jwtService.extractUserId(token)).isEqualTo(1L);
        assertThat(jwtService.extractRole(token)).isEqualTo(Role.USER);
        assertThat(jwtService.isTokenValid(token, principal)).isTrue();
    }

    @Test
    void rejectsExpiredToken() throws InterruptedException {
        JwtService jwtService = new JwtService(props(1L));
        CustomUserDetails principal = principal();

        String token = jwtService.generateToken(principal);
        Thread.sleep(10);

        assertThat(jwtService.isTokenValid(token, principal)).isFalse();
    }
}

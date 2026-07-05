package com.drivemaster.drivemastermain.dao;

import com.drivemaster.drivemastermain.domain.Role;
import com.drivemaster.drivemastermain.domain.User;
import com.drivemaster.drivemastermain.domain.UserStatus;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UserDaoIT extends AbstractDaoIT {

    private final UserDao userDao = new UserDao(connectionPool);

    @Test
    void insertsAndFindsByEmail() {
        User user = User.builder()
                .name("Alice")
                .email("alice@example.com")
                .passwordHash("hash")
                .role(Role.USER)
                .status(UserStatus.ACTIVE)
                .build();

        User saved = userDao.insert(user);

        assertThat(saved.getId()).isNotNull();
        assertThat(userDao.findByEmail("alice@example.com")).isPresent();
        assertThat(userDao.existsByEmail("alice@example.com")).isTrue();
        assertThat(userDao.countByRole(Role.USER)).isEqualTo(1);
    }

    @Test
    void updatesStatus() {
        User saved = userDao.insert(User.builder()
                .name("Dealer Dan")
                .email("dan@example.com")
                .passwordHash("hash")
                .role(Role.DEALER)
                .status(UserStatus.PENDING)
                .build());

        userDao.update(saved.toBuilder().status(UserStatus.ACTIVE).build());

        User reloaded = userDao.findById(saved.getId()).orElseThrow();
        assertThat(reloaded.getStatus()).isEqualTo(UserStatus.ACTIVE);
    }
}

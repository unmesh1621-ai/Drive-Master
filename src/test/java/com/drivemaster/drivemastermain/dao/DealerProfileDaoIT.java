package com.drivemaster.drivemastermain.dao;

import com.drivemaster.drivemastermain.domain.DealerProfile;
import com.drivemaster.drivemastermain.domain.Role;
import com.drivemaster.drivemastermain.domain.User;
import com.drivemaster.drivemastermain.domain.UserStatus;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class DealerProfileDaoIT extends AbstractDaoIT {

    private final UserDao userDao = new UserDao(connectionPool);
    private final DealerProfileDao dealerProfileDao = new DealerProfileDao(connectionPool);

    @Test
    void findsPendingDealersAndTracksApproval() {
        User dealerUser = userDao.insert(User.builder()
                .name("Dealer Dan")
                .email("dan@example.com")
                .passwordHash("hash")
                .role(Role.DEALER)
                .status(UserStatus.PENDING)
                .build());

        DealerProfile profile = dealerProfileDao.insert(DealerProfile.builder()
                .userId(dealerUser.getId())
                .businessName("Dan's Motors")
                .build());

        assertThat(dealerProfileDao.findPending()).extracting(DealerProfile::getUserId)
                .contains(dealerUser.getId());
        assertThat(dealerProfileDao.countApproved()).isEqualTo(0);

        dealerProfileDao.update(profile.toBuilder().approvedAt(LocalDateTime.now()).build());

        assertThat(dealerProfileDao.countApproved()).isEqualTo(1);
        assertThat(dealerProfileDao.findByUserId(dealerUser.getId()).orElseThrow().isApproved()).isTrue();
    }
}

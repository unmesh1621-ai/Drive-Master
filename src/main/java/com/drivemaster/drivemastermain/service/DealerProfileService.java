package com.drivemaster.drivemastermain.service;

import com.drivemaster.drivemastermain.dao.DealerProfileDao;
import com.drivemaster.drivemastermain.dao.RatingDao;
import com.drivemaster.drivemastermain.domain.DealerProfile;
import com.drivemaster.drivemastermain.dto.DealerProfileResponse;
import com.drivemaster.drivemastermain.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class DealerProfileService {

    private final DealerProfileDao dealerProfileDao;
    private final RatingDao ratingDao;

    public DealerProfileService(DealerProfileDao dealerProfileDao, RatingDao ratingDao) {
        this.dealerProfileDao = dealerProfileDao;
        this.ratingDao = ratingDao;
    }

    public DealerProfileResponse getMyProfile(Long userId) {
        DealerProfile profile = dealerProfileDao.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("No dealer profile found for user " + userId));
        return DealerProfileResponse.from(profile, ratingDao.averageForDealer(userId));
    }
}

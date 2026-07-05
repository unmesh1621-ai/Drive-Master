package com.drivemaster.drivemastermain.config;

import com.drivemaster.drivemastermain.dao.UserDao;
import com.drivemaster.drivemastermain.domain.Role;
import com.drivemaster.drivemastermain.domain.User;
import com.drivemaster.drivemastermain.domain.UserStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@Order(2)
public class AdminBootstrap implements ApplicationRunner {
    private static final Logger log = LoggerFactory.getLogger(AdminBootstrap.class);

    private final UserDao userDao;
    private final PasswordEncoder passwordEncoder;
    private final AppProperties props;

    public AdminBootstrap(UserDao userDao, PasswordEncoder passwordEncoder, AppProperties props) {
        this.userDao = userDao;
        this.passwordEncoder = passwordEncoder;
        this.props = props;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (userDao.countByRole(Role.ADMIN) > 0) {
            return;
        }

        AppProperties.Admin adminProps = props.getAdmin();
        User admin = User.builder()
                .name("Platform Admin")
                .email(adminProps.getBootstrapEmail())
                .passwordHash(passwordEncoder.encode(adminProps.getBootstrapPassword()))
                .role(Role.ADMIN)
                .status(UserStatus.ACTIVE)
                .build();
        userDao.insert(admin);
        log.warn("Bootstrap admin account created: {} -- change the password immediately",
                adminProps.getBootstrapEmail());
    }
}

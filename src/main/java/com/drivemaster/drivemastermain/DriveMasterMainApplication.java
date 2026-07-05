package com.drivemaster.drivemastermain;

import com.drivemaster.drivemastermain.config.AppProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(AppProperties.class)
public class DriveMasterMainApplication {
    public static void main(String[] args) {
        SpringApplication.run(DriveMasterMainApplication.class, args);
    }
}

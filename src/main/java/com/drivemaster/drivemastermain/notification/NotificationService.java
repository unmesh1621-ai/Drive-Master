package com.drivemaster.drivemastermain.notification;

import com.drivemaster.drivemastermain.domain.Order;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutorService;

@Service
public class NotificationService {
    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    private final ExecutorService notificationExecutor;

    public NotificationService(ExecutorService notificationExecutor) {
        this.notificationExecutor = notificationExecutor;
    }

    public void notifyDealerOfNewOrder(Order order) {
        notificationExecutor.submit(() -> {
            simulateLatency();
            log.info("[NOTIFICATION] Dealer notified: new {} order #{} placed for vehicle {}",
                    order.getType(), order.getId(), order.getVehicleId());
        });
    }

    public void notifyUserOrderStatusChanged(Order order) {
        notificationExecutor.submit(() -> {
            simulateLatency();
            log.info("[NOTIFICATION] User notified: order #{} status changed to {}",
                    order.getId(), order.getStatus());
        });
    }

    private void simulateLatency() {
        try {
            Thread.sleep(50);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}

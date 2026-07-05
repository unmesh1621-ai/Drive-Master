package com.drivemaster.drivemastermain.notification;

import com.drivemaster.drivemastermain.event.OrderCreatedListener;
import com.drivemaster.drivemastermain.event.OrderEvent;
import org.springframework.stereotype.Component;

@Component
public class DealerOrderNotificationListener implements OrderCreatedListener {

    private final NotificationService notificationService;

    public DealerOrderNotificationListener(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @Override
    public void onOrderCreated(OrderEvent event) {
        notificationService.notifyDealerOfNewOrder(event.order());
    }
}

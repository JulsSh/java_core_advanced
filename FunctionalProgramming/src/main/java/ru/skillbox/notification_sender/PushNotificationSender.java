package ru.skillbox.notification_sender;

import ru.skillbox.notification.Notification;
import ru.skillbox.notification.PushNotification;

import java.util.List;
import java.util.stream.Collectors;

public class PushNotificationSender implements NotificationSender<PushNotification>{
    @Override
    public void send(PushNotification notification) {
        System.out.println("PUSH");
        System.out.println("title: " + notification.getTitle());
        System.out.println("receiver: " + notification.getUserAccount());
        System.out.println("message: " + notification.formattedMessage());
        System.out.println();
    }

    @Override
    public void send(List<PushNotification> notifications) {
        for (PushNotification n : notifications) send(n);
    }
}

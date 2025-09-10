package ru.skillbox.notification_sender;

import ru.skillbox.notification.SmsNotification;

import java.util.List;
import java.util.stream.Collectors;


public class SmsNotificationSender implements NotificationSender<SmsNotification> {
    @Override
    public void send(SmsNotification notification) {
        System.out.println("SMS");
        String receivers = notification.getRecipients().stream().collect(Collectors.joining(", "));
        System.out.println("receivers: " + receivers);
        System.out.println("message: " + notification.formattedMessage());
        System.out.println();
    }

    @Override
    public void send(List<SmsNotification> notifications) {
        for (SmsNotification n : notifications) send(n);
    }
}

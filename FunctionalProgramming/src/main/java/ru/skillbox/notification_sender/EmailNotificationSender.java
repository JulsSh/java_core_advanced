package ru.skillbox.notification_sender;

import ru.skillbox.notification.EmailNotification;

import java.util.List;
import java.util.stream.Collectors;

public class EmailNotificationSender implements NotificationSender<EmailNotification> {
    @Override
    public void send(EmailNotification notification) {
        System.out.println("EMAIL");
        System.out.println("subject: " + notification.getSubject());
        String receivers = notification.getRecipients().stream().collect(Collectors.joining(", "));
        System.out.println("receivers: " + receivers);
        System.out.println("message: " + notification.formattedMessage());
        System.out.println();
    }

    @Override
    public void send(List<EmailNotification> notifications) {
        for (EmailNotification n : notifications) send(n);
    }
}

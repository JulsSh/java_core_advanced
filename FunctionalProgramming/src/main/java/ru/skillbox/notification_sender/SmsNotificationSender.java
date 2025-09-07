package ru.skillbox.notification_sender;

import ru.skillbox.notification.Notification;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class SmsNotification implements Notification {
    private final String message;
    private final List<String> recipients = new ArrayList<>();

    public SmsNotification(String message) {
        if (message == null || message.isBlank())
            throw new IllegalArgumentException("message must not be blank");
        this.message = message;
    }

    public void addRecipient(String phone) {
        if (phone == null || phone.isBlank() || !phone.matches("\\+?\\d+"))
            throw new IllegalArgumentException("invalid phone: " + phone);
        recipients.add(phone);
    }

    public List<String> getRecipients() {
        return Collections.unmodifiableList(recipients);
    }

    @Override
    public String formattedMessage() {
        return message;
    }
}

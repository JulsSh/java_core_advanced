package ru.skillbox.notification;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SmsNotification implements Notification{
    private final String message;
    private final List<String> recipients = new ArrayList<>();

    public SmsNotification(String message) {
        this.message = message;
    }

    @Override
    public String formattedMessage() {
        return message;
    }
    public void addRecipient(String phone) {
        if (phone == null || phone.isBlank() || !phone.matches("\\+?\\d+"))
            throw new IllegalArgumentException("invalid phone: " + phone);
        recipients.add(phone);
    }

    public List<String> getRecipients() {
        return Collections.unmodifiableList(recipients);
    }

}

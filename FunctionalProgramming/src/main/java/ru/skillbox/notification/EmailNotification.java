package ru.skillbox.notification;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class EmailNotification implements Notification {
private final String message;
private final String subject;
private final List<String> recipients = new ArrayList<>();

    public EmailNotification(String message, String subject) {
        this.message = message;
        this.subject = subject;
    }

    @Override
    public String formattedMessage() {
        return "<p>" + message + "</p>";
    }
    public String getSubject() {
        return subject;
    }

    public List<String> getRecipients() {
        return Collections.unmodifiableList(new ArrayList<>(recipients));
    }

    public void addRecipient(String email) {
        if (email == null || email.isBlank() || !email.contains("@"))
            throw new IllegalArgumentException("invalid email: " + email);
        recipients.add(email);
    }
}

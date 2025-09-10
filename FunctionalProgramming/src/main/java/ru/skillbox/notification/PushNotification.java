package ru.skillbox.notification;

import java.util.List;

public class PushNotification implements Notification{

    private final String message;
    private final String title;
    private final String userAccount;

    public PushNotification(String message, String title, String userAccount) {
        if (message == null || message.trim().isEmpty()) {
            throw new IllegalArgumentException("Message must not be blank");
        }
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("Title must not be blank");
        }
        if (userAccount == null || userAccount.trim().isEmpty()) {
            throw new IllegalArgumentException("User account must not be blank");
        }

        this.message = message;
        this.title = title;
        this.userAccount = userAccount;
    }
    @Override
    public String formattedMessage() {
        return "\ud83d\udc4b" + message;
    }

    @Override
    public List<String> getRecipients() {
        return List.of();
    }

    public String getTitle() {
        return title;
    }

    public String getUserAccount() {
        return userAccount;
    }
}

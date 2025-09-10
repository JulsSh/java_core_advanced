package ru.skillbox.notification;

import java.util.List;

/**
 * Уведомления для пользователей
 */
public interface Notification {
    /**
     * @return форматированные тело сообщений
     */
    String formattedMessage();

    List<String> getRecipients();
}

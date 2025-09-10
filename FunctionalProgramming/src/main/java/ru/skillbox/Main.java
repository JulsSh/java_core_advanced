package ru.skillbox;

import ru.skillbox.notification.EmailNotification;
import ru.skillbox.notification.PushNotification;
import ru.skillbox.notification.SmsNotification;
import ru.skillbox.notification_sender.EmailNotificationSender;
import ru.skillbox.notification_sender.PushNotificationSender;
import ru.skillbox.notification_sender.SmsNotificationSender;

import java.util.List;

public class Main {
    public static void main(String[] args) {
        // --- EMAIL ---
        EmailNotification email1 = new EmailNotification("Спасибо за регистрацию на сервисе!", "Успешная регистрация!");
        email1.addRecipient("oleg@java.skillbox.ru");
        email1.addRecipient("masha@java.skillbox.ru");
        email1.addRecipient("yan@java.skillbox.ru");

        EmailNotification email2 = new EmailNotification("Добро пожаловать!", "Приветствие");
        email2.addRecipient("user1@example.com");

        EmailNotification email3 = new EmailNotification("Подтвердите email, пожалуйста", "Подтверждение");
        email3.addRecipient("user2@example.com");
        email3.addRecipient("user3@example.com");

        EmailNotificationSender emailSender = new EmailNotificationSender();
        emailSender.send(email1); // одиночное
        emailSender.send(List.of(email2, email3)); // список

        // --- SMS ---
        SmsNotification sms1 = new SmsNotification("Спасибо за регистрацию на сервисе!");
        sms1.addRecipient("+70001234567");

        SmsNotification sms2 = new SmsNotification("Ваш код: 1234");
        sms2.addRecipient("+49111111111");
        sms2.addRecipient("+49222222222");

        SmsNotification sms3 = new SmsNotification("Напоминание: встреча завтра в 10:00");
        sms3.addRecipient("+49333333333");

        SmsNotificationSender smsSender = new SmsNotificationSender();
        smsSender.send(sms1);
        smsSender.send(List.of(sms2, sms3));

        // --- PUSH ---
        PushNotification push1 = new PushNotification("Спасибо за регистрацию на сервисе!", "Успешная регистрация!", "o.yanovich");
        PushNotification push2 = new PushNotification("Новый вход в аккаунт", "Безопасность", "user123");
        PushNotification push3 = new PushNotification("Доступна новая функция", "Обновление", "masha");

        PushNotificationSender pushSender = new PushNotificationSender();
        pushSender.send(push1);
        pushSender.send(List.of(push2, push3));
    }
}

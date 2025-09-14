import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.Logger;
import ru.skillbox.exceptions.CustomerAlreadyExistsException;
import ru.skillbox.exceptions.InvalidComponentCountException;
import ru.skillbox.exceptions.InvalidEmailFormatException;
import ru.skillbox.exceptions.InvalidPhoneFormatException;

import java.util.regex.Pattern;

import static org.apache.logging.log4j.LogManager.*;

public class CustomerStorage {
    private static final Logger LOG =  getLogger(CustomerStorage.class);
    private static final Logger QLOG = getLogger("queries");

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    private final Map<String, Customer> storage;

    public CustomerStorage() {
        storage = new HashMap<>();
    }

    public void addCustomer(String data) {
        try {

            if (data == null || data.isBlank()) {
                throw new InvalidComponentCountException(
                        "Пустая строка данных. Ожидается: \"Имя Фамилия email phone\".");
            }

            // Разбиваем и требуем ровно 4 компонента: Имя Фамилия Email Телефон
            String[] components = data.trim().split("\\s+");
            if (components.length != 4) {
                throw new InvalidComponentCountException(
                        "Некорректное количество компонентов: " + components.length +
                                ". Ожидалось 4: \"Имя Фамилия email phone\".\n" +
                                "Пример: add Василий Петров vasily.petrov@gmail.com +79215637722");
            }

            final int INDEX_NAME = 0;
            final int INDEX_SURNAME = 1;
            final int INDEX_EMAIL = 2;
            final int INDEX_PHONE = 3;

            String name = components[INDEX_NAME] + " " + components[INDEX_SURNAME];
            String email = components[INDEX_EMAIL];
            String phone = normalizeAndValidatePhone(components[INDEX_PHONE]);

            if (!EMAIL_PATTERN.matcher(email).matches()) {
                throw new InvalidEmailFormatException(
                        "Неверный формат email: \"" + email + "\". Пример: vasily.petrov@gmail.com");
            }

            if (storage.containsKey(name)) {
                throw new CustomerAlreadyExistsException(
                        "Клиент \"" + name + "\" уже существует. Используйте другое имя или удалите старого клиента."
                );
            }

            storage.put(name, new Customer(name, phone, email));
            LOG.info("Добавлен клиент: " + name);
        } catch (RuntimeException e) {
            // Логируем и пробрасываем дальше — пусть Main решит, что показать пользователю
            LOG.error("Ошибка при добавлении клиента: " + e.getMessage());
            throw e;
        }
    }

    public void listCustomers() {
        QLOG.info("list");
        storage.values().forEach(System.out::println);
    }

    public void removeCustomer(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Укажите имя и фамилию клиента для удаления.");
        }
        if (storage.remove(name) == null) {
            throw new IllegalArgumentException("Клиент \"" + name + "\" не найден.");
        }
        LOG.info("Удалён клиент: " + name);
    }

    public Customer getCustomer(String name) {
        return storage.get(name);
    }

    public int getCount() {
        int size = storage.size();
        QLOG.info("count -> {}", size);
        return size;
    }

    // --- Helpers ---

    /**
     * Принимаем русский номер в вольном виде и нормализуем к формату +7XXXXXXXXXX.
     * Допустимые входы: +7XXXXXXXXXX, 8XXXXXXXXXX, 7XXXXXXXXXX (все — ровно 11 цифр)
     */
    private String normalizeAndValidatePhone(String raw) {
        if (raw == null || raw.isBlank()) {
            throw new InvalidPhoneFormatException("Телефон не указан.");
        }
        String digits = raw.replaceAll("\\D", ""); // убираем всё, кроме цифр

        if (digits.length() != 11) {
            throw new InvalidPhoneFormatException(
                    "Неверный телефон: \"" + raw + "\". Ожидается 11 цифр. Пример: +79215637722");
        }

        char first = digits.charAt(0);
        if (first != '7' && first != '8') {
            throw new InvalidPhoneFormatException(
                    "Неверный телефон: \"" + raw + "\". Должен начинаться с 7 или 8.");
        }

        // Нормализуем к +7XXXXXXXXXX
        String last10 = digits.substring(1);
        return "+7" + last10;
    }
}

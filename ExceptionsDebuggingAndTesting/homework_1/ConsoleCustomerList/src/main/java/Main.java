import java.util.Scanner;
import ru.skillbox.exceptions.CustomerAlreadyExistsException;
import ru.skillbox.exceptions.InvalidComponentCountException;
import ru.skillbox.exceptions.InvalidEmailFormatException;
import ru.skillbox.exceptions.InvalidPhoneFormatException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Main {



    private static final Logger LOGGER = LogManager.getLogger(Main.class);
    private static final Logger QLOG   = LogManager.getLogger("queries");

    private static final String ADD_COMMAND = "add Василий Петров vasily.petrov@gmail.com +79215637722";
    private static final String COMMAND_EXAMPLES = "\t" + ADD_COMMAND + "\n\tlist\n\tcount\n\tremove Василий Петров";
    private static final String COMMAND_ERROR = "Wrong command! Available command examples: \n" + COMMAND_EXAMPLES;
    private static final String HELP_TEXT = "Command examples:\n" + COMMAND_EXAMPLES;

    public static void main(String[] args) {
        System.out.println("LOG_DIR=" + System.getProperty("logDir"));
        Scanner scanner = new Scanner(System.in);
        CustomerStorage storage = new CustomerStorage();

        QLOG.info("App started. Waiting for commands...");
        LOGGER.info("Probe via root logger (should also be in queries.log)");

        System.out.println("ConsoleCustomerList started. Type `help` and press Enter.");

        while (true) {
            System.out.print("> ");
            String command = scanner.nextLine();
            if (command == null) { continue; }
            QLOG.info("Command received: {}", command);
            handleCommand(storage, command);
        }
    }

    private static void handleCommand(CustomerStorage storage, String command) {
        String[] tokens = command.trim().split("\\s+", 2);
        String action = tokens[0].toLowerCase();

        try {
            switch (action) {
                case "add":
                    handleAdd(storage, tokens);
                    break;
                case "list":
                    storage.listCustomers();
                    break;
                case "remove":
                    handleRemove(storage, tokens);
                    break;
                case "count":
                    System.out.println("There are " + storage.getCount() + " customers");
                    break;
                case "help":
                    System.out.println(HELP_TEXT);
                    break;
                default:
                    System.out.println(COMMAND_ERROR);
            }
        } catch (InvalidComponentCountException |
                 InvalidEmailFormatException |
                 InvalidPhoneFormatException |
                 CustomerAlreadyExistsException |
                 IllegalArgumentException e) {
            System.out.println("Ошибка: " + e.getMessage());
            LOGGER.error("User input error: {}", e.getMessage(), e);
        } catch (Exception e) {
            System.out.println("Непредвиденная ошибка. Подробности в логе.");
            LOGGER.error("Unexpected error", e);
        }
    }

    private static void handleAdd(CustomerStorage storage, String[] tokens) {
        if (tokens.length < 2 || tokens[1].isBlank()) {
            System.out.println("Укажите данные клиента: \"Имя Фамилия email phone\".\nПример: " + ADD_COMMAND);
            return;
        }
        storage.addCustomer(tokens[1]);
    }

    private static void handleRemove(CustomerStorage storage, String[] tokens) {
        if (tokens.length < 2 || tokens[1].isBlank()) {
            System.out.println("Укажите имя и фамилию клиента для удаления. Пример: remove Василий Петров");
            return;
        }
        storage.removeCustomer(tokens[1]);
    }
}

package ru.skillbox.collection_adv;

import java.util.*;

import static ru.skillbox.collection_adv.Commands.*;


public class ProgramMenu {
    private static final int MAX_AGE = 150;
    private final Map<Integer, UserModel> byPassport = new HashMap<Integer, UserModel>();
    private final ArrayList<HashSet<Integer>> buckets = new ArrayList<HashSet<Integer>>();
    private long totalAge = 0L;

    public ProgramMenu() {
        for (int i = 0; i <= MAX_AGE; i++) {
            buckets.add(new HashSet<Integer>());
        }

        // при желании — предзагрузка примеров:
        preload(new UserModel(12345, 38, "Malika"));
        preload(new UserModel(82345, 54, "Malika"));
        preload(new UserModel(35689, 12, "Selma"));
        preload(new UserModel(128865444, 92, "Ghee"));
    }

    private void preload(UserModel u) {
        byPassport.put(u.getPassportNumber(), u);
        if (u.getAge() >= 0 && u.getAge() <= MAX_AGE) {
            buckets.get(u.getAge()).add(u.getPassportNumber());
        }
        totalAge += u.getAge();
    }

    private static final Map<String, Commands> CMD = new HashMap<>();

    static {
        CMD.put("in", IN);
        CMD.put("del", DEL);
        CMD.put("count", Commands.COUNT);
        CMD.put("avg", Commands.AVG);
        CMD.put("median", Commands.MEDIAN);
        CMD.put("young", Commands.YOUNG);
        CMD.put("old", Commands.OLD);
        CMD.put("print", Commands.PRINT);
        CMD.put("help", Commands.HELP);
        CMD.put("exit", EXIT);
    }


    public void startProgram() {
        Scanner sc = new Scanner(System.in);
        while (true) {
            help();
            String input = sc.nextLine().toLowerCase().trim();
            Commands cmd = CMD.get(input);
            if (cmd == null) {
                System.out.println("Unknown command. Type 'help'.");
                continue;
            }

            switch (cmd) {
                case IN:
                    commandIn(sc);
                    break;
                case DEL:
                    commandDel(sc);
                    break;
                case AVG:
                    commandAvg();
                    break;
                case OLD:
                    commandOld();
                    break;
                case EXIT:
                    System.out.println("Program will exit");
                    System.exit(0);  // 0 = normal exit, non-zero = error
                    break;
                case HELP:
                    help();
                    break;
                case COUNT:
                    commandCount();
                    break;
                case YOUNG:
                    commandYoung();
                    break;
                case MEDIAN:
                    commandMedian();
                    break;
                case PRINT:
                    commandPrint();
                    break;
                default:
                    System.out.println("Command recognized: " + cmd);
            }
        }
    }

    private static void help() {
        System.out.println("Available options: ");
        System.out.println();
        System.out.println(IN + " - add user data");
        System.out.println(DEL + " - delete user data");
        System.out.println(COUNT + " - amount of users in memory");
        System.out.println(AVG + " - average age of all users");
        System.out.println(MEDIAN + " - median of age");
        System.out.println(YOUNG + " - youngest user");
        System.out.println(OLD + " - oldest user");
        System.out.println(PRINT + " - print all users");
        System.out.println(HELP + " - all commands displayed");
        System.out.println(EXIT + " - exit program");
    }


    private void commandPrint() {
        if (byPassport.isEmpty()) {
            System.out.println("Пользователи не добавлены.");
            return;
        }
        // Проходим возраста по возрастанию
        for (int age = 0; age <= MAX_AGE; age++) {
            if (buckets.get(age).isEmpty()) continue;
            // для стабильности можно отсортировать по имени, потом по паспорту (без лямбд)
            ArrayList<UserModel> sameAge = new ArrayList<UserModel>();
            for (Integer passport : buckets.get(age)) {
                sameAge.add(byPassport.get(passport));
            }
            Collections.sort(sameAge, new Comparator<UserModel>() {
                @Override
                public int compare(UserModel a, UserModel b) {
                    int nameCmp = a.getUserName().compareTo(b.getUserName());
                    if (nameCmp != 0) return nameCmp;
                    return Integer.compare(a.getPassportNumber(), b.getPassportNumber());
                }
            });
            for (UserModel u : sameAge) {
                System.out.println(u);
            }
        }
    }

    private void commandOld() {
        if (byPassport.isEmpty()) {
            System.out.println("Пользователи не добавлены.");
            return;
        }
        for (int age = MAX_AGE; age >= 0; age--) {
            if (!buckets.get(age).isEmpty()) {
                Integer passport = buckets.get(age).iterator().next();
                UserModel u = byPassport.get(passport);
                System.out.println("Самый старший: " + u);
                return;
            }
        }
        System.out.println("Не найден самый старший (внутренняя ошибка).");
    }

    private void commandYoung() {
        if (byPassport.isEmpty()) {
            System.out.println("Пользователи не добавлены.");
            return;
        }
        for (int age = 0; age <= MAX_AGE; age++) {
            if (!buckets.get(age).isEmpty()) {
                // берём любой паспорт из корзины возраста
                Integer passport = buckets.get(age).iterator().next();
                UserModel u = byPassport.get(passport);
                System.out.println("Самый молодой: " + u);
                return;
            }
        }
        System.out.println("Не найден самый молодой (внутренняя ошибка).");
    }

    private void commandMedian() {
        int n = byPassport.size();
        if (n == 0) {
            System.out.println("Пользователи не добавлены.");
            return;
        }

        // Находим позиции середины (для чётного — две середины)
        int m1 = (n - 1) / 2; // индекс слева (0-based)
        int m2 = n / 2;       // индекс справа (0-based)

        int seen = 0;
        Integer age1 = null, age2 = null;

        for (int age = 0; age <= MAX_AGE; age++) {
            int bucketSize = buckets.get(age).size();
            if (bucketSize == 0) continue;

            int startIndex = seen;
            int endIndex = seen + bucketSize - 1;

            if (age1 == null && m1 >= startIndex && m1 <= endIndex) {
                age1 = age;
            }
            if (age2 == null && m2 >= startIndex && m2 <= endIndex) {
                age2 = age;
            }

            seen += bucketSize;
            if (age1 != null && age2 != null) break;
        }

        if (age1 != null && age2 != null) {
            double median = (age1 + age2) / 2.0;
            System.out.println("Медиана возраста: " + median);
        } else {
            System.out.println("Не удалось вычислить медиану (внутренняя ошибка).");
        }
    }

    private void commandAvg() {
        int n = byPassport.size();
        if (n == 0) {
            System.out.println("Пользователи не добавлены.");
            return;
        }
        double avg = (double) totalAge / n;
        System.out.println("Средний возраст: " + avg);
    }

    private void commandCount() {
        System.out.println("Количество пользователей: " + byPassport.size());
    }

    private void commandDel(Scanner sc) {
        System.out.print("Введите номер паспорта для удаления: ");
        String pLine = sc.nextLine().trim();
        int passport;
        try {
            passport = Integer.parseInt(pLine);
        } catch (NumberFormatException e) {
            System.out.println("Номер паспорта должен быть целым числом.");
            return;
        }

        UserModel removed = byPassport.remove(passport);
        if (removed == null) {
            System.out.println("Пользователь с паспортом " + passport + " не найден.");
            return;
        }

        // убрать из корзины возраста
        int age = removed.getAge();
        if (age >= 0 && age <= MAX_AGE) {
            buckets.get(age).remove(passport);
        }
        totalAge -= age;

        System.out.println("Пользователь удалён: " + removed);
    }

    private void commandIn(Scanner sc) {
        System.out.print("Введите номер паспорта: ");
        String pLine = sc.nextLine().trim();
        int passport;
        try {
            passport = Integer.parseInt(pLine);
        } catch (NumberFormatException e) {
            System.out.println("Номер паспорта должен быть целым числом.");
            return;
        }

        if (byPassport.containsKey(passport)) {
            System.out.println("Пользователь с таким паспортом уже существует: " + byPassport.get(passport));
            return;
        }

        System.out.print("Введите имя: ");
        String name = sc.nextLine().trim();

        System.out.print("Введите возраст: ");
        String aLine = sc.nextLine().trim();
        int age;
        try {
            age = Integer.parseInt(aLine);
            if (age < 0 || age > MAX_AGE) {
                System.out.println("Возраст должен быть от 0 до " + MAX_AGE + ".");
                return;
            }
        } catch (NumberFormatException e) {
            System.out.println("Возраст должен быть целым неотрицательным числом.");
            return;
        }

        UserModel u = new UserModel(passport, age, name);
        byPassport.put(passport, u);
        buckets.get(age).add(passport);
        totalAge += age;

        System.out.println("Пользователь добавлен: " + u);
    }
}


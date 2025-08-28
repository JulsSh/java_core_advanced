package com.example;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;

public class App {
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    private static final NumberFormat MONEY_FMT = NumberFormat.getCurrencyInstance(Locale.getDefault());

    public static void main(String[] args) {
        List<Transaction> ledger = new ArrayList<>();
        Scanner sc = new Scanner(System.in);
        printHelp();
        while (true) {
            String line = sc.nextLine().trim();
            if (line.equalsIgnoreCase("HELP")) {
                printHelp();
            } else if (line.equalsIgnoreCase("REPORT")) {
                printReport(ledger);
            } else if (line.equalsIgnoreCase("EXIT")) {
                printReport(ledger);
                break;
            } else {
                try {
                    Transaction tx = parseTransaction(line);
                    ledger.add(tx);
                    System.out.println("Added: " + tx.description());
                } catch (Exception e) {
                    System.out.println("Wrong format. Example: Описание; 123.45; INCOME; 24.03.2036");
                }
            }
        }
    }

    private static void printReport(List<Transaction> ledger) {
        BigDecimal income = BigDecimal.ZERO;
        BigDecimal expense = BigDecimal.ZERO;
        for (Transaction tx : ledger) {
            if (tx.type() == TransactionType.INCOME) {
                income = income.add(tx.amount());
            } else {
                expense = expense.add(tx.amount());
            }
        }
        BigDecimal balance = income.subtract(expense);
        System.out.println("========== REPORT ==========");
        System.out.println("Total income : " + MONEY_FMT.format(income));
        System.out.println("Total expense: " + MONEY_FMT.format(expense));
        System.out.println("Balance : " + MONEY_FMT.format(balance));
        System.out.println();
        System.out.println("Last 5 transactions:");
        int start = Math.max(0, ledger.size() - 5);
        List<Transaction> last5 = ledger.subList(start, ledger.size());
        for (Transaction tx : last5) {
            String formattedDate = DATE_FMT.format(tx.date());
            System.out.printf("%-20s %10s %8s %12s%n", tx.description(), MONEY_FMT.format(tx.amount()), tx.type(), formattedDate);
        }
        System.out.println("============================");
    }

    private static void printHelp() {
        System.out.println("========== HELP ==========");
        System.out.println("You can use the following commands:");
        System.out.println(" HELP - show this help text");
        System.out.println(" REPORT - show financial report");
        System.out.println(" EXIT - show report and exit the program");
        System.out.println();
        System.out.println("If you don’t type a command, enter a transaction in the format:");
        System.out.println(" Description; amount; type; date");
        System.out.println("Example:");
        System.out.println(" Билет на Марс; 2499.99; EXPENSE; 24.03.2036");
        System.out.println("==========================");
    }

    static Transaction parseTransaction(String line) {
        String[] parts = line.split(";");
        if (parts.length != 4) {
            throw new IllegalArgumentException("Incorrect format" + line);
        }
        String description = parts[0].trim();
        BigDecimal amount = new BigDecimal(parts[1].trim());
        TransactionType type = TransactionType.valueOf(parts[2].trim().toUpperCase());
        LocalDate date = LocalDate.parse(parts[3].trim(), DATE_FMT);
        return new Transaction(description, amount, type, date);
    }
}
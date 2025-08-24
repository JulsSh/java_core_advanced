package java.com.example;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class App {
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    private static final NumberFormat MONEY_FMT = NumberFormat.getCurrencyInstance(Locale.getDefault());

    public static void main(String[] args) {
        String line = "Билет на Марс; 2499.99; EXPENSE; 24.03.2036";
        Transaction tx = parseTransaction(line);
        // 🔹 3. Print the result
        System.out.println("Parsed transaction:");
        System.out.println("  description = " + tx.description());
        System.out.println("  amount      = " + tx.amount());
        System.out.println("  type        = " + tx.type());
        System.out.println("  date        = " + tx.date());

    }
        static Transaction parseTransaction (String line){
            String[] parts = line.split(";");
            if(parts.length !=4){
                throw new IllegalArgumentException("Incorrect format" + line);
            }
            String description = parts[0].trim();
            BigDecimal amount = new BigDecimal(parts[1].trim());
            TransactionType type = TransactionType.valueOf(parts[2].trim().toUpperCase());
            LocalDate date = LocalDate.parse(parts[3].trim(), DATE_FMT);

            return new Transaction(description, amount, type, date);
        }
    }

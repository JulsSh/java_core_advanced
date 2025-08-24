package com.example;

import java.math.BigDecimal;
import java.time.LocalDate;

public record Transaction(String description, BigDecimal amount, TransactionType type, LocalDate date) {
}

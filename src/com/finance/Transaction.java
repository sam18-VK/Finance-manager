package com.finance;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Represents a single income or expense entry.
 * Core OOP model: immutable-ish data object with validation in the
 * constructor (fails fast via InvalidTransactionException).
 */
public class Transaction {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final int id;
    private final TransactionType type;
    private final double amount;
    private final String category;
    private final String description;
    private final LocalDate date;

    public Transaction(int id, TransactionType type, double amount, String category,
                        String description, LocalDate date) throws InvalidTransactionException {
        if (amount <= 0) {
            throw new InvalidTransactionException("Amount must be positive (got " + amount + ")");
        }
        if (category == null || category.isBlank()) {
            throw new InvalidTransactionException("Category cannot be empty");
        }
        if (date == null) {
            throw new InvalidTransactionException("Date cannot be null");
        }
        this.id = id;
        this.type = type;
        this.amount = amount;
        this.category = category.trim();
        this.description = description == null ? "" : description.trim();
        this.date = date;
    }

    public int getId() { return id; }
    public TransactionType getType() { return type; }
    public double getAmount() { return amount; }
    public String getCategory() { return category; }
    public String getDescription() { return description; }
    public LocalDate getDate() { return date; }

    /** Signed amount: positive for income, negative for expense — handy for running totals. */
    public double getSignedAmount() {
        return type == TransactionType.INCOME ? amount : -amount;
    }

    /** Serializes this transaction to one CSV line (used by FileHandler). */
    public String toCsvLine() {
        // Escape commas in description by replacing with semicolons (simple CSV safety)
        String safeDescription = description.replace(",", ";");
        return String.join(",",
                String.valueOf(id),
                type.name(),
                String.valueOf(amount),
                category,
                safeDescription,
                date.format(DATE_FMT));
    }

    /** Parses one CSV line back into a Transaction. */
    public static Transaction fromCsvLine(String line) throws InvalidTransactionException {
        String[] parts = line.split(",", -1);
        if (parts.length != 6) {
            throw new InvalidTransactionException("Malformed CSV line: " + line);
        }
        try {
            int id = Integer.parseInt(parts[0].trim());
            TransactionType type = TransactionType.valueOf(parts[1].trim());
            double amount = Double.parseDouble(parts[2].trim());
            String category = parts[3].trim();
            String description = parts[4].trim();
            LocalDate date = LocalDate.parse(parts[5].trim(), DATE_FMT);
            return new Transaction(id, type, amount, category, description, date);
        } catch (NumberFormatException | java.time.format.DateTimeParseException e) {
            throw new InvalidTransactionException("Could not parse CSV line: " + line + " (" + e.getMessage() + ")");
        }
    }

    @Override
    public String toString() {
        return String.format("#%-4d %-7s %10.2f  [%-15s] %-20s  %s",
                id, type, amount, category, description, date.format(DATE_FMT));
    }
}

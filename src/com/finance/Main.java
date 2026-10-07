package com.finance;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

/**
 * Console front-end for the Personal Finance Management & Analytics System.
 * Pure menu-driven I/O — all real logic lives in FinanceManager / FileHandler.
 */
public class Main {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final Scanner scanner = new Scanner(System.in);
    private static final FinanceManager manager = new FinanceManager();

    public static void main(String[] args) {
        System.out.println("=========================================");
        System.out.println(" Personal Finance Management & Analytics ");
        System.out.println("=========================================");
        seedSampleData();

        boolean running = true;
        while (running) {
            printMenu();
            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1": addTransaction(TransactionType.INCOME); break;
                case "2": addTransaction(TransactionType.EXPENSE); break;
                case "3": viewAllTransactions(); break;
                case "4": searchTransactions(); break;
                case "5": categoryWiseAnalysis(); break;
                case "6": monthlySummary(); break;
                case "7": setBudget(); break;
                case "8": budgetStatus(); break;
                case "9": saveToFile(); break;
                case "10": loadFromFile(); break;
                case "11": removeTransaction(); break;
                case "0": running = false; break;
                default: System.out.println("Invalid option, try again.");
            }
        }
        System.out.println("Goodbye!");
    }

    private static void printMenu() {
        System.out.println();
        System.out.printf("Balance: %.2f  |  Transactions: %d%n", manager.getBalance(), manager.getTransactionCount());
        System.out.println("-----------------------------------------");
        System.out.println(" 1. Add Income");
        System.out.println(" 2. Add Expense");
        System.out.println(" 3. View All Transactions");
        System.out.println(" 4. Search Transactions");
        System.out.println(" 5. Category-wise Analysis");
        System.out.println(" 6. Monthly Summary");
        System.out.println(" 7. Set Budget Limit");
        System.out.println(" 8. View Budget Status (current month)");
        System.out.println(" 9. Save to File");
        System.out.println("10. Load from File");
        System.out.println("11. Remove Transaction by ID");
        System.out.println(" 0. Exit");
        System.out.print("Choose an option: ");
    }

    // ---------------------------------------------------------------
    private static void addTransaction(TransactionType type) {
        try {
            System.out.print("Amount: ");
            double amount = Double.parseDouble(scanner.nextLine().trim());

            System.out.print("Category (e.g. Food, Rent, Salary): ");
            String category = scanner.nextLine().trim();

            System.out.print("Description (optional): ");
            String description = scanner.nextLine().trim();

            System.out.print("Date (yyyy-MM-dd), blank = today: ");
            String dateStr = scanner.nextLine().trim();
            LocalDate date = dateStr.isBlank() ? LocalDate.now() : LocalDate.parse(dateStr, DATE_FMT);

            Transaction t = manager.addTransaction(type, amount, category, description, date);
            System.out.println("Added: " + t);

        } catch (NumberFormatException e) {
            System.out.println("Error: amount must be a valid number.");
        } catch (DateTimeParseException e) {
            System.out.println("Error: date must be in yyyy-MM-dd format.");
        } catch (InvalidTransactionException e) {
            System.out.println("Error: " + e.getMessage());
        } catch (BudgetExceededException e) {
            System.out.println("Transaction recorded, but WARNING: " + e.getMessage());
        }
    }

    private static void removeTransaction() {
        try {
            System.out.print("Transaction ID to remove: ");
            int id = Integer.parseInt(scanner.nextLine().trim());
            boolean removed = manager.removeTransaction(id);
            System.out.println(removed ? "Removed." : "No transaction with that ID.");
        } catch (NumberFormatException e) {
            System.out.println("Error: please enter a valid integer ID.");
        }
    }

    private static void viewAllTransactions() {
        List<Transaction> all = manager.getAllTransactions();
        if (all.isEmpty()) {
            System.out.println("No transactions recorded yet.");
            return;
        }
        System.out.println("\nID   TYPE    AMOUNT      CATEGORY         DESCRIPTION           DATE");
        for (Transaction t : all) {
            System.out.println(t);
        }
    }

    private static void searchTransactions() {
        System.out.print("Filter by type (INCOME/EXPENSE, blank = any): ");
        String typeStr = scanner.nextLine().trim();
        TransactionType type = null;
        if (!typeStr.isBlank()) {
            try {
                type = TransactionType.valueOf(typeStr.toUpperCase());
            } catch (IllegalArgumentException e) {
                System.out.println("Unrecognized type, ignoring filter.");
            }
        }

        System.out.print("Filter by category (blank = any): ");
        String category = scanner.nextLine().trim();

        System.out.print("Keyword in description (blank = any): ");
        String keyword = scanner.nextLine().trim();

        System.out.print("From date yyyy-MM-dd (blank = no lower bound): ");
        LocalDate from = parseOptionalDate(scanner.nextLine().trim());

        System.out.print("To date yyyy-MM-dd (blank = no upper bound): ");
        LocalDate to = parseOptionalDate(scanner.nextLine().trim());

        List<Transaction> results = manager.search(type, category, keyword, from, to);
        if (results.isEmpty()) {
            System.out.println("No matching transactions.");
        } else {
            System.out.println("\nFound " + results.size() + " transaction(s):");
            for (Transaction t : results) System.out.println(t);
        }
    }

    private static LocalDate parseOptionalDate(String s) {
        if (s.isBlank()) return null;
        try {
            return LocalDate.parse(s, DATE_FMT);
        } catch (DateTimeParseException e) {
            System.out.println("Could not parse that date, ignoring.");
            return null;
        }
    }

    private static void categoryWiseAnalysis() {
        System.out.println("\n-- Expense by Category --");
        Map<String, Double> expenses = manager.getCategoryTotals(TransactionType.EXPENSE);
        if (expenses.isEmpty()) {
            System.out.println("No expenses recorded yet.");
        } else {
            expenses.entrySet().stream()
                    .sorted((a, b) -> Double.compare(b.getValue(), a.getValue()))
                    .forEach(e -> System.out.printf("%-15s %.2f%n", e.getKey(), e.getValue()));
        }

        System.out.println("\n-- Income by Category --");
        Map<String, Double> income = manager.getCategoryTotals(TransactionType.INCOME);
        if (income.isEmpty()) {
            System.out.println("No income recorded yet.");
        } else {
            income.entrySet().stream()
                    .sorted((a, b) -> Double.compare(b.getValue(), a.getValue()))
                    .forEach(e -> System.out.printf("%-15s %.2f%n", e.getKey(), e.getValue()));
        }
    }

    private static void monthlySummary() {
        Map<YearMonth, FinanceManager.MonthlySummary> summaries = manager.getMonthlySummaries();
        if (summaries.isEmpty()) {
            System.out.println("No data yet.");
            return;
        }
        for (FinanceManager.MonthlySummary s : summaries.values()) {
            System.out.printf("%n%s  |  Income: %.2f  Expense: %.2f  Net: %.2f%n",
                    s.month, s.totalIncome, s.totalExpense, s.getNet());
            s.expenseByCategory.entrySet().stream()
                    .sorted((a, b) -> Double.compare(b.getValue(), a.getValue()))
                    .forEach(e -> System.out.printf("    %-15s %.2f%n", e.getKey(), e.getValue()));
        }
    }

    private static void setBudget() {
        System.out.print("Category: ");
        String category = scanner.nextLine().trim();
        try {
            System.out.print("Monthly limit: ");
            double limit = Double.parseDouble(scanner.nextLine().trim());
            manager.setBudget(category, limit);
            System.out.println("Budget set: " + category + " -> " + limit);
        } catch (NumberFormatException e) {
            System.out.println("Error: limit must be a valid number.");
        }
    }

    private static void budgetStatus() {
        YearMonth currentMonth = YearMonth.now();
        Map<String, Budget> budgets = manager.getBudgets();
        if (budgets.isEmpty()) {
            System.out.println("No budgets configured yet.");
            return;
        }
        Map<String, Double> status = manager.getBudgetStatusForMonth(currentMonth);
        System.out.println("\nBudget status for " + currentMonth + ":");
        for (Budget b : budgets.values()) {
            double spent = status.getOrDefault(b.getCategory(), 0.0);
            String flag = spent > b.getMonthlyLimit() ? "  OVER BUDGET" : "";
            System.out.printf("%-15s spent %.2f / limit %.2f%s%n",
                    b.getCategory(), spent, b.getMonthlyLimit(), flag);
        }
    }

    private static void saveToFile() {
        System.out.print("File name to save (e.g. transactions.csv): ");
        String fileName = scanner.nextLine().trim();
        try {
            FileHandler.save(manager, fileName);
            System.out.println("Saved " + manager.getTransactionCount() + " transactions to " + fileName);
        } catch (java.io.IOException e) {
            System.out.println("Error saving file: " + e.getMessage());
        }
    }

    private static void loadFromFile() {
        System.out.print("File name to load (e.g. transactions.csv): ");
        String fileName = scanner.nextLine().trim();
        try {
            FileHandler.LoadResult result = FileHandler.load(manager, fileName);
            System.out.printf("Loaded %d transactions (%d skipped due to errors).%n",
                    result.loadedCount, result.skippedCount);
        } catch (java.io.IOException e) {
            System.out.println("Error loading file: " + e.getMessage());
        }
    }

    /** A few starter transactions so the menu isn't empty on first run / for demos. */
    private static void seedSampleData() {
        try {
            manager.setBudget("Food", 5000);
            manager.setBudget("Entertainment", 2000);

            manager.addTransaction(TransactionType.INCOME, 50000, "Salary", "October salary", LocalDate.now().minusDays(5));
            manager.addTransaction(TransactionType.EXPENSE, 1200, "Food", "Groceries", LocalDate.now().minusDays(4));
            manager.addTransaction(TransactionType.EXPENSE, 800, "Entertainment", "Movie night", LocalDate.now().minusDays(2));
            manager.addTransaction(TransactionType.EXPENSE, 3000, "Rent", "Hostel rent", LocalDate.now().minusDays(1));
        } catch (InvalidTransactionException | BudgetExceededException e) {
            // Sample data is trusted; ignore in the unlikely case it ever fails.
        }
    }
}

package com.finance;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Core business-logic class: owns the in-memory transaction list and the
 * per-category budgets, and provides every analytics operation the app
 * exposes (category breakdown, monthly summaries, search, budget checks).
 *
 * Demonstrates: Collections (ArrayList, HashMap, TreeMap), the Date-Time
 * API (LocalDate / YearMonth), and exception handling.
 */
public class FinanceManager {

    private final List<Transaction> transactions = new ArrayList<>();
    private final Map<String, Budget> budgets = new HashMap<>();
    private int nextId = 1;

    // ---------------------------------------------------------------
    // Adding / removing transactions
    // ---------------------------------------------------------------

    /**
     * Adds a transaction. If it's an expense that pushes its category over
     * budget for that month, the transaction is still recorded but a
     * BudgetExceededException is thrown afterwards so the caller can warn
     * the user without losing the entry.
     */
    public Transaction addTransaction(TransactionType type, double amount, String category,
                                       String description, LocalDate date)
            throws InvalidTransactionException, BudgetExceededException {

        Transaction t = new Transaction(nextId, type, amount, category, description, date);
        nextId++;
        transactions.add(t);

        if (type == TransactionType.EXPENSE && budgets.containsKey(category)) {
            Budget budget = budgets.get(category);
            double spentThisMonth = getCategoryTotalForMonth(category, YearMonth.from(date));
            if (spentThisMonth > budget.getMonthlyLimit()) {
                throw new BudgetExceededException(category, budget.getMonthlyLimit(), spentThisMonth);
            }
        }
        return t;
    }

    public boolean removeTransaction(int id) {
        return transactions.removeIf(t -> t.getId() == id);
    }

    public List<Transaction> getAllTransactions() {
        List<Transaction> sorted = new ArrayList<>(transactions);
        sorted.sort(Comparator.comparing(Transaction::getDate));
        return Collections.unmodifiableList(sorted);
    }

    // ---------------------------------------------------------------
    // Search
    // ---------------------------------------------------------------

    /** Flexible search: any parameter may be null/blank to mean "don't filter on this". */
    public List<Transaction> search(TransactionType type, String category, String keyword,
                                     LocalDate from, LocalDate to) {
        List<Transaction> results = new ArrayList<>();
        for (Transaction t : transactions) {
            if (type != null && t.getType() != type) continue;
            if (category != null && !category.isBlank()
                    && !t.getCategory().equalsIgnoreCase(category.trim())) continue;
            if (keyword != null && !keyword.isBlank()
                    && !t.getDescription().toLowerCase().contains(keyword.trim().toLowerCase())) continue;
            if (from != null && t.getDate().isBefore(from)) continue;
            if (to != null && t.getDate().isAfter(to)) continue;
            results.add(t);
        }
        results.sort(Comparator.comparing(Transaction::getDate));
        return results;
    }

    // ---------------------------------------------------------------
    // Category-wise analysis
    // ---------------------------------------------------------------

    public Map<String, Double> getCategoryTotals(TransactionType type) {
        Map<String, Double> totals = new HashMap<>();
        for (Transaction t : transactions) {
            if (t.getType() != type) continue;
            totals.merge(t.getCategory(), t.getAmount(), Double::sum);
        }
        return totals;
    }

    private double getCategoryTotalForMonth(String category, YearMonth month) {
        double total = 0;
        for (Transaction t : transactions) {
            if (t.getType() == TransactionType.EXPENSE
                    && t.getCategory().equalsIgnoreCase(category)
                    && YearMonth.from(t.getDate()).equals(month)) {
                total += t.getAmount();
            }
        }
        return total;
    }

    // ---------------------------------------------------------------
    // Monthly summaries
    // ---------------------------------------------------------------

    public static class MonthlySummary {
        public final YearMonth month;
        public double totalIncome;
        public double totalExpense;
        public final Map<String, Double> expenseByCategory = new HashMap<>();

        public MonthlySummary(YearMonth month) { this.month = month; }

        public double getNet() { return totalIncome - totalExpense; }
    }

    /** One summary per month that has at least one transaction, ordered chronologically. */
    public Map<YearMonth, MonthlySummary> getMonthlySummaries() {
        Map<YearMonth, MonthlySummary> summaries = new TreeMap<>(); // TreeMap => naturally sorted by month
        for (Transaction t : transactions) {
            YearMonth ym = YearMonth.from(t.getDate());
            MonthlySummary summary = summaries.computeIfAbsent(ym, MonthlySummary::new);
            if (t.getType() == TransactionType.INCOME) {
                summary.totalIncome += t.getAmount();
            } else {
                summary.totalExpense += t.getAmount();
                summary.expenseByCategory.merge(t.getCategory(), t.getAmount(), Double::sum);
            }
        }
        return summaries;
    }

    // ---------------------------------------------------------------
    // Budgets
    // ---------------------------------------------------------------

    public void setBudget(String category, double monthlyLimit) {
        budgets.put(category, new Budget(category, monthlyLimit));
    }

    public Map<String, Budget> getBudgets() {
        return Collections.unmodifiableMap(budgets);
    }

    /** Spend vs. limit for every configured budget, for the given month. */
    public Map<String, Double> getBudgetStatusForMonth(YearMonth month) {
        Map<String, Double> status = new HashMap<>();
        for (String category : budgets.keySet()) {
            status.put(category, getCategoryTotalForMonth(category, month));
        }
        return status;
    }

    // ---------------------------------------------------------------
    // Overall stats
    // ---------------------------------------------------------------

    public double getTotalIncome() {
        return transactions.stream()
                .filter(t -> t.getType() == TransactionType.INCOME)
                .mapToDouble(Transaction::getAmount).sum();
    }

    public double getTotalExpense() {
        return transactions.stream()
                .filter(t -> t.getType() == TransactionType.EXPENSE)
                .mapToDouble(Transaction::getAmount).sum();
    }

    public double getBalance() {
        return getTotalIncome() - getTotalExpense();
    }

    public int getTransactionCount() {
        return transactions.size();
    }

    /** Used by FileHandler when loading from disk, to keep IDs consistent. */
    void loadTransactionDirect(Transaction t) {
        transactions.add(t);
        if (t.getId() >= nextId) {
            nextId = t.getId() + 1;
        }
    }

    void clearAll() {
        transactions.clear();
        nextId = 1;
    }
}

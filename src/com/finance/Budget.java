package com.finance;

/**
 * A monthly spending limit set for one expense category.
 */
public class Budget {
    private final String category;
    private double monthlyLimit;

    public Budget(String category, double monthlyLimit) {
        this.category = category;
        this.monthlyLimit = monthlyLimit;
    }

    public String getCategory() { return category; }
    public double getMonthlyLimit() { return monthlyLimit; }
    public void setMonthlyLimit(double monthlyLimit) { this.monthlyLimit = monthlyLimit; }

    @Override
    public String toString() {
        return String.format("%-15s limit = %.2f", category, monthlyLimit);
    }
}

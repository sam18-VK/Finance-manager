package com.finance;

/**
 * Thrown (as a warning condition the caller chooses how to handle) when
 * adding an expense would push a category's spend past its monthly budget.
 */
public class BudgetExceededException extends Exception {
    private final String category;
    private final double limit;
    private final double totalAfter;

    public BudgetExceededException(String category, double limit, double totalAfter) {
        super(String.format("Budget exceeded for '%s': limit = %.2f, spent so far = %.2f",
                category, limit, totalAfter));
        this.category = category;
        this.limit = limit;
        this.totalAfter = totalAfter;
    }

    public String getCategory() { return category; }
    public double getLimit() { return limit; }
    public double getTotalAfter() { return totalAfter; }
}

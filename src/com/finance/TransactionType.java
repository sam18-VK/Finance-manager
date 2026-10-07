package com.finance;

/**
 * Enum distinguishing money coming in vs. going out.
 * Using an enum (rather than a String/boolean flag) is itself one of the
 * "Core Java Concepts" the project is meant to demonstrate.
 */
public enum TransactionType {
    INCOME,
    EXPENSE
}

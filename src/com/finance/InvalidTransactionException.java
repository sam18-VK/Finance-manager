package com.finance;

/**
 * Thrown when a transaction fails basic validation
 * (e.g. non-positive amount, blank category, null date).
 */
public class InvalidTransactionException extends Exception {
    public InvalidTransactionException(String message) {
        super(message);
    }
}

package dev.coffer.adapter.fabric.execution;

/**
 * Storage-level error.
 */
public class BalanceStoreException extends Exception {
    public BalanceStoreException(String message, Throwable cause) {
        super(message, cause);
    }

    public BalanceStoreException(String message) {
        super(message);
    }
}

package dev.coffer.adapter.fabric.execution;

import java.util.Objects;

/**
 * BALANCE CREDIT PLANNING REFUSAL
 *
 * Responsibility:
 * - Explicit, non-punitive reason why credit planning could not proceed.
 */
public final class BalanceCreditPlanningRefusal {

    private final String code;
    private final String message;

    private BalanceCreditPlanningRefusal(String code, String message) {
        this.code = Objects.requireNonNull(code, "code");
        this.message = Objects.requireNonNull(message, "message");

        if (this.code.isBlank()) {
            throw new IllegalArgumentException("code must be non-empty");
        }
        if (this.message.isBlank()) {
            throw new IllegalArgumentException("message must be non-empty");
        }
    }

    public static BalanceCreditPlanningRefusal of(String code, String message) {
        return new BalanceCreditPlanningRefusal(code, message);
    }

    public String code() {
        return code;
    }

    public String message() {
        return message;
    }
}

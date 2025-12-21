package dev.coffer.adapter.fabric.execution;

import java.util.Objects;

/**
 * Simple success/failure result for adapter execution components.
 *
 * Responsibility:
 * - Represent whether an operation succeeded.
 * - Carry a non-null reason on failure.
 *
 * Invariants:
 * - success=true implies reason=null.
 * - success=false implies reason is non-null/non-blank.
 */
public final class ExecutionResult {
    private final boolean success;
    private final String reason;

    private ExecutionResult(boolean success, String reason) {
        this.success = success;
        this.reason = reason;
    }

    public static ExecutionResult ok() {
        return new ExecutionResult(true, null);
    }

    public static ExecutionResult fail(String reason) {
        Objects.requireNonNull(reason, "reason");
        if (reason.isBlank()) {
            throw new IllegalArgumentException("reason must be non-blank");
        }
        return new ExecutionResult(false, reason);
    }

    public boolean success() {
        return success;
    }

    public String reason() {
        return reason;
    }
}

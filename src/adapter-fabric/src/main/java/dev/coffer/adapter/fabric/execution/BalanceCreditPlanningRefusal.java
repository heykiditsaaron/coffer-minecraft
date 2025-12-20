package dev.coffer.adapter.fabric.execution;

import java.util.Objects;

/**
 * BALANCE CREDIT PLANNING REFUSAL — PHASE 3D.4
 *
 * Represents an explicit, non-punitive reason why a BalanceCreditPlan
 * could not be produced from a Core evaluation result.
 *
 * Rules:
 * - No guessing: refusals must be factual and boring.
 * - No punishment: refusal is not a fault and not an exception.
 * - Adapter-owned: this is for UI/command messaging and auditability.
 *
 * This type contains NO behavior.
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

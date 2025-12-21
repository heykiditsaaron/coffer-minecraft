package dev.coffer.adapter.fabric.execution;

import java.util.Objects;
import java.util.Optional;

/**
 * BALANCE CREDIT PLANNING RESULT
 *
 * Responsibility:
 * - Hold either a planned BalanceCreditPlan or an explicit refusal.
 */
public final class BalanceCreditPlanningResult {

    private final BalanceCreditPlan plan;
    private final BalanceCreditPlanningRefusal refusal;

    private BalanceCreditPlanningResult(
            BalanceCreditPlan plan,
            BalanceCreditPlanningRefusal refusal
    ) {
        this.plan = plan;
        this.refusal = refusal;
    }

    public static BalanceCreditPlanningResult planned(BalanceCreditPlan plan) {
        return new BalanceCreditPlanningResult(
                Objects.requireNonNull(plan, "plan"),
                null
        );
    }

    public static BalanceCreditPlanningResult refused(BalanceCreditPlanningRefusal refusal) {
        return new BalanceCreditPlanningResult(
                null,
                Objects.requireNonNull(refusal, "refusal")
        );
    }

    public boolean planned() {
        return plan != null;
    }

    public Optional<BalanceCreditPlan> plan() {
        return Optional.ofNullable(plan);
    }

    public Optional<BalanceCreditPlanningRefusal> refusal() {
        return Optional.ofNullable(refusal);
    }
}

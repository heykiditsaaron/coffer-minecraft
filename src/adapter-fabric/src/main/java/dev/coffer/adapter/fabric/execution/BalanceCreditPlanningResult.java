package dev.coffer.adapter.fabric.execution;

import java.util.Objects;
import java.util.Optional;

/**
 * BALANCE CREDIT PLANNING RESULT — PHASE 3D.4
 *
 * Explicit outcome of attempting to construct a BalanceCreditPlan from:
 * - Core evaluation result
 * - Core valuation snapshot
 * - adapter-owned identity binding
 *
 * This result is designed to be:
 * - honest (no guessing)
 * - non-punitive (refusal is expected)
 * - UI-friendly (reason is explicit)
 * - audit-friendly (codes are stable)
 *
 * This type contains NO behavior beyond safe construction.
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

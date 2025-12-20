package dev.coffer.adapter.fabric.execution;

import dev.coffer.core.ExchangeEvaluationResult;
import dev.coffer.core.ValuationSnapshot;

import java.util.Objects;
import java.util.UUID;

/**
 * BALANCE CREDIT PLANNER — PHASE 3D.4
 *
 * Adapter-owned planning component responsible for transforming a Core
 * evaluation result into an explicit, immutable BalanceCreditPlan.
 *
 * This class:
 * - performs NO mutation
 * - performs NO execution
 * - performs NO persistence
 * - performs NO recomputation of value
 *
 * It exists solely to absorb the burden of honesty by:
 * - inspecting Core-produced valuation truth
 * - refusing explicitly when planning is impossible
 * - freezing credit intent before any execution occurs
 *
 * All outcomes are explicit and non-punitive.
 */
public final class BalanceCreditPlanner {

    public BalanceCreditPlanner() {
        // stateless
    }

    /**
     * Attempt to construct a BalanceCreditPlan from a Core evaluation result.
     *
     * Planning rules:
     * - Evaluation must be allowed (PASS)
     * - A valuation snapshot must be present
     * - Snapshot must be a ValuationSnapshot
     * - At least one item must have been accepted
     * - Total accepted value must be > 0
     *
     * Any failure results in an explicit planning refusal.
     */
    public BalanceCreditPlanningResult plan(
            UUID targetPlayerId,
            ExchangeEvaluationResult evaluationResult
    ) {
        Objects.requireNonNull(targetPlayerId, "targetPlayerId");
        Objects.requireNonNull(evaluationResult, "evaluationResult");

        // Planning is only meaningful after Core PASS.
        if (!evaluationResult.allowed()) {
            return BalanceCreditPlanningResult.refused(
                    BalanceCreditPlanningRefusal.of(
                            "EVALUATION_DENIED",
                            "Exchange evaluation did not pass; no credit can be planned."
                    )
            );
        }

        Object snapshotObj = evaluationResult.valuationSnapshot();
        if (snapshotObj == null) {
            return BalanceCreditPlanningResult.refused(
                    BalanceCreditPlanningRefusal.of(
                            "MISSING_VALUATION_SNAPSHOT",
                            "No valuation snapshot was provided by Core."
                    )
            );
        }

        if (!(snapshotObj instanceof ValuationSnapshot snapshot)) {
            return BalanceCreditPlanningResult.refused(
                    BalanceCreditPlanningRefusal.of(
                            "UNSUPPORTED_VALUATION_SNAPSHOT",
                            "Valuation snapshot type is not supported by this adapter."
                    )
            );
        }

        if (!snapshot.hasAnyAccepted()) {
            return BalanceCreditPlanningResult.refused(
                    BalanceCreditPlanningRefusal.of(
                            "NO_ACCEPTED_ITEMS",
                            "No accepted items were eligible for credit."
                    )
            );
        }

        long creditAmount = snapshot.totalAcceptedValue();
        if (creditAmount <= 0L) {
            return BalanceCreditPlanningResult.refused(
                    BalanceCreditPlanningRefusal.of(
                            "ZERO_CREDIT_VALUE",
                            "Total accepted value was zero; no credit can be planned."
                    )
            );
        }

        BalanceCreditPlan plan =
                new BalanceCreditPlan(
                        targetPlayerId,
                        creditAmount
                );

        return BalanceCreditPlanningResult.planned(plan);
    }
}

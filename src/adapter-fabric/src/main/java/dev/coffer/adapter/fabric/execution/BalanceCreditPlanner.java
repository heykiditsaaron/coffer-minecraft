package dev.coffer.adapter.fabric.execution;

import dev.coffer.core.ExchangeEvaluationResult;
import dev.coffer.core.ValuationSnapshot;

import java.util.Objects;
import java.util.UUID;

/**
 * BALANCE CREDIT PLANNER
 *
 * Responsibility:
 * - Transform Core evaluation result into an explicit BalanceCreditPlan.
 *
 * Invariants:
 * - Only plans after Core PASS.
 * - Requires ValuationSnapshot with accepted value > 0.
 * - Refuses calmly and explicitly when planning is impossible.
 */
public final class BalanceCreditPlanner {

    public BalanceCreditPlanner() {
        // stateless
    }

    public BalanceCreditPlanningResult plan(
            UUID targetPlayerId,
            ExchangeEvaluationResult evaluationResult
    ) {
        Objects.requireNonNull(targetPlayerId, "targetPlayerId");
        Objects.requireNonNull(evaluationResult, "evaluationResult");

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

        java.util.Map<String, Long> totals = snapshot.totalsByCurrency();
        if (totals.isEmpty()) {
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
                        totals
                );

        return BalanceCreditPlanningResult.planned(plan);
    }
}

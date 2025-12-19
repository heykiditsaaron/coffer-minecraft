package dev.coffer.adapter.fabric.execution;

import dev.coffer.core.ExchangeEvaluationResult;

import java.util.Objects;

/**
 * FABRIC MUTATION EXECUTOR — PHASE 3D PILLAR 1
 *
 * This class defines the execution boundary between truthful evaluation
 * and real-world mutation.
 *
 * In Pillar 1, this executor performs NO mutation.
 * It exists only to prove that the execution path is reached safely.
 */
public final class FabricMutationExecutor {

    public FabricMutationExecutor() {
        // no state
    }

    /**
     * Execute a confirmed exchange.
     *
     * For Pillar 1:
     * - No inventory changes
     * - No balance changes
     * - No rollback logic
     * - No retries
     *
     * This method proves the execution boundary exists.
     */
    public void execute(ExchangeEvaluationResult evaluationResult) {
        Objects.requireNonNull(evaluationResult, "evaluationResult");

        // Intentionally no-op.
        // Future pillars will introduce real mutation here.
    }
}

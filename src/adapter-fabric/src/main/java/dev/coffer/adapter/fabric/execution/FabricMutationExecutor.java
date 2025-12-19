package dev.coffer.adapter.fabric.execution;

import dev.coffer.core.ExchangeEvaluationResult;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.Objects;

/**
 * FABRIC MUTATION EXECUTOR — PHASE 3D.2
 *
 * This class defines the execution boundary between truthful evaluation
 * and real-world mutation.
 *
 * In Phase 3D.2, this executor still performs NO mutation.
 *
 * What Phase 3D.2 adds:
 * - an adapter-owned MutationContext
 * - explicit binding checks between:
 *   - the target player
 *   - the evaluated result
 *   - the adapter's frozen mutation intent
 *
 * This prevents execution from "reconstructing" or "guessing" what to mutate.
 */
public final class FabricMutationExecutor {

    public FabricMutationExecutor() {
        // no state
    }

    /**
     * Execute a confirmed exchange against an adapter-owned mutation plan.
     *
     * Phase 3D.2 behavior:
     * - Validates binding invariants only
     * - Performs NO inventory changes
     * - Performs NO balance changes
     *
     * Fail-closed policy:
     * - If binding fails, execution does not proceed.
     */
    public void execute(
            ServerPlayerEntity targetPlayer,
            ExchangeEvaluationResult evaluationResult,
            MutationContext mutationContext
    ) {
        Objects.requireNonNull(targetPlayer, "targetPlayer");
        Objects.requireNonNull(evaluationResult, "evaluationResult");
        Objects.requireNonNull(mutationContext, "mutationContext");

        // Execution is only meaningful on PASS.
        if (!evaluationResult.allowed()) {
            return;
        }

        // Binding invariant: the mutation plan must target the same player we intend to mutate.
        if (!targetPlayer.getUuid().equals(mutationContext.targetPlayerId())) {
            // Fail closed. No mutation is allowed to occur under mismatch.
            return;
        }

        // Phase 3D.2 ends here intentionally.
        // Future phases will apply actual mutation using mutationContext,
        // but only after additional binding checks against Core acceptance.
    }
}

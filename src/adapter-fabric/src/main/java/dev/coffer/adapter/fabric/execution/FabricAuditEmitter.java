package dev.coffer.adapter.fabric.execution;

import dev.coffer.adapter.fabric.boundary.DeclaredExchangeRequest;
import dev.coffer.core.AuditRecord;
import dev.coffer.core.ExchangeEvaluationResult;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * FABRIC AUDIT EMITTER (PHASE 3B.4).
 *
 * Emits a single audit record per execution attempt.
 *
 * This class:
 * - records facts only
 * - does not interpret meaning
 * - does not enforce persistence
 *
 * Persistence is intentionally deferred.
 */
public final class FabricAuditEmitter {

    /**
     * Emit an audit record for a completed execution.
     *
     * Exactly one audit must be emitted per attempt.
     */
    public AuditRecord emit(
            DeclaredExchangeRequest declared,
            ExchangeEvaluationResult result
    ) {
        Objects.requireNonNull(declared, "declared request must be non-null");
        Objects.requireNonNull(result, "result must be non-null");

        return new AuditRecord(
                UUID.randomUUID(),          // audit id
                declared.target().uuid(),   // account
                result.verdict(),
                result.reasonCodes(),
                result.balanceMutations(),
                Instant.now()
        );
    }
}

package dev.coffer.core;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

/**
 * CORE INVARIANTS (BINDING)
 *
 * 1. Evaluation is deterministic and side-effect free.
 * 2. Exactly one ExchangeEvaluationResult is returned per invocation.
 * 3. Exactly one AuditRecord is emitted per invocation.
 * 4. Evaluation short-circuits on the first denial.
 * 5. No policy layer may mutate state.
 * 6. Valuation produces data only; mutation occurs elsewhere.
 * 7. PASS indicates mutation is possible, not performed.
 * 8. DENY is explicit and final; no stacking occurs.
 * 9. Zero or negative value cannot produce PASS.
 * 10. Core has no knowledge of adapters, storage, or UI.
 *
 * Violation of any invariant indicates a Core bug.
 */
public final class CoreEngine {

    private final List<PolicyLayer> policyLayers;
    private final ValuationService valuationService;
    private final AuditSink auditSink;

    public CoreEngine(
            List<PolicyLayer> policyLayers,
            ValuationService valuationService,
            AuditSink auditSink
    ) {
        this.policyLayers = List.copyOf(
                Objects.requireNonNull(policyLayers, "policyLayers")
        );
        this.valuationService = Objects.requireNonNull(valuationService, "valuationService");
        this.auditSink = Objects.requireNonNull(auditSink, "auditSink");
    }

    /**
     * Evaluate an exchange request through all policy layers and valuation.
     *
     * @param request immutable exchange request
     * @return final evaluation result
     */
    public ExchangeEvaluationResult evaluate(ExchangeRequest request) {
        Objects.requireNonNull(request, "request");

        ExchangeEvaluationResult result;

        for (PolicyLayer layer : policyLayers) {
            PolicyDecision decision = layer.evaluate(request);

            if (decision == null) {
                result = ExchangeEvaluationResult.deny(
                        DenialReason.INTERNAL_INCONSISTENCY
                );
                emitAudit(request, result);
                return result;
            }

            if (!decision.allowed()) {
                result = ExchangeEvaluationResult.deny(
                        decision.denialReason()
                );
                emitAudit(request, result);
                return result;
            }
        }

        ValuationSnapshot snapshot = valuationService.valuate(request);

        if (snapshot == null) {
            result = ExchangeEvaluationResult.deny(
                    DenialReason.INTERNAL_INCONSISTENCY
            );
            emitAudit(request, result);
            return result;
        }

        if (!snapshot.hasAnyAccepted()) {
            result = ExchangeEvaluationResult.deny(
                    DenialReason.INVALID_VALUE
            );
            emitAudit(request, result);
            return result;
        }

        result = ExchangeEvaluationResult.pass(snapshot);
        emitAudit(request, result);
        return result;
    }

    private void emitAudit(
            ExchangeRequest request,
            ExchangeEvaluationResult result
    ) {
        auditSink.emit(
                new AuditRecord(
                        Instant.now(),
                        request,
                        result
                )
        );
    }
}

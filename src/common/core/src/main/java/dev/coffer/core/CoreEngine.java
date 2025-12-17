package dev.coffer.core;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

/**
 * Core Engine — flow orchestration only.
 *
 * Encodes policy order, valuation, denial short-circuiting,
 * and emits audit records for every decision.
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

    public ExchangeEvaluationResult evaluate(ExchangeRequest request) {
        Objects.requireNonNull(request, "request");

        ExchangeEvaluationResult result;

        for (PolicyLayer layer : policyLayers) {
            PolicyDecision decision = layer.evaluate(request);

            if (!decision.allowed()) {
                result = ExchangeEvaluationResult.deny(
                        decision.denialReason()
                );
                emitAudit(request, result);
                return result;
            }
        }

        ValuationSnapshot snapshot = valuationService.valuate(request);

        if (!snapshot.hasAnyAccepted()) {
            result = ExchangeEvaluationResult.deny(DenialReason.INVALID_VALUE);
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

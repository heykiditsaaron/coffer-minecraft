package dev.coffer.core;

import java.util.List;
import java.util.Objects;

/**
 * Core Engine — flow orchestration only.
 *
 * This class encodes policy order and denial short-circuiting.
 * It does NOT implement policy meaning.
 */
public final class CoreEngine {

    private final List<PolicyLayer> policyLayers;
    private final ValuationService valuationService;

    public CoreEngine(
            List<PolicyLayer> policyLayers,
            ValuationService valuationService
    ) {
        this.policyLayers = List.copyOf(
                Objects.requireNonNull(policyLayers, "policyLayers")
        );
        this.valuationService = Objects.requireNonNull(valuationService, "valuationService");
    }

    public ExchangeEvaluationResult evaluate(ExchangeRequest request) {
        Objects.requireNonNull(request, "request");

        for (PolicyLayer layer : policyLayers) {
            PolicyDecision decision = layer.evaluate(request);

            if (!decision.allowed()) {
                return ExchangeEvaluationResult.deny(
                        decision.denialReason()
                );
            }
        }

        ValuationSnapshot snapshot = valuationService.valuate(request);

        if (!snapshot.hasAnyAccepted()) {
            return ExchangeEvaluationResult.deny(DenialReason.INVALID_VALUE);
        }

        return ExchangeEvaluationResult.pass(snapshot);
    }
}

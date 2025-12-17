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

    public CoreEngine(List<PolicyLayer> policyLayers) {
        this.policyLayers = List.copyOf(
                Objects.requireNonNull(policyLayers, "policyLayers")
        );
    }

    /**
     * Evaluate an exchange request through all policy layers.
     *
     * @param request immutable exchange request
     * @return final evaluation result
     */
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

        // No policy denied.
        // Valuation snapshot will be added later.
        return ExchangeEvaluationResult.pass(new Object());
    }
}

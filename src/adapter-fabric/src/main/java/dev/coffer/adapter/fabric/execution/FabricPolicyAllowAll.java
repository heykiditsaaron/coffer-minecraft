package dev.coffer.adapter.fabric.execution;

import dev.coffer.core.ExchangeRequest;
import dev.coffer.core.PolicyDecision;
import dev.coffer.core.PolicyLayer;

import java.util.Objects;

public final class FabricPolicyAllowAll implements PolicyLayer {

    @Override
    public PolicyDecision evaluate(ExchangeRequest request) {
        Objects.requireNonNull(request, "request");
        return PolicyDecision.allow();
    }
}

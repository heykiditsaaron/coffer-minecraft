package dev.coffer.adapter.fabric.execution;

import dev.coffer.adapter.fabric.boundary.DeclaredExchangeRequest;
import dev.coffer.core.CoreEngine;
import dev.coffer.core.ExchangeEvaluationResult;
import dev.coffer.core.ExchangeRequest;

import java.util.Objects;

/**
 * FABRIC CORE EXECUTOR
 *
 * Contract-aligned:
 * - translates adapter request to opaque Core ExchangeRequest
 * - invokes CoreEngine
 * - returns result without interpretation
 */
public final class FabricCoreExecutor {

    private final CoreEngine coreEngine;

    public FabricCoreExecutor(CoreEngine coreEngine) {
        this.coreEngine = Objects.requireNonNull(coreEngine, "coreEngine");
    }

    public ExchangeEvaluationResult execute(DeclaredExchangeRequest declaredRequest) {
        Objects.requireNonNull(declaredRequest, "declaredRequest");

        ExchangeRequest request =
                FabricToCoreTranslator.translate(declaredRequest);

        return coreEngine.evaluate(request);
    }
}

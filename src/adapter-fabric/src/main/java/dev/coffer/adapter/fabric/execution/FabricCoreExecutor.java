package dev.coffer.adapter.fabric.execution;

import dev.coffer.adapter.fabric.boundary.DeclaredExchangeRequest;
import dev.coffer.core.CoreEngine;
import dev.coffer.core.ExchangeEvaluationResult;
import dev.coffer.core.ExchangeRequest;

import java.util.Objects;

/**
 * FABRIC CORE EXECUTOR (PHASE 3B.2).
 *
 * This class is responsible for:
 * - invoking the CoreEngine
 * - returning the Core result unchanged
 *
 * This class does NOT:
 * - interpret results
 * - apply mutations
 * - emit audits
 * - perform permission checks
 *
 * It is a thin execution bridge.
 */
public final class FabricCoreExecutor {

    private final CoreEngine coreEngine;

    public FabricCoreExecutor(CoreEngine coreEngine) {
        this.coreEngine = Objects.requireNonNull(coreEngine, "coreEngine must be non-null");
    }

    /**
     * Execute a declared exchange request through the Core.
     */
    public ExchangeEvaluationResult execute(DeclaredExchangeRequest declaredRequest) {
        Objects.requireNonNull(declaredRequest, "declaredRequest must be non-null");

        ExchangeRequest coreRequest =
                FabricToCoreTranslator.translate(declaredRequest);

        return coreEngine.evaluate(coreRequest);
    }
}

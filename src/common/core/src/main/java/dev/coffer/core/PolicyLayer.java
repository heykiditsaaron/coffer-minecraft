package dev.coffer.core;

/**
 * A single policy layer in the Core evaluation pipeline.
 *
 * Implementations may ONLY:
 * - deny explicitly, or
 * - allow and get out of the way
 *
 * Implementations must not:
 * - mutate state
 * - infer downstream behavior
 * - depend on adapters or platforms
 */
@FunctionalInterface
public interface PolicyLayer {

    PolicyDecision evaluate(ExchangeRequest request);
}

package dev.coffer.core;

import java.util.List;

/**
 * Performs valuation evaluation.
 *
 * This service:
 * - evaluates value
 * - produces a snapshot
 * - does not mutate state
 */
public interface ValuationService {

    ValuationSnapshot valuate(ExchangeRequest request);
}

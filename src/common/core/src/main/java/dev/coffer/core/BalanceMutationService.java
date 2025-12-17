package dev.coffer.core;

/**
 * Performs explicit balance mutations.
 *
 * This service is the ONLY place where balance mutation may occur.
 * It is separate from exchange evaluation by design.
 */
public interface BalanceMutationService {

    BalanceMutationResult apply(BalanceMutation mutation);
}

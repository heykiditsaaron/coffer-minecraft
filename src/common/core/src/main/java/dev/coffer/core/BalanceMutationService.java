package dev.coffer.core;

/**
 * Performs explicit balance mutations.
 *
 * This service is the ONLY place where balance mutation may occur.
 * Implementations must emit audit records.
 */
public interface BalanceMutationService {

    BalanceMutationResult apply(
            BalanceMutation mutation,
            AuditSink auditSink
    );
}

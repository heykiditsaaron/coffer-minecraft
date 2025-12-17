package dev.coffer.core;

/**
 * Sink for audit records emitted by Core.
 *
 * Core does not know where audits go.
 * Implementations decide persistence, logging, or forwarding.
 */
public interface AuditSink {

    void emit(AuditRecord record);
}

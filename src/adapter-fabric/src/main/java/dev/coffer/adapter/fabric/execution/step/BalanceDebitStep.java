package dev.coffer.adapter.fabric.execution.step;

import dev.coffer.adapter.fabric.execution.BalanceStore;
import dev.coffer.adapter.fabric.execution.BalanceStoreException;
import dev.coffer.adapter.fabric.execution.ExecutionResult;
import dev.coffer.adapter.fabric.execution.FabricAuditSink;

import java.util.Objects;
import java.util.UUID;

public final class BalanceDebitStep {
    private final BalanceStore store;
    private final long amount;
    private final String currencyId;
    private final FabricAuditSink auditSink;

    private boolean applied;

    public BalanceDebitStep(BalanceStore store, long amount, String currencyId, FabricAuditSink auditSink) {
        this.store = Objects.requireNonNull(store, "store");
        this.auditSink = Objects.requireNonNull(auditSink, "auditSink");
        this.amount = amount;
        this.currencyId = Objects.requireNonNull(currencyId, "currencyId");
        if (amount <= 0) {
            throw new IllegalArgumentException("amount must be > 0");
        }
    }

    public ExecutionResult apply(UUID playerId) {
        if (applied) return ExecutionResult.fail("ALREADY_APPLIED");
        if (playerId == null) return ExecutionResult.fail("PLAYER_MISSING");
        try {
            long current = store.getBalance(playerId, currencyId);
            if (current < amount) {
                return ExecutionResult.fail("INSUFFICIENT_FUNDS");
            }
            store.applyDelta(playerId, currencyId, -amount);
            applied = true;
            return ExecutionResult.ok();
        } catch (BalanceStoreException e) {
            auditSink.emitAdmin("STORAGE_ERROR", e.getMessage());
            System.err.println("[Coffer][Storage] " + e.getMessage());
            return ExecutionResult.fail("STORAGE_ERROR");
        }
    }

    public void rollback(UUID playerId) {
        if (!applied || playerId == null) return;
        try {
            store.applyDelta(playerId, currencyId, amount);
        } catch (BalanceStoreException e) {
            auditSink.emitAdmin("STORAGE_ERROR", e.getMessage());
            System.err.println("[Coffer][Storage] " + e.getMessage());
        } finally {
            applied = false;
        }
    }
}

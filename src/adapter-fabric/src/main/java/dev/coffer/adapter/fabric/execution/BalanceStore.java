package dev.coffer.adapter.fabric.execution;

import java.util.UUID;

/**
 * Persistent balance store.
 */
public interface BalanceStore {
    long getBalance(UUID account, String currencyId) throws BalanceStoreException;

    void applyDelta(UUID account, String currencyId, long delta) throws BalanceStoreException;
}

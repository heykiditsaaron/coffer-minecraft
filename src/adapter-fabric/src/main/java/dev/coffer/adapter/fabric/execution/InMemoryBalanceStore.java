package dev.coffer.adapter.fabric.execution;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * IN-MEMORY BALANCE STORE
 *
 * Responsibility:
 * - Adapter-owned, non-persistent balance storage for testing/diagnostic flows.
 *
 * Invariants:
 * - No persistence guarantees.
 * - Thread-safe map-backed deltas.
 */
public final class InMemoryBalanceStore {

    private final Map<UUID, Long> balances = new ConcurrentHashMap<>();

    public long getBalance(UUID account) {
        return balances.getOrDefault(account, 0L);
    }

    public void applyDelta(UUID account, long delta) {
        balances.merge(account, delta, Long::sum);
    }
}

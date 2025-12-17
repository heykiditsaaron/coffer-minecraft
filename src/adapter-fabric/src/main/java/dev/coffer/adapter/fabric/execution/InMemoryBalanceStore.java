package dev.coffer.adapter.fabric.execution;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * IN-MEMORY BALANCE STORE (PHASE 3B.3).
 *
 * This store is:
 * - volatile
 * - non-persistent
 * - adapter-owned
 *
 * It exists solely to prove mutation execution.
 *
 * DO NOT assume durability.
 * DO NOT assume restart survival.
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

package dev.coffer.adapter.fabric;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

/**
 * FABRIC ADAPTER — RUNTIME "DOOR" (PHASE 3.A).
 *
 * This class is the single, stable entry point for all Fabric-side callers:
 * - commands
 * - UI
 * - other mods / integrations (non-command invocation)
 *
 * IMPORTANT:
 * - This class does NOT implement economic meaning.
 * - This class does NOT call Core in Phase 3.A.
 * - This class ONLY enforces operational readiness and provides explicit refusals.
 *
 * Future phases will add methods that declare inputs to Core and route evaluation,
 * but those methods MUST still funnel through this runtime door.
 */
public final class CofferFabricRuntime {

    private static final AtomicReference<CofferFabricRuntime> INSTANCE = new AtomicReference<>(null);

    private final AtomicReference<CofferFabricState> state;
    private final AtomicReference<CofferFabricRefusal> failureRefusal;

    private CofferFabricRuntime() {
        this.state = new AtomicReference<>(CofferFabricState.UNINITIALIZED);
        this.failureRefusal = new AtomicReference<>(null);
    }

    /**
     * Create the singleton runtime exactly once per server instance.
     * If already created, returns the existing instance.
     */
    public static CofferFabricRuntime createOrGet() {
        CofferFabricRuntime existing = INSTANCE.get();
        if (existing != null) return existing;

        CofferFabricRuntime created = new CofferFabricRuntime();
        if (INSTANCE.compareAndSet(null, created)) {
            return created;
        }
        return Objects.requireNonNull(INSTANCE.get(), "Runtime instance unexpectedly null after CAS failure");
    }

    /**
     * Get the runtime if it exists (no creation).
     */
    public static Optional<CofferFabricRuntime> getIfPresent() {
        return Optional.ofNullable(INSTANCE.get());
    }

    /**
     * Reset the singleton for server stop boundaries.
     * This is operational only; it must not be used to "recover" from failures.
     */
    public static void clearForServerStop() {
        INSTANCE.set(null);
    }

    public CofferFabricState getState() {
        return state.get();
    }

    public boolean isReady() {
        return state.get() == CofferFabricState.READY;
    }

    /**
     * Transition to INITIALIZING. Safe to call multiple times.
     */
    public void markInitializing() {
        state.set(CofferFabricState.INITIALIZING);
        failureRefusal.set(null);
    }

    /**
     * Transition to READY. This indicates the adapter may accept requests.
     */
    public void markReady() {
        state.set(CofferFabricState.READY);
        failureRefusal.set(null);
    }

    /**
     * Enter reload refusal boundary. While in RELOADING, all entry attempts MUST refuse.
     *
     * Phase 3.A does not implement reload, but MUST leave room for it.
     */
    public void beginReload() {
        state.set(CofferFabricState.RELOADING);
    }

    /**
     * Exit reload boundary. If a failure was recorded, the adapter remains FAILED.
     */
    public void endReload() {
        if (failureRefusal.get() != null) {
            state.set(CofferFabricState.FAILED);
            return;
        }
        state.set(CofferFabricState.READY);
    }

    /**
     * Transition to FAILED with an explicit adapter-level refusal.
     * This must NEVER be represented as a Core denial reason.
     */
    public void markFailed(CofferFabricRefusal refusal) {
        if (refusal == null) {
            throw new IllegalArgumentException("refusal must be non-null");
        }
        failureRefusal.set(refusal);
        state.set(CofferFabricState.FAILED);
    }

    /**
     * Returns an explicit adapter-level refusal if the runtime is not READY.
     * This is the canonical "refuse early" mechanism for Phase 3.A.
     */
    public Optional<CofferFabricRefusal> refuseIfNotReady() {
        CofferFabricState s = state.get();

        if (s == CofferFabricState.READY) {
            return Optional.empty();
        }

        CofferFabricRefusal failure = failureRefusal.get();
        if (failure != null) {
            return Optional.of(failure);
        }

        // Non-failure refusal states: explicit, boring, and non-semantic.
        return Optional.of(CofferFabricRefusal.of(
                "ADAPTER_NOT_READY",
                "Coffer is not ready to accept requests (" + s + ")."
        ));
    }
}

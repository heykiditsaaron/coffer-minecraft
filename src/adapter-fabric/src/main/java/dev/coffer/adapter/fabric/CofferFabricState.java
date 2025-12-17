package dev.coffer.adapter.fabric;

/**
 * FABRIC ADAPTER — OPERATIONAL STATE ONLY.
 *
 * This state machine is NOT Core semantics.
 * It exists only to answer: "May the adapter accept requests right now?"
 *
 * - No economic meaning is encoded here.
 * - No valuation meaning is encoded here.
 * - No policy meaning is encoded here.
 *
 * Any non-READY state MUST result in explicit adapter-level refusal.
 */
public enum CofferFabricState {
    /**
     * Adapter has not been initialized for a server instance.
     */
    UNINITIALIZED,

    /**
     * Adapter is constructing its runtime "door" and wiring lifecycle boundaries.
     */
    INITIALIZING,

    /**
     * Adapter runtime is available and may accept requests.
     */
    READY,

    /**
     * Adapter is intentionally refusing all requests due to reload boundary.
     */
    RELOADING,

    /**
     * Adapter encountered an unrecoverable initialization/runtime failure
     * and MUST refuse all requests explicitly.
     */
    FAILED
}

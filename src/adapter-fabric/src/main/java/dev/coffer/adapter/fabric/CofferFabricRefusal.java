package dev.coffer.adapter.fabric;

/**
 * FABRIC ADAPTER — REFUSAL SHAPE ONLY.
 *
 * This is an adapter-level refusal, NOT a Core denial reason.
 * It exists to truthfully refuse when the adapter cannot (or must not) proceed:
 * - not READY
 * - reload in progress
 * - missing declared inputs
 * - unsupported/ambiguous platform data (e.g., metadata uncertainty)
 *
 * This type must remain non-semantic and non-authoritative.
 */
public record CofferFabricRefusal(
        String refusalCode,
        String message
) {
    public static CofferFabricRefusal of(String refusalCode, String message) {
        if (refusalCode == null || refusalCode.isBlank()) {
            throw new IllegalArgumentException("refusalCode must be non-empty");
        }
        if (message == null || message.isBlank()) {
            throw new IllegalArgumentException("message must be non-empty");
        }
        return new CofferFabricRefusal(refusalCode, message);
    }
}

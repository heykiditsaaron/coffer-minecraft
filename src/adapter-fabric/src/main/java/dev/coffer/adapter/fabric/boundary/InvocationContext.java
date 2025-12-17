package dev.coffer.adapter.fabric.boundary;

import java.util.Optional;
import java.util.UUID;

/**
 * FABRIC ADAPTER — INVOCATION CONTEXT ONLY (PHASE 3.B).
 *
 * This preserves the rule: "Commands are callers, not owners."
 * Any caller (command/UI/mod/system) declares who invoked and who is targeted.
 *
 * This is NOT a permission system.
 * Permission enforcement remains adapter-owned and configuration-owned.
 */
public record InvocationContext(
        InvokerKind invokerKind,
        Optional<UUID> invokerUuid
) {
    public InvocationContext {
        if (invokerKind == null) throw new IllegalArgumentException("invokerKind must be non-null");
        if (invokerUuid == null) throw new IllegalArgumentException("invokerUuid must be non-null (use Optional.empty())");
    }

    public static InvocationContext player(UUID uuid) {
        return new InvocationContext(InvokerKind.PLAYER, Optional.ofNullable(uuid));
    }

    public static InvocationContext console() {
        return new InvocationContext(InvokerKind.CONSOLE, Optional.empty());
    }

    public static InvocationContext mod(Optional<UUID> invokerUuid) {
        return new InvocationContext(InvokerKind.MOD, invokerUuid == null ? Optional.empty() : invokerUuid);
    }

    public static InvocationContext system() {
        return new InvocationContext(InvokerKind.SYSTEM, Optional.empty());
    }
}

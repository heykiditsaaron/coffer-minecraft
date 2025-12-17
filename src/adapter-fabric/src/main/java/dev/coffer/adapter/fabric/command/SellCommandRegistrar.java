package dev.coffer.adapter.fabric.command;

import dev.coffer.adapter.fabric.CofferFabricRuntime;
import dev.coffer.adapter.fabric.CofferFabricRefusal;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

import java.util.Optional;

/**
 * FABRIC ADAPTER — SELL COMMAND (PHASE 3.E).
 *
 * Diagnostic-only bulk liquidation entry point.
 *
 * This command exists to:
 * - prove aggregation entry exists
 * - prove routing through the runtime door
 * - prove explicit refusal under adapter constraints
 *
 * IMPORTANT:
 * - NO inventory UI
 * - NO item parsing
 * - NO Core calls
 * - NO mutation
 *
 * Aggregation semantics are intentionally deferred.
 */
public final class SellCommandRegistrar {

    private SellCommandRegistrar() {
        // static-only
    }

    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            dispatcher.register(
                    CommandManager.literal("sell")
                            .executes(context -> {
                                ServerCommandSource source = context.getSource();

                                Optional<CofferFabricRefusal> refusal =
                                        CofferFabricRuntime.getIfPresent()
                                                .flatMap(CofferFabricRuntime::refuseIfNotReady);

                                if (refusal.isPresent()) {
                                    source.sendError(
                                            Text.literal("[Coffer] " + refusal.get().message())
                                    );
                                    return 0;
                                }

                                // Phase 3.E (Option A): diagnostic-only acknowledgement.
                                source.sendFeedback(
                                        () -> Text.literal(
                                                "[Coffer] Sell request acknowledged (diagnostic only)."
                                        ),
                                        false
                                );
                                return 1;
                            })
            );
        });
    }
}

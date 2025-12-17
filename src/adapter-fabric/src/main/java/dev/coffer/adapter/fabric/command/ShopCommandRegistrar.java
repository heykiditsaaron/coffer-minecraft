package dev.coffer.adapter.fabric.command;

import dev.coffer.adapter.fabric.CofferFabricRuntime;
import dev.coffer.adapter.fabric.CofferFabricRefusal;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

import java.util.Optional;

/**
 * FABRIC ADAPTER — SHOP COMMAND (PHASE 3.D).
 *
 * Diagnostic-only entry point for Admin Shop UI access.
 * This command proves:
 * - permission-gated access (stubbed here)
 * - routing through the runtime door
 * - explicit refusal when unavailable
 *
 * NO UI is opened yet.
 * NO Core calls occur.
 * NO inventory interaction exists.
 */
public final class ShopCommandRegistrar {

    private ShopCommandRegistrar() {
        // static-only
    }

    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            dispatcher.register(
                    CommandManager.literal("shop")
                            .then(CommandManager.literal("open")
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

                                        // Phase 3.D (Option A): diagnostic-only acknowledgement.
                                        source.sendFeedback(
                                                () -> Text.literal(
                                                        "[Coffer] Shop access acknowledged (diagnostic only)."
                                                ),
                                                false
                                        );
                                        return 1;
                                    })
                            )
            );
        });
    }
}

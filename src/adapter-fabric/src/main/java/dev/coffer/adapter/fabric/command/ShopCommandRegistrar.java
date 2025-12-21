package dev.coffer.adapter.fabric.command;

import dev.coffer.adapter.fabric.CofferFabricRefusal;
import dev.coffer.adapter.fabric.CofferFabricRuntime;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

import java.util.Optional;

/**
 * FABRIC ADAPTER — SHOP COMMAND (DIAGNOSTIC)
 *
 * Responsibility:
 * - Register diagnostic `/shop open` entry.
 * - Route through runtime door; refuse when not READY.
 *
 * Not responsible for:
 * - UI, Core invocation, or permissions (beyond runtime readiness).
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

package dev.coffer.adapter.fabric.command;

import dev.coffer.adapter.fabric.CofferFabricRefusal;
import dev.coffer.adapter.fabric.CofferFabricRuntime;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;

import java.util.Optional;

/**
 * FABRIC ADAPTER — COMMAND REGISTRAR
 *
 * Responsibility:
 * - Register diagnostic `/coffer` command.
 * - Route through the runtime door and refuse when not READY.
 *
 * Not responsible for:
 * - Core invocation or economic meaning.
 */
public final class CofferCommandRegistrar {

    private CofferCommandRegistrar() {
        // static-only
    }

    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            dispatcher.register(
                    CommandManager.literal("coffer")
                            .executes(context -> {
                                ServerCommandSource source = context.getSource();

                                Optional<CofferFabricRefusal> refusal =
                                        CofferFabricRuntime.getIfPresent()
                                                .flatMap(CofferFabricRuntime::refuseIfNotReady);

                                if (refusal.isPresent()) {
                                    source.sendError(
                                            net.minecraft.text.Text.literal(
                                                    "[Coffer] " + refusal.get().message()
                                            )
                                    );
                                    return 0;
                                }

                                source.sendFeedback(
                                        () -> net.minecraft.text.Text.literal(
                                                "[Coffer] Adapter is READY (diagnostic)."
                                        ),
                                        false
                                );
                                return 1;
                            })
            );
        });
    }
}

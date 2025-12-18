package dev.coffer.adapter.fabric.command;

import dev.coffer.adapter.fabric.boundary.DeclaredExchangeRequest;
import dev.coffer.adapter.fabric.declaration.InventoryDeclarationBuilder;
import dev.coffer.adapter.fabric.execution.FabricCoreExecutor;
import dev.coffer.core.ExchangeEvaluationResult;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

import java.util.Optional;

/**
 * SELL COMMAND — PHASE 3C.2
 *
 * This command captures player intent and delegates declaration construction.
 *
 * Phase 3C.2:
 * - Inventory-backed declaration is attempted first.
 * - If no truthful declaration exists, the adapter refuses BEFORE Core invocation.
 *
 * Mutation is still not performed in this phase.
 */
public final class SellCommandRegistrar {

    private SellCommandRegistrar() {
        // static-only
    }

    public static void register(FabricCoreExecutor executor) {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
                dispatcher.register(
                        CommandManager.literal("sell")
                                .executes(ctx -> execute(ctx.getSource(), executor))
                )
        );
    }

    private static int execute(ServerCommandSource source, FabricCoreExecutor executor) {
        if (source.getPlayer() == null) {
            source.sendError(Text.literal("[Coffer] Players only."));
            return 0;
        }

        Optional<DeclaredExchangeRequest> maybeRequest =
                InventoryDeclarationBuilder.fromPlayer(source.getPlayer());

        if (maybeRequest.isEmpty()) {
            // Phase 3C.2: refuse if no truthful exchange declaration exists.
            source.sendError(Text.literal("[Coffer] Nothing to sell (no owned items eligible)."));
            return 0;
        }

        DeclaredExchangeRequest request = maybeRequest.get();
        ExchangeEvaluationResult result = executor.execute(request);

        if (!result.allowed()) {
            source.sendError(
                    Text.literal("[Coffer] Denied: " + result.denialReason())
            );
            return 0;
        }

        // Phase 3C.2: evaluation only — no mutation
        source.sendFeedback(
                () -> Text.literal("[Coffer] Sell evaluated successfully."),
                false
        );

        return 1;
    }
}

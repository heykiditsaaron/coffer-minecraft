package dev.coffer.adapter.fabric.command;

import dev.coffer.adapter.fabric.boundary.DeclaredExchangeRequest;
import dev.coffer.adapter.fabric.boundary.DeclaredIdentity;
import dev.coffer.adapter.fabric.boundary.DeclaredItem;
import dev.coffer.adapter.fabric.boundary.ExchangeIntent;
import dev.coffer.adapter.fabric.boundary.InvocationContext;
import dev.coffer.adapter.fabric.boundary.MetadataRelevance;
import dev.coffer.adapter.fabric.execution.FabricCoreExecutor;
import dev.coffer.core.ExchangeEvaluationResult;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

import java.util.List;
import java.util.UUID;

/**
 * SELL COMMAND — PHASE 3C.1
 *
 * This command currently exercises evaluation ONLY.
 * No mutation is performed until a real inventory-backed
 * sell flow exists.
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

        UUID playerId = source.getPlayer().getUuid();

        // Placeholder declaration — inventory-backed selection will replace this
        DeclaredExchangeRequest request =
                new DeclaredExchangeRequest(
                        ExchangeIntent.SELL,
                        InvocationContext.player(playerId),
                        DeclaredIdentity.of(playerId),
                        List.of(
                                DeclaredItem.withoutMetadata(
                                        "minecraft:dirt",
                                        1,
                                        MetadataRelevance.IGNORED_BY_DECLARATION
                                )
                        )
                );

        ExchangeEvaluationResult result = executor.execute(request);

        if (!result.allowed()) {
            source.sendError(
                    Text.literal("[Coffer] Denied: " + result.denialReason())
            );
            return 0;
        }

        // Phase 3C.1: evaluation only — no mutation
        source.sendFeedback(
                () -> Text.literal("[Coffer] Sell evaluated successfully."),
                false
        );

        return 1;
    }
}

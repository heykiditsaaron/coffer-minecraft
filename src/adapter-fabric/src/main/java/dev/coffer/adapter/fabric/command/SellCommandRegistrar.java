package dev.coffer.adapter.fabric.command;

import dev.coffer.adapter.fabric.boundary.DeclaredExchangeRequest;
import dev.coffer.adapter.fabric.boundary.DeclaredIdentity;
import dev.coffer.adapter.fabric.boundary.DeclaredItem;
import dev.coffer.adapter.fabric.boundary.ExchangeIntent;
import dev.coffer.adapter.fabric.boundary.InvocationContext;
import dev.coffer.adapter.fabric.boundary.MetadataRelevance;
import dev.coffer.adapter.fabric.execution.FabricCoreExecutor;
import dev.coffer.adapter.fabric.execution.FabricMutationExecutor;
import dev.coffer.core.ExchangeEvaluationResult;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

import java.util.List;
import java.util.UUID;

/**
 * SELL COMMAND — PHASE 3D PILLAR 1
 *
 * This command now reaches the mutation execution boundary.
 * No real mutation is performed yet.
 */
public final class SellCommandRegistrar {

    private SellCommandRegistrar() {
        // static-only
    }

    public static void register(FabricCoreExecutor executor) {
        FabricMutationExecutor mutationExecutor = new FabricMutationExecutor();

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
                dispatcher.register(
                        CommandManager.literal("sell")
                                .executes(ctx -> execute(ctx.getSource(), executor, mutationExecutor))
                )
        );
    }

    private static int execute(
            ServerCommandSource source,
            FabricCoreExecutor coreExecutor,
            FabricMutationExecutor mutationExecutor
    ) {
        if (source.getPlayer() == null) {
            source.sendError(Text.literal("[Coffer] Players only."));
            return 0;
        }

        UUID playerId = source.getPlayer().getUuid();

        // Placeholder declaration — inventory-backed selection comes later
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

        ExchangeEvaluationResult result = coreExecutor.execute(request);

        if (!result.allowed()) {
            source.sendError(
                    Text.literal("[Coffer] Denied.")
            );
            return 0;
        }

        // Phase 3D Pillar 1:
        // Reach execution boundary (no mutation yet)
        mutationExecutor.execute(result);

        source.sendFeedback(
                () -> Text.literal("[Coffer] Exchange execution reached. No changes were made."),
                false
        );

        return 1;
    }
}

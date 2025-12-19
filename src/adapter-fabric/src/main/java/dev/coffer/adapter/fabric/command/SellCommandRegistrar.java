package dev.coffer.adapter.fabric.command;

import dev.coffer.adapter.fabric.boundary.DeclaredExchangeRequest;
import dev.coffer.adapter.fabric.boundary.DeclaredIdentity;
import dev.coffer.adapter.fabric.boundary.DeclaredItem;
import dev.coffer.adapter.fabric.boundary.ExchangeIntent;
import dev.coffer.adapter.fabric.boundary.InvocationContext;
import dev.coffer.adapter.fabric.boundary.MetadataRelevance;
import dev.coffer.adapter.fabric.execution.FabricCoreExecutor;
import dev.coffer.adapter.fabric.execution.FabricMutationExecutor;
import dev.coffer.adapter.fabric.execution.MutationContext;
import dev.coffer.core.ExchangeEvaluationResult;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.item.Items;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.List;
import java.util.UUID;

/**
 * SELL COMMAND — PHASE 3D.2
 *
 * This command constructs an adapter-owned MutationContext to prevent
 * mutation execution from reconstructing or guessing what should be mutated.
 *
 * IMPORTANT:
 * - This command still uses a placeholder declared exchange request for now.
 * - Phase 3D.2 performs NO mutation.
 * - This phase only proves truthful ownership gating + execution binding.
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
        ServerPlayerEntity player = source.getPlayer();

        if (player == null) {
            source.sendError(Text.literal("[Coffer] Players only."));
            return 0;
        }

        UUID playerId = player.getUuid();

        // PHASE 3D.2: adapter-truth ownership gating (minimal, explicit, placeholder-aligned)
        //
        // For now, the declared request still references a single dirt item.
        // Therefore the mutation plan must also be derived from owned inventory truth for dirt,
        // otherwise we refuse to proceed (no truthful exchange exists).
        int ownedDirtCount = player.getInventory().count(Items.DIRT);
        if (ownedDirtCount <= 0) {
            source.sendError(Text.literal("[Coffer] Nothing to sell (no owned items eligible)."));
            return 0;
        }

        MutationContext mutationContext =
                new MutationContext(
                        playerId,
                        List.of(
                                new MutationContext.PlannedRemoval("minecraft:dirt", 1)
                        )
                );

        // Placeholder declaration — inventory-backed selection comes later (Phase 3C.2 / sell menu)
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
            source.sendError(Text.literal("[Coffer] Denied."));
            return 0;
        }

        // PHASE 3D.2:
        // Reach execution boundary WITH an explicit, adapter-owned mutation plan.
        // Still no real mutation occurs.
        mutationExecutor.execute(player, result, mutationContext);

        source.sendFeedback(
                () -> Text.literal("[Coffer] Exchange execution reached. No changes were made."),
                false
        );

        return 1;
    }
}

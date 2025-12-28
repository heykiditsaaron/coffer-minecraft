package dev.coffer.adapter.fabric.command;

import dev.coffer.adapter.fabric.boundary.DeclaredExchangeRequest;
import dev.coffer.adapter.fabric.boundary.DeclaredItem;
import dev.coffer.adapter.fabric.execution.MutationContext;
import dev.coffer.adapter.fabric.execution.ui.SellPreviewManager;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * SELL COMMAND
 *
 * Responsibility:
 * - Single-step sell: declaration -> Core eval -> planning -> atomic execution.
 * - Calm refusals; no intermediate preview state.
 *
 * Not responsible for:
 * - Building declarations (uses InventoryDeclarationBuilder).
 * - Planning credit (uses BalanceCreditPlanner).
 * - Performing mutation (uses FabricMutationTransactionExecutor).
 *
 * Invariants:
 * - Refuses if runtime not READY.
 * - No mutation without Core PASS, planned credit, and matching identities.
 * - Absence of eligible items is a calm, non-error outcome.
 */
public final class SellCommandRegistrar {

    private SellCommandRegistrar() {
        // static-only
    }

    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
                dispatcher.register(
                        CommandManager.literal("sell")
                                .requires(src -> PermissionGate.hasPermission(src, "coffer.command.sell.execute", 0))
                                .executes(ctx -> execute(ctx.getSource()))
                )
        );
    }

    private static int execute(ServerCommandSource source) {
        ServerPlayerEntity player = source.getPlayer();
        if (player == null) {
            source.sendError(Text.literal("[Coffer] Players only."));
            return 0;
        }

        SellPreviewManager.openPreview(player);
        return 1;
    }

    public static MutationContext toMutationContext(UUID playerId, DeclaredExchangeRequest request) {
        List<MutationContext.PlannedRemoval> removals = new ArrayList<>();
        for (DeclaredItem item : request.items()) {
            if (item.count() <= 0) {
                continue;
            }
            int qty = Math.toIntExact(item.count());
            removals.add(new MutationContext.PlannedRemoval(item.itemId(), qty));
        }
        return new MutationContext(playerId, removals);
    }
}

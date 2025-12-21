package dev.coffer.adapter.fabric.command;

import dev.coffer.adapter.fabric.CofferFabricRefusal;
import dev.coffer.adapter.fabric.CofferFabricRuntime;
import dev.coffer.adapter.fabric.boundary.DeclaredExchangeRequest;
import dev.coffer.adapter.fabric.boundary.DeclaredItem;
import dev.coffer.adapter.fabric.declaration.InventoryDeclarationBuilder;
import dev.coffer.adapter.fabric.execution.BalanceCreditPlan;
import dev.coffer.adapter.fabric.execution.BalanceCreditPlanner;
import dev.coffer.adapter.fabric.execution.BalanceCreditPlanningResult;
import dev.coffer.adapter.fabric.execution.FabricCoreExecutor;
import dev.coffer.adapter.fabric.execution.FabricMutationTransactionExecutor;
import dev.coffer.adapter.fabric.execution.MutationContext;
import dev.coffer.core.ExchangeEvaluationResult;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * SELL COMMAND
 *
 * Responsibility:
 * - Entry surface only; delegates declaration, evaluation, planning, execution.
 * - Surfaces explicit, calm feedback.
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

    public static void register(FabricCoreExecutor coreExecutor) {
        Objects.requireNonNull(coreExecutor, "coreExecutor");

        FabricMutationTransactionExecutor mutationExecutor = new FabricMutationTransactionExecutor();
        BalanceCreditPlanner creditPlanner = new BalanceCreditPlanner();

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
                dispatcher.register(
                        CommandManager.literal("sell")
                                .executes(ctx -> execute(ctx.getSource(), coreExecutor, mutationExecutor, creditPlanner))
                )
        );
    }

    private static int execute(
            ServerCommandSource source,
            FabricCoreExecutor coreExecutor,
            FabricMutationTransactionExecutor mutationExecutor,
            BalanceCreditPlanner creditPlanner
    ) {
        ServerPlayerEntity player = source.getPlayer();
        if (player == null) {
            source.sendError(Text.literal("[Coffer] Players only."));
            return 0;
        }

        Optional<CofferFabricRefusal> readiness =
                CofferFabricRuntime.getIfPresent()
                        .flatMap(CofferFabricRuntime::refuseIfNotReady);

        if (readiness.isPresent()) {
            source.sendError(Text.literal("[Coffer] " + readiness.get().message()));
            return 0;
        }

        Optional<DeclaredExchangeRequest> maybeRequest =
                InventoryDeclarationBuilder.fromPlayer(player);

        if (maybeRequest.isEmpty()) {
            source.sendFeedback(() -> Text.literal("[Coffer] Nothing to sell right now."), false);
            return 1;
        }

        DeclaredExchangeRequest request = maybeRequest.get();

        ExchangeEvaluationResult evaluationResult = coreExecutor.execute(request);

        if (!evaluationResult.allowed()) {
            source.sendFeedback(() -> Text.literal("[Coffer] Denied. No changes were made."), false);
            return 1;
        }

        BalanceCreditPlanningResult planningResult =
                creditPlanner.plan(player.getUuid(), evaluationResult);

        if (!planningResult.planned()) {
            String message = planningResult.refusal()
                    .map(r -> r.message())
                    .orElse("Unable to plan credit. No changes were made.");
            source.sendFeedback(() -> Text.literal("[Coffer] " + message), false);
            return 1;
        }

        BalanceCreditPlan creditPlan = planningResult.plan().orElseThrow();

        MutationContext mutationContext = toMutationContext(player.getUuid(), request);

        var execResult =
                mutationExecutor.executeAtomic(player, evaluationResult, mutationContext, creditPlan);

        if (!execResult.success()) {
            source.sendFeedback(
                    () -> Text.literal("[Coffer] No changes were made (" + execResult.reason() + ")."),
                    false
            );
            return 1;
        }

        source.sendFeedback(
                () -> Text.literal("[Coffer] Sold."),
                false
        );
        return 1;
    }

    private static MutationContext toMutationContext(UUID playerId, DeclaredExchangeRequest request) {
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

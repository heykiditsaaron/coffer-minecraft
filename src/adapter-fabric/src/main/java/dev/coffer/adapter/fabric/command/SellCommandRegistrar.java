package dev.coffer.adapter.fabric.command;

import dev.coffer.adapter.fabric.CofferFabricRefusal;
import dev.coffer.adapter.fabric.CofferFabricRuntime;
import dev.coffer.adapter.fabric.boundary.*;
import dev.coffer.adapter.fabric.execution.*;
import dev.coffer.core.CoreEngine;
import dev.coffer.core.ExchangeEvaluationResult;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * FABRIC ADAPTER — SELL COMMAND (PHASE 3B.5).
 *
 * This command is now a REAL execution path.
 *
 * Still:
 * - no UI
 * - no persistence
 * - no metadata parsing
 *
 * This exists so the economy can be PLAYED and FELT.
 */
public final class SellCommandRegistrar {

    private static final InMemoryBalanceStore BALANCE_STORE = new InMemoryBalanceStore();

    private static final FabricMutationExecutor MUTATION_EXECUTOR =
            new FabricMutationExecutor(BALANCE_STORE);

    private static final FabricAuditEmitter AUDIT_EMITTER =
            new FabricAuditEmitter();

    private SellCommandRegistrar() {
        // static-only
    }

    public static void register(CoreEngine coreEngine) {
        FabricCoreExecutor executor = new FabricCoreExecutor(coreEngine);

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            dispatcher.register(
                    CommandManager.literal("sell")
                            .executes(context -> executeSell(context.getSource(), executor))
            );
        });
    }

    private static int executeSell(ServerCommandSource source, FabricCoreExecutor executor) {
        Optional<CofferFabricRefusal> refusal =
                CofferFabricRuntime.getIfPresent()
                        .flatMap(CofferFabricRuntime::refuseIfNotReady);

        if (refusal.isPresent()) {
            source.sendError(Text.literal("[Coffer] " + refusal.get().message()));
            return 0;
        }

        UUID playerId = source.getPlayer() != null
                ? source.getPlayer().getUuid()
                : null;

        if (playerId == null) {
            source.sendError(Text.literal("[Coffer] Only players may sell items."));
            return 0;
        }

        // Phase 3B.5: minimal declared request (single placeholder item)
        DeclaredExchangeRequest declared = new DeclaredExchangeRequest(
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

        ExchangeEvaluationResult result;
        try {
            result = executor.execute(declared);
        } catch (Exception e) {
            source.sendError(Text.literal("[Coffer] Execution failed: " + e.getMessage()));
            return 0;
        }

        if (!result.verdict().isAccepted()) {
            source.sendError(Text.literal("[Coffer] Sell denied: " + result.verdict()));
            return 0;
        }

        MUTATION_EXECUTOR.apply(result);
        AUDIT_EMITTER.emit(declared, result);

        long newBalance = BALANCE_STORE.getBalance(playerId);

        source.sendFeedback(
                () -> Text.literal("[Coffer] Sell accepted. New balance: " + newBalance),
                false
        );

        return 1;
    }
}

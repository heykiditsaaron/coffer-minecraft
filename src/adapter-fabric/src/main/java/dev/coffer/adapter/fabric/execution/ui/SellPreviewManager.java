package dev.coffer.adapter.fabric.execution.ui;

import dev.coffer.adapter.fabric.AdapterServices;
import dev.coffer.adapter.fabric.CofferFabricRefusal;
import dev.coffer.adapter.fabric.CofferFabricRuntime;
import dev.coffer.adapter.fabric.boundary.DeclaredExchangeRequest;
import dev.coffer.adapter.fabric.boundary.DeclaredIdentity;
import dev.coffer.adapter.fabric.boundary.DeclaredItem;
import dev.coffer.adapter.fabric.boundary.ExchangeIntent;
import dev.coffer.adapter.fabric.boundary.InvocationContext;
import dev.coffer.adapter.fabric.boundary.MetadataRelevance;
import dev.coffer.adapter.fabric.execution.BalanceCreditPlan;
import dev.coffer.adapter.fabric.execution.BalanceCreditPlanner;
import dev.coffer.adapter.fabric.execution.BalanceCreditPlanningResult;
import dev.coffer.adapter.fabric.execution.FabricCoreExecutor;
import dev.coffer.adapter.fabric.execution.MoneyFormatter;
import dev.coffer.adapter.fabric.execution.ExecutionResult;
import dev.coffer.adapter.fabric.execution.MutationContext;
import dev.coffer.adapter.fabric.execution.step.BalanceCreditStep;
import dev.coffer.adapter.fabric.execution.step.ContainerRemovalStep;
import dev.coffer.core.ExchangeEvaluationResult;
import dev.coffer.core.ValuationSnapshot;
import dev.coffer.core.ExchangeRequest;
import dev.coffer.adapter.fabric.execution.FabricValuationService;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.registry.Registries;
import net.minecraft.item.ItemStack;
import java.util.ArrayList;
import java.util.List;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages sell preview tokens and confirmation flow.
 */
public final class SellPreviewManager {

    private static final Map<UUID, SellPreviewToken> TOKENS = new ConcurrentHashMap<>();

    private SellPreviewManager() {
        // static-only
    }

    public static void openPreview(ServerPlayerEntity player) {
        if (player == null) return;

        Optional<CofferFabricRefusal> refusal =
                CofferFabricRuntime.getIfPresent()
                        .flatMap(CofferFabricRuntime::refuseIfNotReady);
        if (refusal.isPresent()) {
            player.sendMessage(Text.literal("[Coffer] " + refusal.get().message()), false);
            return;
        }

        AdapterServices.Snapshot snapshot = AdapterServices.get().orElse(null);
        if (snapshot == null) {
            player.sendMessage(Text.literal("[Coffer] Adapter services unavailable."), false);
            return;
        }

        DeclaredExchangeRequest placeholder =
                new DeclaredExchangeRequest(
                        ExchangeIntent.SELL,
                        InvocationContext.player(player.getUuid()),
                        DeclaredIdentity.of(player.getUuid()),
                        java.util.List.of()
                );
        ExchangeEvaluationResult eval = ExchangeEvaluationResult.pass(new ValuationSnapshot(java.util.List.of()));

        SellPreviewToken token = new SellPreviewToken(
                UUID.randomUUID(),
                player.getUuid(),
                placeholder,
                eval,
                null,
                Instant.now()
        );
        TOKENS.put(player.getUuid(), token);

        MoneyFormatter formatter = snapshot.moneyFormatter();
        int rows = SellPreviewScreenHandler.computeRows(token);
        player.openHandledScreen(
                new net.minecraft.screen.SimpleNamedScreenHandlerFactory(
                        (syncId, inv, p) -> new SellPreviewScreenHandler(syncId, rows, token, formatter, snapshot.currencyConfig(), inv),
                        Text.literal("Coffer Sell Preview")
                )
        );
    }

    public static void cancel(ServerPlayerEntity player) {
        if (player == null) return;
        TOKENS.remove(player.getUuid());
        if (player.currentScreenHandler instanceof SellPreviewScreenHandler handler) {
            handler.returnContentsTo(player);
        }
        player.closeHandledScreen();
        player.sendMessage(Text.literal("[Coffer] Sell cancelled. No changes were made."), false);
    }

    public static void confirm(ServerPlayerEntity player) {
        if (player == null) return;
        SellPreviewToken token = TOKENS.remove(player.getUuid());
        if (token == null) {
            player.sendMessage(Text.literal("[Coffer] Preview expired. Please preview again."), false);
            player.closeHandledScreen();
            return;
        }

        AdapterServices.Snapshot snapshot = AdapterServices.get().orElse(null);
        if (snapshot == null) {
            player.sendMessage(Text.literal("[Coffer] Adapter services unavailable."), false);
            player.closeHandledScreen();
            return;
        }

        Optional<CofferFabricRefusal> refusal =
                CofferFabricRuntime.getIfPresent()
                        .flatMap(CofferFabricRuntime::refuseIfNotReady);
        if (refusal.isPresent()) {
            player.sendMessage(Text.literal("[Coffer] " + refusal.get().message()), false);
            player.closeHandledScreen();
            return;
        }

        if (!(player.currentScreenHandler instanceof SellPreviewScreenHandler handler)) {
            player.sendMessage(Text.literal("[Coffer] Sell window not active; please open again."), false);
            player.closeHandledScreen();
            return;
        }

        DeclaredExchangeRequest declared = buildRequestFromHandler(handler, player.getUuid());
        if (declared.items().isEmpty()) {
            handler.returnContentsTo(player);
            player.sendMessage(Text.literal("[Coffer] Place items in the sell window first."), false);
            player.closeHandledScreen();
            return;
        }

        FabricCoreExecutor coreExecutor = snapshot.coreExecutor();
        ExchangeEvaluationResult eval = coreExecutor.execute(declared);
        if (!eval.allowed()) {
            String reason = eval.denialReason() == null ? "Denied." : "Denied: " + eval.denialReason();
            handler.updateFromEvaluation(eval, snapshot.blacklistConfig(), fallbackValuation(snapshot, declared));
            handler.returnContentsTo(player);
            player.sendMessage(Text.literal("[Coffer] " + reason + " No changes were made."), false);
            player.closeHandledScreen();
            return;
        }

        BalanceCreditPlanner creditPlanner = snapshot.creditPlanner();
        BalanceCreditPlanningResult planningResult =
                creditPlanner.plan(player.getUuid(), eval);

        if (!planningResult.planned()) {
            String message = planningResult.refusal()
                    .map(r -> r.message())
                    .orElse("Unable to plan credit. No changes were made.");
            handler.returnContentsTo(player);
            player.sendMessage(Text.literal("[Coffer] " + message), false);
            player.closeHandledScreen();
            return;
        }

        BalanceCreditPlan creditPlan = planningResult.plan().orElseThrow();

        List<MutationContext.PlannedRemoval> removals = buildAcceptedRemovals(eval, handler);
        ContainerRemovalStep removalStep = new ContainerRemovalStep(handler.itemsInventory(), handler.controlRowStart(), removals);
        handler.updateFromEvaluation(eval, snapshot.blacklistConfig(), fallbackValuation(snapshot, declared));

        ExecutionResult removeResult = removalStep.apply();
        if (!removeResult.success()) {
            handler.returnContentsTo(player);
            player.sendMessage(Text.literal("[Coffer] Unable to remove items; no changes were made."), false);
            player.closeHandledScreen();
            return;
        }

        java.util.List<BalanceCreditStep> creditSteps = new java.util.ArrayList<>();
        for (var entry : creditPlan.creditsByCurrency().entrySet()) {
            BalanceCreditStep step = new BalanceCreditStep(snapshot.balanceStore(), entry.getKey(), entry.getValue(), snapshot.auditSink());
            ExecutionResult creditResult = step.apply(player.getUuid());
            if (!creditResult.success()) {
                // rollback applied credits and items
                for (int i = creditSteps.size() - 1; i >= 0; i--) {
                    creditSteps.get(i).rollback(player.getUuid());
                }
                removalStep.rollback();
                handler.returnContentsTo(player);
                String reason = creditResult.reason();
                String friendly = reason != null && reason.contains("STORAGE") ? "Storage unavailable; no changes were made." : "Unable to credit; no changes were made.";
                player.sendMessage(Text.literal("[Coffer] " + friendly), false);
                player.closeHandledScreen();
                return;
            }
            creditSteps.add(step);
        }

        MoneyFormatter formatter = snapshot.moneyFormatter();
        player.sendMessage(Text.literal("[Coffer] Sold: " + describeCredits(snapshot, creditPlan) + "."), false);

        if (handler.hasAnyItems()) {
            handler.recalc();
        } else {
            player.closeHandledScreen();
        }
    }

    public static void clearForPlayer(UUID playerId) {
        if (playerId == null) return;
        TOKENS.remove(playerId);
    }

    private static DeclaredExchangeRequest buildRequestFromHandler(SellPreviewScreenHandler handler, UUID playerId) {
        java.util.List<DeclaredItem> items = new java.util.ArrayList<>();
        for (int i = 0; i < handler.controlRowStart(); i++) {
            ItemStack stack = handler.itemsInventory().getStack(i);
            if (stack == null || stack.isEmpty()) continue;
            String itemId = Registries.ITEM.getId(stack.getItem()).toString();
            long count = stack.getCount();
            items.add(DeclaredItem.withoutMetadata(itemId, count, MetadataRelevance.IGNORED_BY_DECLARATION));
        }

        return new DeclaredExchangeRequest(
                ExchangeIntent.SELL,
                InvocationContext.player(playerId),
                DeclaredIdentity.of(playerId),
                items
        );
    }

    private static List<MutationContext.PlannedRemoval> buildAcceptedRemovals(ExchangeEvaluationResult eval, SellPreviewScreenHandler handler) {
        List<MutationContext.PlannedRemoval> removals = new ArrayList<>();
        Object snapObj = eval.valuationSnapshot();
        if (!(snapObj instanceof ValuationSnapshot snapshot)) {
            return removals;
        }
        snapshot.itemResults().forEach(r -> {
            if (r.accepted() && r.item() instanceof DeclaredItem di) {
                removals.add(new MutationContext.PlannedRemoval(di.itemId(), (int) di.count()));
            }
        });

        // Clamp to what actually exists in the container to avoid over-removal.
        return clampToInventory(removals, handler);
    }

    private static List<MutationContext.PlannedRemoval> clampToInventory(List<MutationContext.PlannedRemoval> removals, SellPreviewScreenHandler handler) {
        List<MutationContext.PlannedRemoval> clamped = new ArrayList<>();
        for (MutationContext.PlannedRemoval r : removals) {
            int available = countInInventory(handler, r.itemId());
            int qty = Math.min(available, r.quantity());
            if (qty > 0) {
                clamped.add(new MutationContext.PlannedRemoval(r.itemId(), qty));
            }
        }
        return clamped;
    }

    private static int countInInventory(SellPreviewScreenHandler handler, String itemId) {
        int count = 0;
        for (int i = 0; i < handler.controlRowStart(); i++) {
            ItemStack stack = handler.itemsInventory().getStack(i);
            if (stack == null || stack.isEmpty()) continue;
            String id = Registries.ITEM.getId(stack.getItem()).toString();
            if (itemId.equals(id)) {
                count += stack.getCount();
            }
        }
        return count;
    }

    private static ValuationSnapshot fallbackValuation(AdapterServices.Snapshot snapshot, DeclaredExchangeRequest req) {
        try {
            FabricValuationService fv = new FabricValuationService(snapshot.valuationConfig(), snapshot.blacklistConfig(), snapshot.currencyConfig().defaultCurrency().id());
            return fv.valuate(new ExchangeRequest(req.invoker(), req.intent(), req));
        } catch (Exception e) {
            return null;
        }
    }

    private static String describeCredits(AdapterServices.Snapshot snapshot, BalanceCreditPlan plan) {
        var currencyConfig = snapshot.currencyConfig();
        var parts = new ArrayList<String>();
        for (var entry : plan.creditsByCurrency().entrySet()) {
            var defOpt = currencyConfig.find(entry.getKey());
            if (defOpt.isPresent()) {
                MoneyFormatter fmt = new MoneyFormatter(defOpt.get());
                parts.add(fmt.format(entry.getValue()));
            } else {
                parts.add(entry.getValue() + " " + entry.getKey());
            }
        }
        return String.join(", ", parts);
    }
}

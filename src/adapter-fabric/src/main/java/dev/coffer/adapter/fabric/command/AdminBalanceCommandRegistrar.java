package dev.coffer.adapter.fabric.command;

import com.mojang.brigadier.arguments.LongArgumentType;
import dev.coffer.adapter.fabric.AdapterServices;
import dev.coffer.adapter.fabric.config.CurrencyConfig;
import dev.coffer.adapter.fabric.execution.BalanceStore;
import dev.coffer.adapter.fabric.execution.BalanceStoreException;
import dev.coffer.adapter.fabric.execution.FabricAuditSink;
import dev.coffer.adapter.fabric.execution.MoneyFormatter;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.command.argument.EntityArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * ADMIN BALANCE COMMANDS
 *
 * Responsibility:
 * - Server-authoritative credit/debit operations (no valuation/exchange path).
 *
 * Invariants:
 * - Credits require amount > 0.
 * - Debits require amount > 0 and sufficient balance.
 * - All operations are auditable and non-persistent (in-memory store).
 */
public final class AdminBalanceCommandRegistrar {

    private AdminBalanceCommandRegistrar() {
        // static-only
    }

    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            dispatcher.register(
                        CommandManager.literal("coffer")
                                .then(CommandManager.literal("balance")
                                        .requires(src -> PermissionGate.hasPermission(src, "coffer.command.balance.self", 0))
                                        .executes(ctx -> showBalanceSelf(ctx.getSource()))
                                        .then(CommandManager.argument("player", EntityArgumentType.player())
                                                .requires(src -> PermissionGate.hasPermission(src, "coffer.command.balance.others", 2))
                                                .executes(ctx -> {
                                                    ServerPlayerEntity target = EntityArgumentType.getPlayer(ctx, "player");
                                                    return showBalance(ctx.getSource(), target);
                                                })
                                                .then(CommandManager.argument("currency", StringArgumentType.word())
                                                        .suggests(AdminBalanceCommandRegistrar::suggestCurrencies)
                                                        .executes(ctx -> {
                                                            ServerPlayerEntity target = EntityArgumentType.getPlayer(ctx, "player");
                                                            String currencyId = StringArgumentType.getString(ctx, "currency");
                                                            return showBalance(ctx.getSource(), target, currencyId);
                                                        })
                                                )
                                        )
                                        .then(CommandManager.argument("currency", StringArgumentType.word())
                                                .suggests(AdminBalanceCommandRegistrar::suggestCurrencies)
                                                .executes(ctx -> {
                                                    String currencyId = StringArgumentType.getString(ctx, "currency");
                                                    return showBalanceSelf(ctx.getSource(), currencyId);
                                                })
                                        )
                                )
                                .then(CommandManager.literal("credit")
                                        .requires(src -> PermissionGate.hasPermission(src, "coffer.command.credit", 2))
                                        .then(CommandManager.argument("player", EntityArgumentType.player())
                                                .then(CommandManager.argument("amount", LongArgumentType.longArg(1))
                                                        .executes(ctx -> {
                                                            ServerPlayerEntity target = EntityArgumentType.getPlayer(ctx, "player");
                                                            long amount = LongArgumentType.getLong(ctx, "amount");
                                                        return credit(ctx.getSource(), target, amount);
                                                    })
                                                    .then(CommandManager.argument("currency", StringArgumentType.word())
                                                            .suggests(AdminBalanceCommandRegistrar::suggestCurrencies)
                                                            .executes(ctx -> {
                                                                ServerPlayerEntity target = EntityArgumentType.getPlayer(ctx, "player");
                                                                long amount = LongArgumentType.getLong(ctx, "amount");
                                                                String currencyId = StringArgumentType.getString(ctx, "currency");
                                                                return credit(ctx.getSource(), target, amount, currencyId);
                                                            })
                                                    )
                                            )
                                    )
                            )
                                .then(CommandManager.literal("debit")
                                        .requires(src -> PermissionGate.hasPermission(src, "coffer.command.debit", 2))
                                        .then(CommandManager.argument("player", EntityArgumentType.player())
                                                .then(CommandManager.argument("amount", LongArgumentType.longArg(1))
                                                        .executes(ctx -> {
                                                            ServerPlayerEntity target = EntityArgumentType.getPlayer(ctx, "player");
                                                            long amount = LongArgumentType.getLong(ctx, "amount");
                                                        return debit(ctx.getSource(), target, amount);
                                                    })
                                                    .then(CommandManager.argument("currency", StringArgumentType.word())
                                                            .suggests(AdminBalanceCommandRegistrar::suggestCurrencies)
                                                            .executes(ctx -> {
                                                                ServerPlayerEntity target = EntityArgumentType.getPlayer(ctx, "player");
                                                                long amount = LongArgumentType.getLong(ctx, "amount");
                                                                String currencyId = StringArgumentType.getString(ctx, "currency");
                                                                return debit(ctx.getSource(), target, amount, currencyId);
                                                            })
                                                    )
                                            )
                                    )
                            )
            );

            // Aliases without the /coffer prefix.
            dispatcher.register(
                    CommandManager.literal("balance")
                            .requires(src -> PermissionGate.hasPermission(src, "coffer.command.balance.self", 0))
                            .executes(ctx -> showBalanceSelf(ctx.getSource()))
                            .then(CommandManager.argument("player", EntityArgumentType.player())
                                    .requires(src -> PermissionGate.hasPermission(src, "coffer.command.balance.others", 2))
                                    .executes(ctx -> {
                                        ServerPlayerEntity target = EntityArgumentType.getPlayer(ctx, "player");
                                        return showBalance(ctx.getSource(), target);
                                    })
                                    .then(CommandManager.argument("currency", StringArgumentType.word())
                                            .suggests(AdminBalanceCommandRegistrar::suggestCurrencies)
                                            .executes(ctx -> {
                                                ServerPlayerEntity target = EntityArgumentType.getPlayer(ctx, "player");
                                                String currencyId = StringArgumentType.getString(ctx, "currency");
                                                return showBalance(ctx.getSource(), target, currencyId);
                                            })
                                    )
                            )
                            .then(CommandManager.argument("currency", StringArgumentType.word())
                                    .suggests(AdminBalanceCommandRegistrar::suggestCurrencies)
                                    .executes(ctx -> {
                                        String currencyId = StringArgumentType.getString(ctx, "currency");
                                        return showBalanceSelf(ctx.getSource(), currencyId);
                                    })
                            )
            );
            dispatcher.register(
                    CommandManager.literal("credit")
                            .requires(src -> PermissionGate.hasPermission(src, "coffer.command.credit", 2))
                            .then(CommandManager.argument("player", EntityArgumentType.player())
                                    .then(CommandManager.argument("amount", LongArgumentType.longArg(1))
                                            .executes(ctx -> {
                                                ServerPlayerEntity target = EntityArgumentType.getPlayer(ctx, "player");
                                                long amount = LongArgumentType.getLong(ctx, "amount");
                                                return credit(ctx.getSource(), target, amount);
                                            })
                                            .then(CommandManager.argument("currency", StringArgumentType.word())
                                                    .suggests(AdminBalanceCommandRegistrar::suggestCurrencies)
                                                    .executes(ctx -> {
                                                        ServerPlayerEntity target = EntityArgumentType.getPlayer(ctx, "player");
                                                        long amount = LongArgumentType.getLong(ctx, "amount");
                                                        String currencyId = StringArgumentType.getString(ctx, "currency");
                                                        return credit(ctx.getSource(), target, amount, currencyId);
                                                    })
                                            )
                                    )
                            )
            );
            dispatcher.register(
                    CommandManager.literal("debit")
                            .requires(src -> PermissionGate.hasPermission(src, "coffer.command.debit", 2))
                            .then(CommandManager.argument("player", EntityArgumentType.player())
                                    .then(CommandManager.argument("amount", LongArgumentType.longArg(1))
                                            .executes(ctx -> {
                                                ServerPlayerEntity target = EntityArgumentType.getPlayer(ctx, "player");
                                                long amount = LongArgumentType.getLong(ctx, "amount");
                                                return debit(ctx.getSource(), target, amount);
                                            })
                                            .then(CommandManager.argument("currency", StringArgumentType.word())
                                                    .suggests(AdminBalanceCommandRegistrar::suggestCurrencies)
                                                    .executes(ctx -> {
                                                        ServerPlayerEntity target = EntityArgumentType.getPlayer(ctx, "player");
                                                        long amount = LongArgumentType.getLong(ctx, "amount");
                                                        String currencyId = StringArgumentType.getString(ctx, "currency");
                                                        return debit(ctx.getSource(), target, amount, currencyId);
                                                    })
                                            )
                                    )
                            )
            );
        });
    }

    private static int credit(ServerCommandSource source, ServerPlayerEntity target, long amount) {
        AdapterServices.Snapshot snapshot = AdapterServices.get().orElse(null);
        return credit(source, target, amount, snapshot != null ? snapshot.currencyConfig().defaultCurrency().id() : null);
    }

    private static int credit(ServerCommandSource source, ServerPlayerEntity target, long amount, String currencyId) {
        AdapterServices.Snapshot snapshot = AdapterServices.get().orElse(null);
        if (snapshot == null) {
            source.sendError(Text.literal("[Coffer] Adapter services unavailable."));
            return 0;
        }
        BalanceStore store = snapshot.balanceStore();
        FabricAuditSink auditSink = snapshot.auditSink();
        CurrencyResolution currency = resolveCurrency(snapshot, currencyId);
        if (!currency.valid()) {
            source.sendError(Text.literal("[Coffer] Unknown currency: " + currencyId + " (known: " + String.join(", ", snapshot.currencyConfig().currencyIds()) + ")"));
            return 0;
        }
        MoneyFormatter formatter = currency.formatter();
        UUID playerId = target.getUuid();
        try {
            store.applyDelta(playerId, currency.currencyId(), amount);
        } catch (BalanceStoreException e) {
            auditSink.emitAdmin("STORAGE_ERROR", e.getMessage());
            System.err.println("[Coffer][Storage] " + e.getMessage());
            source.sendFeedback(() -> Text.literal("[Coffer] Could not update balance (storage unavailable). No changes were made."), false);
            return 1;
        }
        auditSink.emitAdmin("ADMIN_CREDIT", playerId + " +" + amount + " " + currency.currencyId());
        source.sendFeedback(() -> Text.literal("[Coffer] Credited " + formatter.format(amount) + " to " + target.getName().getString()), false);
        return 1;
    }

    private static int showBalanceSelf(ServerCommandSource source) {
        ServerPlayerEntity player = source.getPlayer();
        if (player == null) {
            source.sendError(Text.literal("[Coffer] Players only."));
            return 0;
        }
        return showBalance(source, player);
    }

    private static int showBalanceSelf(ServerCommandSource source, String currencyId) {
        ServerPlayerEntity player = source.getPlayer();
        if (player == null) {
            source.sendError(Text.literal("[Coffer] Players only."));
            return 0;
        }
        return showBalance(source, player, currencyId);
    }

    private static int showBalance(ServerCommandSource source, ServerPlayerEntity target) {
        AdapterServices.Snapshot snapshot = AdapterServices.get().orElse(null);
        if (snapshot == null) {
            source.sendError(Text.literal("[Coffer] Adapter services unavailable."));
            return 0;
        }
        return showBalance(source, target, snapshot.currencyConfig().defaultCurrency().id());
    }

    private static int showBalance(ServerCommandSource source, ServerPlayerEntity target, String currencyId) {
        AdapterServices.Snapshot snapshot = AdapterServices.get().orElse(null);
        if (snapshot == null) {
            source.sendError(Text.literal("[Coffer] Adapter services unavailable."));
            return 0;
        }
        BalanceStore store = snapshot.balanceStore();
        CurrencyResolution currency = resolveCurrency(snapshot, currencyId);
        if (!currency.valid()) {
            source.sendError(Text.literal("[Coffer] Unknown currency: " + currencyId));
            return 0;
        }
        MoneyFormatter formatter = currency.formatter();
        long balance;
        try {
            balance = store.getBalance(target.getUuid(), currency.currencyId());
        } catch (BalanceStoreException e) {
            System.err.println("[Coffer][Storage] " + e.getMessage());
            source.sendFeedback(() -> Text.literal("[Coffer] Could not read balance (storage unavailable)."), false);
            return 1;
        }
        source.sendFeedback(
                () -> Text.literal("[Coffer] Balance for " + target.getName().getString() + ": " + formatter.format(balance) + " (" + currency.currencyId() + ")"),
                false
        );
        return 1;
    }

    private static int debit(ServerCommandSource source, ServerPlayerEntity target, long amount) {
        AdapterServices.Snapshot snapshot = AdapterServices.get().orElse(null);
        return debit(source, target, amount, snapshot != null ? snapshot.currencyConfig().defaultCurrency().id() : null);
    }

    private static int debit(ServerCommandSource source, ServerPlayerEntity target, long amount, String currencyId) {
        AdapterServices.Snapshot snapshot = AdapterServices.get().orElse(null);
        if (snapshot == null) {
            source.sendError(Text.literal("[Coffer] Adapter services unavailable."));
            return 0;
        }
        BalanceStore store = snapshot.balanceStore();
        FabricAuditSink auditSink = snapshot.auditSink();
        CurrencyResolution currency = resolveCurrency(snapshot, currencyId);
        if (!currency.valid()) {
            source.sendError(Text.literal("[Coffer] Unknown currency: " + currencyId));
            return 0;
        }
        MoneyFormatter formatter = currency.formatter();
        UUID playerId = target.getUuid();
        long current;
        try {
            current = store.getBalance(playerId, currency.currencyId());
        } catch (BalanceStoreException e) {
            auditSink.emitAdmin("STORAGE_ERROR", e.getMessage());
            System.err.println("[Coffer][Storage] " + e.getMessage());
            source.sendFeedback(() -> Text.literal("[Coffer] Could not read balance (storage unavailable)."), false);
            return 1;
        }
        if (current < amount) {
            source.sendFeedback(() -> Text.literal("[Coffer] Insufficient balance. No changes were made."), false);
            return 1;
        }
        try {
            store.applyDelta(playerId, currency.currencyId(), -amount);
        } catch (BalanceStoreException e) {
            auditSink.emitAdmin("STORAGE_ERROR", e.getMessage());
            System.err.println("[Coffer][Storage] " + e.getMessage());
            source.sendFeedback(() -> Text.literal("[Coffer] Could not update balance (storage unavailable). No changes were made."), false);
            return 1;
        }
        auditSink.emitAdmin("ADMIN_DEBIT", playerId + " -" + amount + " " + currency.currencyId());
        source.sendFeedback(() -> Text.literal("[Coffer] Debited " + formatter.format(amount) + " from " + target.getName().getString()), false);
        return 1;
    }

    private record CurrencyResolution(String currencyId, MoneyFormatter formatter) {
        boolean valid() {
            return currencyId != null && formatter != null;
        }
    }

    private static CurrencyResolution resolveCurrency(AdapterServices.Snapshot snapshot, String requestedCurrencyId) {
        if (snapshot == null) return new CurrencyResolution(null, null);
        String defaultId = snapshot.currencyConfig().defaultCurrency().id();
        String desiredId = (requestedCurrencyId == null || requestedCurrencyId.isBlank()) ? defaultId : requestedCurrencyId;

        CurrencyConfig.CurrencyDefinition def =
                snapshot.currencyConfig().find(desiredId).orElse(null);

        if (def == null) {
            return new CurrencyResolution(null, null);
        }

        MoneyFormatter formatter = desiredId.equals(defaultId)
                ? snapshot.moneyFormatter()
                : new MoneyFormatter(def);

        return new CurrencyResolution(desiredId, formatter);
    }

    private static CompletableFuture<Suggestions> suggestCurrencies(CommandContext<ServerCommandSource> ctx, SuggestionsBuilder builder) {
        AdapterServices.get().ifPresent(snapshot ->
                snapshot.currencyConfig().currencyIds().forEach(builder::suggest)
        );
        return builder.buildFuture();
    }
}

package dev.coffer.adapter.fabric.command;

import dev.coffer.adapter.fabric.AdapterServices;
import dev.coffer.adapter.fabric.CofferFabricRefusal;
import dev.coffer.adapter.fabric.CofferFabricRuntime;
import dev.coffer.adapter.fabric.config.ShopCatalog;
import dev.coffer.adapter.fabric.config.ShopDefinition;
import dev.coffer.adapter.fabric.config.ShopEntry;
import dev.coffer.adapter.fabric.execution.FabricAuditSink;
import dev.coffer.adapter.fabric.CofferFabricEntrypoint;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import java.nio.file.Path;

import java.util.List;
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
                            .requires(src -> PermissionGate.hasPermission(src, "coffer.command.ready", 0))
                            .executes(context -> handleReady(context.getSource()))
                            .then(CommandManager.literal("help")
                                    .executes(context -> handleHelp(context.getSource()))
                            )
                            .then(CommandManager.literal("audits")
                                    .requires(src -> PermissionGate.hasPermission(src, "coffer.command.audits", 2))
                                    .executes(context -> handleAudits(context.getSource()))
                            )
                            .then(CommandManager.literal("reload")
                                    .requires(src -> PermissionGate.hasPermission(src, "coffer.command.reload", 2))
                                    .executes(context -> handleReload(context.getSource()))
                            )
                            .then(CommandManager.literal("storage")
                                    .requires(src -> PermissionGate.hasPermission(src, "coffer.command.storage", 3))
                                    .then(CommandManager.literal("import-json")
                                            .then(CommandManager.argument("path", com.mojang.brigadier.arguments.StringArgumentType.string())
                                                    .executes(ctx -> handleImportJson(ctx.getSource(), com.mojang.brigadier.arguments.StringArgumentType.getString(ctx, "path")))
                                            )
                                    )
                            )
            );

            dispatcher.register(
                    CommandManager.literal("audits")
                            .requires(src -> PermissionGate.hasPermission(src, "coffer.command.audits", 2))
                            .executes(context -> handleAudits(context.getSource()))
            );
            dispatcher.register(
                    CommandManager.literal("ready")
                            .requires(src -> PermissionGate.hasPermission(src, "coffer.command.ready", 0))
                            .executes(context -> handleReady(context.getSource()))
            );
            dispatcher.register(
                    CommandManager.literal("cofferhelp")
                            .executes(context -> handleHelp(context.getSource()))
            );
            dispatcher.register(
                    CommandManager.literal("reload")
                            .requires(src -> PermissionGate.hasPermission(src, "coffer.command.reload", 2))
                            .executes(context -> handleReload(context.getSource()))
            );
        });
    }

    private static int handleReady(ServerCommandSource source) {
        Optional<CofferFabricRefusal> refusal =
                CofferFabricRuntime.getIfPresent()
                        .flatMap(CofferFabricRuntime::refuseIfNotReady);

        if (refusal.isPresent()) {
            source.sendError(Text.literal("[Coffer] " + refusal.get().message()));
            return 0;
        }

        source.sendFeedback(
                () -> Text.literal("[Coffer] Adapter is READY (diagnostic)."),
                false
        );
        return 1;
    }

    private static int handleHelp(ServerCommandSource source) {
        source.sendFeedback(() -> Text.literal("[Coffer] Commands:"), false);
        source.sendFeedback(() -> Text.literal(" - /ready : check adapter readiness"), false);
        source.sendFeedback(() -> Text.literal(" - /audits : show recent audits (admin)"), false);
        source.sendFeedback(() -> Text.literal(" - /sell : sell owned items for credit"), false);
        source.sendFeedback(() -> Text.literal(" - /shop open <shopId> : list entries for a shop"), false);
        source.sendFeedback(() -> Text.literal(" - /shop price <shopId> <item> [qty] : show shop-adjusted price"), false);
        source.sendFeedback(() -> Text.literal(" - /shop buy <shopId> <item> <qty> : admin shop purchase"), false);
        source.sendFeedback(() -> Text.literal(" - /balance [player] [currency] : view balance"), false);
        source.sendFeedback(() -> Text.literal(" - /credit|/debit <player> <amount> [currency] : admin adjust balance"), false);
        source.sendFeedback(() -> Text.literal(" - /coffer storage import-json <path> : import legacy balances into sqlite (admin)"), false);
        source.sendFeedback(() -> Text.literal(" - /reload : reload configs (admin)"), false);
        return 1;
    }

    private static int handleAudits(ServerCommandSource source) {
        Optional<CofferFabricRefusal> refusal =
                CofferFabricRuntime.getIfPresent()
                        .flatMap(CofferFabricRuntime::refuseIfNotReady);

        if (refusal.isPresent()) {
            source.sendError(Text.literal("[Coffer] " + refusal.get().message()));
            return 0;
        }

        AdapterServices.Snapshot snapshot = AdapterServices.get().orElse(null);
        if (snapshot == null) {
            source.sendError(Text.literal("[Coffer] Adapter services unavailable."));
            return 0;
        }

        FabricAuditSink auditSink = snapshot.auditSink();
        List<String> lines = auditSink.describeRecent(5);
        if (lines.isEmpty()) {
            source.sendFeedback(() -> Text.literal("[Coffer] No audits yet."), false);
            return 1;
        }

        for (String line : lines) {
            source.sendFeedback(() -> Text.literal("[Coffer][Audit] " + line), false);
        }
        return 1;
    }

    private static int handleReload(ServerCommandSource source) {
        Optional<CofferFabricRuntime> rt = CofferFabricRuntime.getIfPresent();
        if (rt.isEmpty()) {
            source.sendError(Text.literal("[Coffer] Runtime not initialized."));
            return 0;
        }

        source.sendFeedback(() -> Text.literal("[Coffer] Reloading configs..."), false);
        CofferFabricEntrypoint.ReloadResult result = CofferFabricEntrypoint.reinitialize(rt.get());
        if (result.succeeded()) {
            source.sendFeedback(() -> Text.literal("[Coffer] Reload complete."), false);
        } else {
            source.sendError(Text.literal("[Coffer] Reload failed; previous state kept: " + result.message()));
        }
        return 1;
    }

    private static int handleImportJson(ServerCommandSource source, String pathStr) {
        Optional<CofferFabricRefusal> refusal =
                CofferFabricRuntime.getIfPresent()
                        .flatMap(CofferFabricRuntime::refuseIfNotReady);

        if (refusal.isPresent()) {
            source.sendError(Text.literal("[Coffer] " + refusal.get().message()));
            return 0;
        }

        AdapterServices.Snapshot snapshot = AdapterServices.get().orElse(null);
        if (snapshot == null) {
            source.sendError(Text.literal("[Coffer] Adapter services unavailable."));
            return 0;
        }

        if (snapshot.storageConfig().type != dev.coffer.adapter.fabric.config.StorageConfig.Type.SQLITE) {
            source.sendError(Text.literal("[Coffer] Import available only when storage.type=sqlite."));
            return 0;
        }

        Path jsonPath = source.getServer().getRunDirectory().resolve(pathStr).normalize();
        if (!jsonPath.toFile().exists()) {
            source.sendError(Text.literal("[Coffer] JSON file not found: " + pathStr));
            return 0;
        }

        dev.coffer.adapter.fabric.execution.SqliteBalanceStore store;
        if (!(snapshot.balanceStore() instanceof dev.coffer.adapter.fabric.execution.SqliteBalanceStore sqliteStore)) {
            source.sendError(Text.literal("[Coffer] Storage not sqlite; cannot import."));
            return 0;
        }
        store = sqliteStore;

        dev.coffer.adapter.fabric.execution.FabricAuditSink auditSink = snapshot.auditSink();

        try {
            int imported = dev.coffer.adapter.fabric.execution.SqliteBalanceStore.adminImportFromJson(jsonPath, store, snapshot.currencyConfig().defaultCurrency().id(), auditSink);
            source.sendFeedback(() -> Text.literal("[Coffer] Import complete. Entries imported: " + imported + "."), false);
            return 1;
        } catch (Exception e) {
            source.sendError(Text.literal("[Coffer] Import failed; no changes applied: " + e.getMessage()));
            return 0;
        }
    }
}

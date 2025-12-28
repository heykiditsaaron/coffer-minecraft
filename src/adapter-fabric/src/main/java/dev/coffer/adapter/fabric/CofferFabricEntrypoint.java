package dev.coffer.adapter.fabric;

import dev.coffer.adapter.fabric.command.CofferCommandRegistrar;
import dev.coffer.adapter.fabric.command.SellCommandRegistrar;
import dev.coffer.adapter.fabric.command.ShopCommandRegistrar;
import dev.coffer.adapter.fabric.command.AdminBalanceCommandRegistrar;
import dev.coffer.adapter.fabric.config.ConfigBootstrap;
import dev.coffer.adapter.fabric.config.CurrencyConfig;
import dev.coffer.adapter.fabric.config.ItemBlacklistConfig;
import dev.coffer.adapter.fabric.config.StorageConfig;
import dev.coffer.adapter.fabric.config.ValuationConfig;
import dev.coffer.adapter.fabric.config.ValuationConfigLoader;
import dev.coffer.adapter.fabric.config.LoggingConfig;
import dev.coffer.adapter.fabric.execution.BalanceStore;
import dev.coffer.adapter.fabric.execution.BalanceStoreException;
import dev.coffer.adapter.fabric.execution.FabricAuditSink;
import dev.coffer.adapter.fabric.execution.FabricCoreExecutor;
import dev.coffer.adapter.fabric.execution.FabricPolicyLayer;
import dev.coffer.adapter.fabric.execution.FabricValuationService;
import dev.coffer.adapter.fabric.execution.JsonBalanceStore;
import dev.coffer.adapter.fabric.execution.MoneyFormatter;
import dev.coffer.adapter.fabric.execution.ShopPricingService;
import dev.coffer.adapter.fabric.execution.ShopPurchasePlanner;
import dev.coffer.adapter.fabric.execution.ShopPurchaseTransactionExecutor;
import dev.coffer.adapter.fabric.execution.CompositeValuationService;
import dev.coffer.adapter.fabric.execution.FabricShopPurchaseValuationService;
import dev.coffer.core.CoreEngine;
import net.fabricmc.api.DedicatedServerModInitializer;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

/**
 * FABRIC ADAPTER — SERVER ENTRYPOINT
 *
 * Responsibility:
 * - Wire Core with adapter-owned valuation and audit sink.
 * - Register command entry points.
 * - Drive the adapter runtime through READY/FAILED states.
 *
 * Not responsible for:
 * - Policy logic (none configured yet).
 * - Valuation config loading (uses empty deny-by-default config).
 * - Any economic meaning beyond wiring.
 *
 * Invariants:
 * - Zero-config boots; valuation denies by default.
 * - Runtime enters READY only after wiring succeeds.
 * - Adapter failure is surfaced explicitly.
 */
public final class CofferFabricEntrypoint implements DedicatedServerModInitializer {

    @Override
    public void onInitializeServer() {
        CofferFabricRuntime runtime = CofferFabricRuntime.createOrGet();
        runtime.markInitializing();

        try {
            ConfigBootstrap.ensureDefaults();
            AdapterServices.Snapshot snapshot = buildSnapshot();
            AdapterServices.install(snapshot);

            CofferCommandRegistrar.register();
            ShopCommandRegistrar.register();
            SellCommandRegistrar.register();
            AdminBalanceCommandRegistrar.register();

            runtime.markReady();
            logReadySnapshot("READY", snapshot);
        } catch (Throwable t) {
            String message = "Coffer failed to initialize economic execution.";
            if (t instanceof BalanceStoreException) {
                message = "Coffer storage unavailable: " + t.getMessage();
                System.err.println("[Coffer][Storage] " + t.getMessage());
            } else if (t instanceof IOException) {
                message = "Coffer config initialization failed: " + t.getMessage();
                System.err.println("[Coffer][Config] " + t.getMessage());
            }
            runtime.markFailed(
                    CofferFabricRefusal.of(
                            "ADAPTER_BOOT_FAILURE",
                            message
                    )
            );
        }
    }

    public static ReloadResult reinitialize(CofferFabricRuntime runtime) {
        runtime.beginReload();
        try {
            AdapterServices.Snapshot snapshot = new CofferFabricEntrypoint().buildSnapshot();
            AdapterServices.install(snapshot);
            runtime.markReady();
            logReadySnapshot("RELOAD", snapshot);
            return ReloadResult.success();
        } catch (Throwable t) {
            runtime.markReady();
            System.err.println("[Coffer][Reload] Reload failed; keeping previous state: " + t.getMessage());
            return ReloadResult.failure(t.getMessage());
        } finally {
            runtime.endReload();
        }
    }

    private AdapterServices.Snapshot buildSnapshot() throws BalanceStoreException, IOException {
        StorageConfig storageConfig = StorageConfig.load();
        var currencyConfig = CurrencyConfig.load();
        var moneyFormatter = new MoneyFormatter(currencyConfig.defaultCurrency());
        Path configDir = net.fabricmc.loader.api.FabricLoader.getInstance().getConfigDir().resolve("coffer");
        Path path = configDir.resolve(storageConfig.path);

        LoggingConfig loggingConfig = LoggingConfig.load();
        var auditSink = new FabricAuditSink(loggingConfig.maxRecords(), loggingConfig.resolveFilePath(), loggingConfig.consoleEnabled());
        BalanceStore balanceStore = createBalanceStore(storageConfig, path, currencyConfig.defaultCurrency().id(), configDir);
        ValuationConfig valuationConfig = ValuationConfigLoader.load(currencyConfig.defaultCurrency().id());
        ItemBlacklistConfig blacklistConfig = ItemBlacklistConfig.load();
        var shopCatalog = dev.coffer.adapter.fabric.config.ShopConfigLoader.load();
        ShopPricingService pricingService = new ShopPricingService(shopCatalog, valuationConfig, currencyConfig.defaultCurrency().id());

        var sellValuationService = new FabricValuationService(valuationConfig, blacklistConfig, currencyConfig.defaultCurrency().id());
        var shopValuationService = new FabricShopPurchaseValuationService(pricingService, currencyConfig.defaultCurrency().id());
        var valuationService = new CompositeValuationService(sellValuationService, shopValuationService);

        CoreEngine coreEngine = new CoreEngine(
                /* policyLayers */ List.of(new FabricPolicyLayer(blacklistConfig)),
                valuationService,
                auditSink
        );

        FabricCoreExecutor executor = new FabricCoreExecutor(coreEngine);
        var mutationExecutor = new dev.coffer.adapter.fabric.execution.FabricMutationTransactionExecutor(balanceStore, auditSink);
        var creditPlanner = new dev.coffer.adapter.fabric.execution.BalanceCreditPlanner();
        ShopPurchasePlanner shopPurchasePlanner = new ShopPurchasePlanner();
        ShopPurchaseTransactionExecutor purchaseTxExecutor = new ShopPurchaseTransactionExecutor(balanceStore, auditSink);

        return new AdapterServices.Snapshot(
                auditSink,
                balanceStore,
                executor,
                mutationExecutor,
                creditPlanner,
                shopCatalog,
                pricingService,
                shopPurchasePlanner,
                purchaseTxExecutor,
                valuationConfig,
                blacklistConfig,
                storageConfig,
                currencyConfig,
                moneyFormatter,
                loggingConfig
        );
    }

    private BalanceStore createBalanceStore(StorageConfig storageConfig, Path resolvedPath, String defaultCurrencyId, Path configDir) throws BalanceStoreException {
        return switch (storageConfig.type) {
            case JSON -> new JsonBalanceStore(resolvedPath, defaultCurrencyId);
            case SQLITE -> new dev.coffer.adapter.fabric.execution.SqliteBalanceStore(resolvedPath, defaultCurrencyId);
        };
    }

    private static void logReadySnapshot(String phase, AdapterServices.Snapshot snapshot) {
        System.out.println("[Coffer][" + phase + "] Honest economy adapter ready.");
        System.out.println("[Coffer][" + phase + "] Currency: " + snapshot.moneyFormatter().describe());
        System.out.println("[Coffer][" + phase + "] Valuation entries=" + snapshot.valuationConfig().entryCount()
                + ", blacklist items=" + snapshot.blacklistConfig().denyItemCount()
                + ", blacklist tags=" + snapshot.blacklistConfig().denyTagCount());
        System.out.println("[Coffer][" + phase + "] Shops loaded=" + snapshot.shopCatalog().shops().size());
        System.out.println("[Coffer][" + phase + "] Storage=" + snapshot.storageConfig().type + " path=" + snapshot.storageConfig().path);
    }

    public record ReloadResult(boolean succeeded, String message) {
        public static ReloadResult success() {
            return new ReloadResult(true, "Reload complete.");
        }

        public static ReloadResult failure(String message) {
            return new ReloadResult(false, message == null ? "Unknown failure" : message);
        }
    }
}

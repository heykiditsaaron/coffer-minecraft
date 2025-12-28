package dev.coffer.adapter.fabric;

import dev.coffer.adapter.fabric.config.CurrencyConfig;
import dev.coffer.adapter.fabric.config.ShopCatalog;
import dev.coffer.adapter.fabric.config.ValuationConfig;
import dev.coffer.adapter.fabric.execution.BalanceCreditPlanner;
import dev.coffer.adapter.fabric.execution.BalanceStore;
import dev.coffer.adapter.fabric.execution.FabricAuditSink;
import dev.coffer.adapter.fabric.execution.FabricCoreExecutor;
import dev.coffer.adapter.fabric.execution.FabricMutationTransactionExecutor;
import dev.coffer.adapter.fabric.execution.MoneyFormatter;
import dev.coffer.adapter.fabric.execution.ShopPricingService;
import dev.coffer.adapter.fabric.execution.ShopPurchasePlanner;
import dev.coffer.adapter.fabric.execution.ShopPurchaseTransactionExecutor;
import dev.coffer.adapter.fabric.config.LoggingConfig;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Adapter-wide service snapshot.
 *
 * Commands pull from this snapshot at execution time so reload can safely
 * replace services without re-registering commands.
 */
public final class AdapterServices {

    private static final AtomicReference<Snapshot> SNAPSHOT = new AtomicReference<>();

    private AdapterServices() {
        // static-only
    }

    public static void install(Snapshot snapshot) {
        SNAPSHOT.set(Objects.requireNonNull(snapshot, "snapshot"));
    }

    public static Optional<Snapshot> get() {
        return Optional.ofNullable(SNAPSHOT.get());
    }

    public static Snapshot require() {
        return get().orElseThrow(() -> new IllegalStateException("Coffer services not initialized"));
    }

    public record Snapshot(
            FabricAuditSink auditSink,
            BalanceStore balanceStore,
            FabricCoreExecutor coreExecutor,
            FabricMutationTransactionExecutor mutationExecutor,
            BalanceCreditPlanner creditPlanner,
            ShopCatalog shopCatalog,
            ShopPricingService shopPricingService,
            ShopPurchasePlanner shopPurchasePlanner,
            ShopPurchaseTransactionExecutor shopPurchaseTransactionExecutor,
            ValuationConfig valuationConfig,
            dev.coffer.adapter.fabric.config.ItemBlacklistConfig blacklistConfig,
            dev.coffer.adapter.fabric.config.StorageConfig storageConfig,
            CurrencyConfig currencyConfig,
            MoneyFormatter moneyFormatter,
            LoggingConfig loggingConfig
    ) {
        public Snapshot {
            Objects.requireNonNull(auditSink, "auditSink");
            Objects.requireNonNull(balanceStore, "balanceStore");
            Objects.requireNonNull(coreExecutor, "coreExecutor");
            Objects.requireNonNull(mutationExecutor, "mutationExecutor");
            Objects.requireNonNull(creditPlanner, "creditPlanner");
            Objects.requireNonNull(shopCatalog, "shopCatalog");
            Objects.requireNonNull(shopPricingService, "shopPricingService");
            Objects.requireNonNull(shopPurchasePlanner, "shopPurchasePlanner");
            Objects.requireNonNull(shopPurchaseTransactionExecutor, "shopPurchaseTransactionExecutor");
            Objects.requireNonNull(valuationConfig, "valuationConfig");
            Objects.requireNonNull(blacklistConfig, "blacklistConfig");
            Objects.requireNonNull(storageConfig, "storageConfig");
            Objects.requireNonNull(currencyConfig, "currencyConfig");
            Objects.requireNonNull(moneyFormatter, "moneyFormatter");
            Objects.requireNonNull(loggingConfig, "loggingConfig");
        }
    }
}

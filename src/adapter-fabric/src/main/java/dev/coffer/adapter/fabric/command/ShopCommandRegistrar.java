package dev.coffer.adapter.fabric.command;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import dev.coffer.adapter.fabric.AdapterServices;
import dev.coffer.adapter.fabric.CofferFabricRefusal;
import dev.coffer.adapter.fabric.CofferFabricRuntime;
import dev.coffer.adapter.fabric.config.ShopCatalog;
import dev.coffer.adapter.fabric.config.ShopDefinition;
import dev.coffer.adapter.fabric.config.ShopEntry;
import dev.coffer.adapter.fabric.execution.MoneyFormatter;
import dev.coffer.adapter.fabric.execution.ShopPricingService;
import dev.coffer.adapter.fabric.execution.ShopPurchasePlanner;
import dev.coffer.adapter.fabric.execution.ShopPurchasePlanningResult;
import dev.coffer.adapter.fabric.execution.ShopPurchasePlan;
import dev.coffer.adapter.fabric.execution.ShopPurchaseTransactionExecutor;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;
import dev.coffer.adapter.fabric.execution.ui.AdminShopScreenHandler;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.command.argument.IdentifierArgumentType;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.ArrayList;
import java.util.List;

/**
 * FABRIC ADAPTER — SHOP COMMANDS (DIAGNOSTIC + PRICING + PURCHASE)
 *
 * Responsibility:
 * - Diagnostic `/shop open`
 * - `/shop price <shop> <item> [quantity]` to show modified price using shop modifiers
 * - `/shops` list loaded shops and entries
 * - `/shop buy <shop> <item> <quantity>` to perform admin shop purchase (mutation-last)
 *
 * Invariants:
 * - Refuses when adapter not READY.
 * - Never creates value; denies when base missing or adjusted price <= 0.
 * - Mutation occurs only after price calculation and checks.
 */
public final class ShopCommandRegistrar {

    private ShopCommandRegistrar() {
        // static-only
    }

    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            dispatcher.register(
                    CommandManager.literal("shop")
                            .then(CommandManager.literal("open")
                                    .requires(src -> PermissionGate.hasPermission(src, "coffer.command.shop.open", 0))
                                    .then(CommandManager.argument("shop", StringArgumentType.word())
                                            .suggests(ShopCommandRegistrar::suggestShops)
                                            .executes(context -> handleShopOpen(context.getSource(), StringArgumentType.getString(context, "shop")))
                                    )
                            )
                            .then(CommandManager.literal("price")
                                    .requires(src -> PermissionGate.hasPermission(src, "coffer.command.shop.price", 0))
                                    .then(CommandManager.argument("shop", StringArgumentType.string())
                                            .suggests(ShopCommandRegistrar::suggestShops)
                                            .then(CommandManager.argument("item", IdentifierArgumentType.identifier())
                                                    .executes(ctx -> {
                                                        String shop = StringArgumentType.getString(ctx, "shop");
                                                        Identifier itemId = IdentifierArgumentType.getIdentifier(ctx, "item");
                                                        return handleShopPrice(ctx.getSource(), shop, itemId, 1);
                                                    })
                                                    .then(CommandManager.argument("quantity", IntegerArgumentType.integer(1))
                                                            .executes(ctx -> {
                                                                String shop = StringArgumentType.getString(ctx, "shop");
                                                                Identifier itemId = IdentifierArgumentType.getIdentifier(ctx, "item");
                                                                int qty = IntegerArgumentType.getInteger(ctx, "quantity");
                                                                return handleShopPrice(ctx.getSource(), shop, itemId, qty);
                                                            })
                                                    )
                                            )
                                    )
                            )
                            .then(CommandManager.literal("buy")
                                    .requires(src -> PermissionGate.hasPermission(src, "coffer.command.shop.buy", 0))
                                    .then(CommandManager.argument("shop", StringArgumentType.string())
                                            .suggests(ShopCommandRegistrar::suggestShops)
                                            .then(CommandManager.argument("item", IdentifierArgumentType.identifier())
                                                    .then(CommandManager.argument("quantity", IntegerArgumentType.integer(1))
                                                            .executes(ctx -> {
                                                                String shop = StringArgumentType.getString(ctx, "shop");
                                                                Identifier itemId = IdentifierArgumentType.getIdentifier(ctx, "item");
                                                                int qty = IntegerArgumentType.getInteger(ctx, "quantity");
                                                                return handleShopBuy(ctx.getSource(), shop, itemId, qty);
                                                            })
                                                    )
                                            )
                                    )
                            )
            );
        });
    }

    private static int handleShopOpen(ServerCommandSource source, String shopName) {
        Optional<CofferFabricRefusal> refusal =
                CofferFabricRuntime.getIfPresent()
                        .flatMap(CofferFabricRuntime::refuseIfNotReady);

        if (refusal.isPresent()) {
            source.sendError(
                    Text.literal("[Coffer] " + refusal.get().message())
            );
            return 0;
        }

        if (source.getPlayer() == null) {
            source.sendError(Text.literal("[Coffer] Players only."));
            return 0;
        }

        AdapterServices.Snapshot snapshot = AdapterServices.get().orElse(null);
        if (snapshot == null) {
            source.sendError(Text.literal("[Coffer] Adapter services unavailable."));
            return 0;
        }

        ShopCatalog catalog = snapshot.shopCatalog();
        Optional<ShopDefinition> def = catalog.shops().stream()
                .filter(s -> s.id().equalsIgnoreCase(shopName))
                .findFirst();

        if (def.isEmpty()) {
            String known = catalog.shops().isEmpty() ? "none configured" :
                    String.join(", ", catalog.shops().stream().map(ShopDefinition::id).toList());
            source.sendError(Text.literal("[Coffer] Shop not found: " + shopName + " (known: " + known + ")"));
            return 0;
        }

        ShopDefinition shop = def.get();
        openShopView(source, snapshot, shop);
        return 1;
    }

    private static int handleShopPrice(ServerCommandSource source, String shopName, Identifier itemId, int quantity) {
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
        ShopPricingService pricingService = snapshot.shopPricingService();
        MoneyFormatter formatter = snapshot.moneyFormatter();
        ShopCatalog catalog = snapshot.shopCatalog();
        Optional<ShopDefinition> def = catalog.shops().stream()
                .filter(s -> s.id().equalsIgnoreCase(shopName))
                .findFirst();
        if (def.isEmpty()) {
            String known = catalog.shops().isEmpty() ? "none" :
                    String.join(", ", catalog.shops().stream().map(ShopDefinition::id).toList());
            source.sendError(Text.literal("[Coffer] Shop not found: " + shopName + " (known: " + known + ")"));
            return 0;
        }

        ShopPricingService.PriceResult res = pricingService.price(shopName, itemId.toString(), quantity);
        if (!res.success()) {
            source.sendFeedback(() -> Text.literal("[Coffer] " + res.message()), false);
            return 1;
        }

        source.sendFeedback(
                () -> Text.literal("[Coffer] " + quantity + " x " + itemId + " in shop '" + shopName + "' costs " + formatter.format(res.totalValue())
                        + " (" + formatter.format(res.perItem()) + " each)"),
                false
        );
        return 1;
    }

    private static int handleShopBuy(ServerCommandSource source, String shopName, Identifier itemId, int quantity) {
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
        ShopPurchasePlanner planner = snapshot.shopPurchasePlanner();
        ShopPurchaseTransactionExecutor executor = snapshot.shopPurchaseTransactionExecutor();
        MoneyFormatter formatter = snapshot.moneyFormatter();
        String currencyId = snapshot.currencyConfig().defaultCurrency().id();
        ShopCatalog catalog = snapshot.shopCatalog();
        Optional<ShopDefinition> def = catalog.shops().stream()
                .filter(s -> s.id().equalsIgnoreCase(shopName))
                .findFirst();
        if (def.isEmpty()) {
            String known = catalog.shops().isEmpty() ? "none" :
                    String.join(", ", catalog.shops().stream().map(ShopDefinition::id).toList());
            source.sendError(Text.literal("[Coffer] Shop not found: " + shopName + " (known: " + known + ")"));
            return 0;
        }

        if (source.getPlayer() == null) {
            source.sendError(Text.literal("[Coffer] Players only."));
            return 0;
        }

        var player = source.getPlayer();

        // Build declared request
        var declared = dev.coffer.adapter.fabric.boundary.DeclaredShopPurchase.of(
                shopName,
                itemId.toString(),
                quantity,
                player.getUuid()
        );

        // Core evaluation
        var evalResult = snapshot.coreExecutor().execute(declared);
        if (!evalResult.allowed()) {
            String reason = evalResult.denialReason() == null ? "Denied." : "Denied: " + evalResult.denialReason();
            source.sendFeedback(() -> Text.literal("[Coffer] " + reason + " No changes were made."), false);
            return 1;
        }

        ShopPurchasePlanningResult planning = planner.plan(declared, evalResult, currencyId);
        if (!planning.planned()) {
            String msg = planning.refusal().orElse("Unable to plan purchase. No changes were made.");
            source.sendFeedback(() -> Text.literal("[Coffer] " + msg), false);
            return 1;
        }

        ShopPurchasePlan plan = planning.plan().orElseThrow();
        var execResult = executor.execute(player, plan);
        if (!execResult.success()) {
            source.sendFeedback(() -> Text.literal("[Coffer] No changes were made (" + execResult.reason() + ")."), false);
            return 1;
        }

        source.sendFeedback(
                () -> Text.literal("[Coffer] Purchased for " + formatter.format(plan.cost()) + "."),
                false
        );
        return 1;
    }

    private static CompletableFuture<Suggestions> suggestShops(CommandContext<ServerCommandSource> ctx, SuggestionsBuilder builder) {
        AdapterServices.get().ifPresent(snapshot ->
                snapshot.shopCatalog().shops().forEach(s -> {
                    builder.suggest(s.id());
                })
        );
        return builder.buildFuture();
    }

    private static void openShopView(ServerCommandSource source, AdapterServices.Snapshot snapshot, ShopDefinition shop) {
        var pricingService = snapshot.shopPricingService();
        var valuationConfig = snapshot.valuationConfig();
        var moneyFormatter = snapshot.moneyFormatter();
        var currencyId = snapshot.currencyConfig().defaultCurrency().id();

        var entries = shop.entries();
        int rows = Math.max(1, Math.min(6, (int) Math.ceil(entries.size() / 9.0)));
        SimpleInventory inv = new SimpleInventory(rows * 9);

        int idx = 0;
        if (entries.isEmpty()) {
            ItemStack stack = new ItemStack(Items.BARRIER);
            stack.set(DataComponentTypes.CUSTOM_NAME, Text.literal("No entries configured"));
            inv.setStack(idx, stack);
        } else {
            for (ShopEntry entry : entries) {
                if (idx >= inv.size()) break;
                ItemStack stack = buildEntryStack(shop, entry, pricingService, valuationConfig, moneyFormatter, currencyId);
                inv.setStack(idx, stack);
                idx++;
            }
        }

        source.getPlayer().openHandledScreen(
                new net.minecraft.screen.SimpleNamedScreenHandlerFactory(
                        (syncId, playerInventory, player) -> new AdminShopScreenHandler(syncId, rows, inv, shop.id(), entries),
                        Text.literal("Coffer Shop: " + shop.id())
                )
        );
    }

    private static ItemStack buildEntryStack(ShopDefinition shop,
                                             ShopEntry entry,
                                             ShopPricingService pricingService,
                                             dev.coffer.adapter.fabric.config.ValuationConfig valuationConfig,
                                             MoneyFormatter formatter,
                                             String currencyId) {
        ItemStack stack;
        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal("kind: " + entry.kind()));

        if (entry.kind() == ShopEntry.Kind.ITEM) {
            Item item = Items.AIR;
            try {
                item = net.minecraft.registry.Registries.ITEM.get(Identifier.of(entry.target()));
            } catch (Exception ignored) {
            }
            if (item == Items.AIR) {
                stack = new ItemStack(Items.BARRIER);
                stack.set(DataComponentTypes.CUSTOM_NAME, Text.literal(entry.target() + " (unknown item)"));
                lore.add(Text.literal("Denied: unknown item id."));
            } else {
                stack = new ItemStack(item);
                stack.set(DataComponentTypes.CUSTOM_NAME, Text.literal(entry.target()));
                ShopPricingService.PriceResult res = pricingService.price(shop.id(), entry.target(), 1);
                if (res.success()) {
                    lore.add(Text.literal("Price: " + formatter.format(res.perItem())));
                } else {
                    lore.add(Text.literal("Denied: " + res.message()));
                }
            }
        } else {
            stack = new ItemStack(Items.WRITABLE_BOOK);
            stack.set(DataComponentTypes.CUSTOM_NAME, Text.literal("#" + entry.target()));
            lore.add(Text.literal("Tag entry (price varies by item)"));
        }

        lore.add(Text.literal("mult=" + entry.multiplier() + ", add=" + entry.additive()));
        if (entry.kind() == ShopEntry.Kind.ITEM) {
            long base = valuationConfig.resolve(entry.target(), resolveTags(entry.target()), currencyId).orElse(-1);
            if (base > 0) {
                lore.add(Text.literal("base=" + base));
            }
        }
        stack.set(DataComponentTypes.LORE, new LoreComponent(lore));
        return stack;
    }

    private static java.util.Set<String> resolveTags(String itemId) {
        try {
            var id = Identifier.of(itemId);
            var item = net.minecraft.registry.Registries.ITEM.get(id);
            if (item != null) {
                return item.getRegistryEntry().streamTags().map(t -> t.id().toString()).collect(java.util.stream.Collectors.toSet());
            }
        } catch (Exception ignored) {
        }
        return java.util.Set.of();
    }
}

package dev.coffer.adapter.fabric.execution.ui;

import dev.coffer.adapter.fabric.AdapterServices;
import dev.coffer.adapter.fabric.boundary.DeclaredExchangeRequest;
import dev.coffer.adapter.fabric.boundary.DeclaredIdentity;
import dev.coffer.adapter.fabric.boundary.DeclaredItem;
import dev.coffer.adapter.fabric.boundary.ExchangeIntent;
import dev.coffer.adapter.fabric.boundary.InvocationContext;
import dev.coffer.adapter.fabric.boundary.MetadataRelevance;
import dev.coffer.adapter.fabric.config.CurrencyConfig;
import dev.coffer.adapter.fabric.config.ItemBlacklistConfig;
import dev.coffer.adapter.fabric.execution.FabricCoreExecutor;
import dev.coffer.adapter.fabric.execution.FabricValuationService;
import dev.coffer.adapter.fabric.execution.MoneyFormatter;
import dev.coffer.core.ExchangeEvaluationResult;
import dev.coffer.core.ExchangeRequest;
import dev.coffer.core.ValuationSnapshot;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.Text;
import net.minecraft.registry.Registries;
import net.minecraft.util.Formatting;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class SellPreviewScreenHandler extends ScreenHandler {

    private final SellPreviewToken token;
    private final MoneyFormatter formatter;
    private final CurrencyConfig currencyConfig;
    private final PlayerInventory playerInventory;
    private final SimpleInventory inventory;
    private final int totalRows;
    private final int controlRowStart;

    // Control row indices relative to control row start
    private static final int CANCEL_INDEX = 3;
    private static final int TOTAL_INDEX = 4;
    private static final int CONFIRM_INDEX = 5;

    public SellPreviewScreenHandler(int syncId, int rows, SellPreviewToken token, MoneyFormatter formatter, CurrencyConfig currencyConfig, PlayerInventory playerInventory) {
        super(typeForRows(rows), syncId);
        this.token = Objects.requireNonNull(token, "token");
        this.formatter = Objects.requireNonNull(formatter, "formatter");
        this.currencyConfig = Objects.requireNonNull(currencyConfig, "currencyConfig");
        this.playerInventory = Objects.requireNonNull(playerInventory, "playerInventory");
        this.totalRows = Math.max(1, Math.min(6, rows));
        int size = this.totalRows * 9;
        this.controlRowStart = size - 9;
        this.inventory = new SimpleInventory(size);
        this.inventory.onOpen(null);

        populateControls();

        for (int i = 0; i < size; i++) {
            int row = i / 9;
            int col = i % 9;
            int x = 8 + col * 18;
            int y = 18 + row * 18;
            if (i >= controlRowStart) {
                this.addSlot(new ControlSlot(this.inventory, i, x, y));
            } else {
                this.addSlot(new ItemSlot(this.inventory, i, x, y));
            }
        }

        int m = (this.totalRows - 4) * 18;
        for (int row = 0; row < 3; ++row) {
            for (int col = 0; col < 9; ++col) {
                this.addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, 103 + row * 18 + m));
            }
        }

        for (int col = 0; col < 9; ++col) {
            this.addSlot(new Slot(playerInventory, col, 8 + col * 18, 161 + m));
        }

        recalc();
    }

    public static int computeRows(SellPreviewToken token) {
        // Always use a full chest (6 rows) with the bottom row as controls.
        return 6;
    }

    private void populateControls() {
        // Fill control row with dark panes
        ItemStack filler = new ItemStack(Items.BLACK_STAINED_GLASS_PANE);
        filler.set(DataComponentTypes.CUSTOM_NAME, Text.empty());
        for (int i = 0; i < 9; i++) {
            inventory.setStack(controlRowStart + i, filler.copy());
        }

        // Cancel
        ItemStack cancel = new ItemStack(Items.BARRIER);
        cancel.set(DataComponentTypes.CUSTOM_NAME, Text.literal("Cancel"));
        inventory.setStack(controlRowStart + CANCEL_INDEX, cancel);

        // Total display
        long total = 0L;
        if (token.valuationSnapshot() != null) {
            total = token.valuationSnapshot().totalAcceptedValue();
        }
        ItemStack totalItem = new ItemStack(Items.GOLD_INGOT);
        totalItem.set(DataComponentTypes.CUSTOM_NAME, Text.literal("Total"));
        List<Text> lore = new ArrayList<>();
        lore.add(Text.literal(formatter.format(total)));
        totalItem.set(DataComponentTypes.LORE, new LoreComponent(lore));
        inventory.setStack(controlRowStart + TOTAL_INDEX, totalItem);

        // Confirm
        ItemStack confirm;
        if (!token.evaluationResult().allowed()) {
            confirm = new ItemStack(Items.RED_STAINED_GLASS_PANE);
            confirm.set(DataComponentTypes.CUSTOM_NAME, Text.literal("Denied by Core"));
            confirm.set(DataComponentTypes.LORE, new LoreComponent(List.of(Text.literal("Adjust items and preview again."))));
        } else {
            confirm = new ItemStack(Items.LIME_STAINED_GLASS_PANE);
            confirm.set(DataComponentTypes.CUSTOM_NAME, Text.literal("Confirm"));
            List<Text> confirmLore = new ArrayList<>();
            confirmLore.add(Text.literal("Sell for " + formatter.format(total)));
            confirm.set(DataComponentTypes.LORE, new LoreComponent(confirmLore));
        }
        inventory.setStack(controlRowStart + CONFIRM_INDEX, confirm);
    }

    public void updateFromEvaluation(dev.coffer.core.ExchangeEvaluationResult eval,
                                     ItemBlacklistConfig blacklistConfig,
                                     ValuationSnapshot fallbackSnapshot) {
        int acceptedCount = 0;
        int rejectedCount = 0;
        dev.coffer.core.ValuationSnapshot snapshotObj = null;
        Object snapObj = eval.valuationSnapshot();
        if (snapObj instanceof dev.coffer.core.ValuationSnapshot snapshot) {
            snapshotObj = snapshot;
            for (dev.coffer.core.ValuationItemResult r : snapshot.itemResults()) {
                if (r.accepted()) acceptedCount++; else rejectedCount++;
            }
        } else if (fallbackSnapshot != null) {
            snapshotObj = fallbackSnapshot;
            for (dev.coffer.core.ValuationItemResult r : snapshotObj.itemResults()) {
                if (r.accepted()) acceptedCount++; else rejectedCount++;
            }
        }

        ItemStack totalItem = inventory.getStack(controlRowStart + TOTAL_INDEX);
        List<Text> lore = new ArrayList<>();
        if (snapshotObj != null) {
            snapshotObj.totalsByCurrency().forEach((cur, amt) -> lore.add(Text.literal(formatAmount(cur, amt))));
        } else {
            lore.add(Text.literal("No valuation"));
        }
        lore.add(Text.literal("Accepted: " + acceptedCount + ", Ignored: " + rejectedCount));
        totalItem.set(DataComponentTypes.LORE, new LoreComponent(lore));

        ItemStack confirm = inventory.getStack(controlRowStart + CONFIRM_INDEX);
        if (!eval.allowed()) {
            confirm = new ItemStack(Items.RED_STAINED_GLASS_PANE);
            confirm.set(DataComponentTypes.CUSTOM_NAME, Text.literal("Denied by Core"));
            confirm.set(DataComponentTypes.LORE, new LoreComponent(List.of(Text.literal("Adjust items and preview again."))));
        } else {
            confirm = new ItemStack(Items.LIME_STAINED_GLASS_PANE);
            confirm.set(DataComponentTypes.CUSTOM_NAME, Text.literal("Confirm"));
            List<Text> confirmLore = new ArrayList<>();
            if (snapshotObj != null) {
                snapshotObj.totalsByCurrency().forEach((cur, amt) -> confirmLore.add(Text.literal(formatAmount(cur, amt))));
            }
            confirm.set(DataComponentTypes.LORE, new LoreComponent(confirmLore));
        }
        inventory.setStack(controlRowStart + CONFIRM_INDEX, confirm);

        // annotate items
        for (int i = 0; i < controlRowStart; i++) {
            ItemStack stack = inventory.getStack(i);
            if (stack == null || stack.isEmpty()) continue;
            String itemId = Registries.ITEM.getId(stack.getItem()).toString();
            List<Text> loreItems = new ArrayList<>();
            boolean deniedByBlacklist = blacklistConfig != null && blacklistConfig.isDenied(itemId, resolveTags(itemId));
            if (deniedByBlacklist) {
                loreItems.add(Text.literal("Blacklisted").formatted(Formatting.DARK_GRAY, Formatting.ITALIC));
            } else if (snapshotObj != null) {
                dev.coffer.core.ValuationItemResult res = findResult(itemId, snapshotObj);
                if (res != null && res.accepted()) {
                    loreItems.add(Text.literal("Value: " + formatAmount(res.currencyId(), res.totalValue())).formatted(Formatting.GREEN));
                } else {
                    loreItems.add(Text.literal("Ignored: no value").formatted(Formatting.RED));
                }
            }
            stack.set(DataComponentTypes.LORE, new LoreComponent(loreItems));
        }
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return true;
    }

    @Override
    public void onClosed(PlayerEntity player) {
        super.onClosed(player);
        returnContentsTo(player);
    }

    @Override
    public void onContentChanged(Inventory changed) {
        super.onContentChanged(changed);
        if (changed == this.inventory) {
            recalc();
        }
    }

    @Override
    public ItemStack quickMove(PlayerEntity player, int index) {
        Slot slot = this.slots.get(index);
        if (slot == null || !slot.hasStack()) {
            return ItemStack.EMPTY;
        }

        ItemStack original = slot.getStack();
        ItemStack copy = original.copy();

        int invSize = this.controlRowStart;
        if (index < invSize) {
            stripAnnotations(original);
            if (!this.insertItem(original, invSize, this.slots.size(), true)) {
                return ItemStack.EMPTY;
            }
        } else {
            if (!this.insertItem(original, 0, invSize, false)) {
                return ItemStack.EMPTY;
            }
        }

        if (original.isEmpty()) {
            slot.setStack(ItemStack.EMPTY);
        } else {
            slot.markDirty();
        }

        return copy;
    }

    @Override
    public void onSlotClick(int slotIndex, int button, SlotActionType actionType, PlayerEntity player) {
        if (slotIndex >= 0 && slotIndex < this.slots.size()) {
            Slot slot = this.slots.get(slotIndex);
            if (slot != null && slot.inventory == this.inventory && slot.getIndex() >= controlRowStart) {
                int rel = slot.getIndex() - controlRowStart;
                if (rel == CANCEL_INDEX) {
                    SellPreviewManager.cancel((net.minecraft.server.network.ServerPlayerEntity) player);
                    return;
                } else if (rel == CONFIRM_INDEX) {
                    SellPreviewManager.confirm((net.minecraft.server.network.ServerPlayerEntity) player);
                    return;
                }
                return; // ignore other control slots
            }
        }
        super.onSlotClick(slotIndex, button, actionType, player);
        recalc();
    }

    void recalc() {
        if (playerInventory == null || playerInventory.player == null) return;
        var snapshot = AdapterServices.get().orElse(null);
        if (snapshot == null) return;
        FabricCoreExecutor core = snapshot.coreExecutor();
        DeclaredExchangeRequest req = buildRequest(playerInventory.player.getUuid());
        ExchangeEvaluationResult eval = core.execute(req);
        ValuationSnapshot fallback = null;
        if (!(eval.valuationSnapshot() instanceof ValuationSnapshot)) {
            FabricValuationService fv = new FabricValuationService(snapshot.valuationConfig(), snapshot.blacklistConfig(), snapshot.currencyConfig().defaultCurrency().id());
            fallback = fv.valuate(new ExchangeRequest(req.invoker(), req.intent(), req));
        }
        updateFromEvaluation(eval, snapshot.blacklistConfig(), fallback);
        sendContentUpdates();
    }

    private DeclaredExchangeRequest buildRequest(java.util.UUID playerId) {
        List<DeclaredItem> items = new ArrayList<>();
        for (int i = 0; i < controlRowStart; i++) {
            ItemStack stack = inventory.getStack(i);
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

    public boolean hasAnyItems() {
        for (int i = 0; i < controlRowStart; i++) {
            ItemStack stack = inventory.getStack(i);
            if (stack != null && !stack.isEmpty()) {
                return true;
            }
        }
        return false;
    }

    private static dev.coffer.core.ValuationItemResult findResult(String itemId, dev.coffer.core.ValuationSnapshot snapshot) {
        for (dev.coffer.core.ValuationItemResult r : snapshot.itemResults()) {
            if (r.item() instanceof DeclaredItem di && di.itemId().equals(itemId)) {
                return r;
            }
        }
        return null;
    }

    private String formatAmount(String currencyId, long amount) {
        if (currencyId != null) {
            var def = currencyConfig.find(currencyId).orElse(null);
            if (def != null) {
                return new MoneyFormatter(def).format(amount);
            }
        }
        return amount + " " + (currencyId == null ? "" : currencyId);
    }

    private static java.util.Set<String> resolveTags(String itemId) {
        java.util.Set<String> tags = new java.util.HashSet<>();
        try {
            var id = Identifier.of(itemId);
            var item = Registries.ITEM.get(id);
            if (item != null) {
                for (TagKey<?> tag : item.getRegistryEntry().streamTags().toList()) {
                    tags.add(tag.id().toString());
                }
            }
        } catch (Exception e) {
            // ignore
        }
        return tags;
    }

    private static void stripAnnotations(ItemStack stack) {
        if (stack == null) return;
        stack.set(DataComponentTypes.LORE, null);
    }

    private static ScreenHandlerType<?> typeForRows(int rows) {
        return switch (Math.max(1, Math.min(6, rows))) {
            case 1 -> ScreenHandlerType.GENERIC_9X1;
            case 2 -> ScreenHandlerType.GENERIC_9X2;
            case 3 -> ScreenHandlerType.GENERIC_9X3;
            case 4 -> ScreenHandlerType.GENERIC_9X4;
            case 5 -> ScreenHandlerType.GENERIC_9X5;
            default -> ScreenHandlerType.GENERIC_9X6;
        };
    }

    public void returnContentsTo(PlayerEntity player) {
        if (player == null) return;
        for (int i = 0; i < controlRowStart; i++) {
            ItemStack stack = inventory.removeStack(i);
            if (stack == null || stack.isEmpty()) continue;
            stripAnnotations(stack);
            if (!playerInventory.insertStack(stack)) {
                player.dropItem(stack, false);
            }
        }
    }

    public SimpleInventory itemsInventory() {
        return inventory;
    }

    public int controlRowStart() {
        return controlRowStart;
    }

    private static final class ControlSlot extends Slot {
        public ControlSlot(SimpleInventory inventory, int index, int x, int y) {
            super(inventory, index, x, y);
        }

        @Override
        public boolean canInsert(ItemStack stack) {
            return false;
        }

        @Override
        public boolean canTakeItems(PlayerEntity playerEntity) {
            return false;
        }
    }

    private static final class ItemSlot extends Slot {
        public ItemSlot(SimpleInventory inventory, int index, int x, int y) {
            super(inventory, index, x, y);
        }

        @Override
        public void onTakeItem(PlayerEntity player, ItemStack stack) {
            stripAnnotations(stack);
            super.onTakeItem(player, stack);
        }
    }
}

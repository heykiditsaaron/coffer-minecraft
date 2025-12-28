package dev.coffer.adapter.fabric.execution.ui;

import dev.coffer.adapter.fabric.AdapterServices;
import dev.coffer.adapter.fabric.boundary.DeclaredShopPurchase;
import dev.coffer.adapter.fabric.config.ShopEntry;
import dev.coffer.adapter.fabric.execution.MoneyFormatter;
import dev.coffer.adapter.fabric.execution.ShopPurchasePlan;
import dev.coffer.adapter.fabric.execution.ShopPurchasePlanner;
import dev.coffer.adapter.fabric.execution.ShopPurchasePlanningResult;
import dev.coffer.adapter.fabric.execution.ShopPurchaseTransactionExecutor;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Admin shop UI: clicking an entry attempts a purchase (quantity 1) via Core + planner + executor.
 */
public final class AdminShopScreenHandler extends ScreenHandler {

    private final Inventory inventory;
    private final String shopId;
    private final Map<Integer, ShopEntry> entryBySlot = new HashMap<>();

    public AdminShopScreenHandler(int syncId, int rows, Inventory sourceInventory, String shopId, List<ShopEntry> entries) {
        super(typeForRows(rows), syncId);
        this.shopId = Objects.requireNonNull(shopId, "shopId");
        int actualRows = Math.max(1, Math.min(6, rows));
        int size = actualRows * 9;
        if (sourceInventory.size() < size) {
            this.inventory = new SimpleInventory(size);
            for (int i = 0; i < Math.min(size, sourceInventory.size()); i++) {
                this.inventory.setStack(i, sourceInventory.getStack(i));
            }
        } else {
            this.inventory = sourceInventory;
        }
        this.inventory.onOpen(null);

        int index = 0;
        for (int row = 0; row < actualRows; row++) {
            for (int col = 0; col < 9; col++) {
                int x = 8 + col * 18;
                int y = 18 + row * 18;
                this.addSlot(new ClickableSlot(this.inventory, index, x, y));
                if (entries != null && index < entries.size()) {
                    entryBySlot.put(index, entries.get(index));
                }
                index++;
            }
        }
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

    @Override
    public boolean canUse(PlayerEntity player) {
        return true;
    }

    @Override
    public ItemStack quickMove(PlayerEntity player, int index) {
        return ItemStack.EMPTY;
    }

    @Override
    public void onSlotClick(int slotIndex, int button, SlotActionType actionType, PlayerEntity player) {
        if (!(player instanceof ServerPlayerEntity serverPlayer)) {
            return;
        }
        if (slotIndex < 0 || slotIndex >= this.slots.size()) return;
        Slot slot = this.slots.get(slotIndex);
        if (slot.inventory == this.inventory) {
            // Block vanilla quick-move/shift-click behavior entirely for shop entries.
            if (actionType == SlotActionType.QUICK_MOVE) {
                sendContentUpdates();
                return;
            }
            ShopEntry entry = entryBySlot.get(slotIndex);
            if (entry == null) return;
            if (entry.kind() != ShopEntry.Kind.ITEM) {
                serverPlayer.sendMessage(Text.literal("[Coffer] This entry requires a specific item; use a tagged item to price/purchase."), false);
                return;
            }

            AdapterServices.Snapshot snapshot = AdapterServices.get().orElse(null);
            if (snapshot == null) {
                serverPlayer.sendMessage(Text.literal("[Coffer] Adapter services unavailable."), false);
                return;
            }

            String currencyId = snapshot.currencyConfig().defaultCurrency().id();
            var declared = DeclaredShopPurchase.of(shopId, entry.target(), 1, serverPlayer.getUuid());
            var evalResult = snapshot.coreExecutor().execute(declared);
            if (!evalResult.allowed()) {
                String reason = evalResult.denialReason() == null ? "Denied." : "Denied: " + evalResult.denialReason();
                serverPlayer.sendMessage(Text.literal("[Coffer] " + reason + " No changes were made."), false);
                return;
            }

            ShopPurchasePlanner planner = snapshot.shopPurchasePlanner();
            ShopPurchasePlanningResult planning = planner.plan(declared, evalResult, currencyId);
            if (!planning.planned()) {
                String msg = planning.refusal().orElse("Unable to plan purchase. No changes were made.");
                serverPlayer.sendMessage(Text.literal("[Coffer] " + msg), false);
                return;
            }

            ShopPurchasePlan plan = planning.plan().orElseThrow();
            ShopPurchaseTransactionExecutor executor = snapshot.shopPurchaseTransactionExecutor();
            var execResult = executor.execute(serverPlayer, plan);
            if (!execResult.success()) {
                serverPlayer.sendMessage(Text.literal("[Coffer] No changes were made (" + execResult.reason() + ")."), false);
                return;
            }

            MoneyFormatter formatter = snapshot.moneyFormatter();
            serverPlayer.sendMessage(Text.literal("[Coffer] Purchased for " + formatter.format(plan.cost()) + "."), false);
            // clear any client-held cursor/ghost items and resync
            serverPlayer.currentScreenHandler.setCursorStack(ItemStack.EMPTY);
            sendContentUpdates(); // resync to prevent client-held ghost stacks
            return;
        }
        super.onSlotClick(slotIndex, button, actionType, player);
    }

    private static final class ClickableSlot extends Slot {
        public ClickableSlot(Inventory inventory, int index, int x, int y) {
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
}

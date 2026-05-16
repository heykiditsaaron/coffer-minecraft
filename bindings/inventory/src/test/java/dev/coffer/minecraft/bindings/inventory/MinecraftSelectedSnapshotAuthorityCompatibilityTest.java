package dev.coffer.minecraft.bindings.inventory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import java.util.List;
import net.minecraft.Bootstrap;
import net.minecraft.SharedConstants;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import org.coffer.firstparty.authority.transferablevalue.port.RemovabilityResult;
import org.coffer.firstparty.authority.transferablevalue.port.TransferableValueSet;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class MinecraftSelectedSnapshotAuthorityCompatibilityTest {
    @BeforeAll
    static void bootstrapMinecraftRegistries() {
        SharedConstants.createGameVersion();
        Bootstrap.initialize();
    }

    @Test
    void hotbarRegionRemovalWouldAcceptDuplicateMaterialOutsideOriginalSelectedSlot() {
        List<ItemStack> hotbarAfterSelectedSlotDrifted = List.of(
                new ItemStack(Items.STONE, 64),
                ItemStack.EMPTY,
                ItemStack.EMPTY,
                ItemStack.EMPTY,
                ItemStack.EMPTY,
                ItemStack.EMPTY,
                ItemStack.EMPTY,
                ItemStack.EMPTY,
                ItemStack.EMPTY);
        MinecraftPlayerInventoryContainer container = new MinecraftPlayerInventoryContainer(
                java.util.UUID.fromString("00000000-0000-0000-0000-000000000904"),
                MinecraftPlayerInventoryContainer.Region.HOTBAR,
                hotbarAfterSelectedSlotDrifted);

        MinecraftItemDescriptor descriptorFromEarlierSelectedCapture =
                new MinecraftItemDescriptor("minecraft:stone", 3, java.util.Optional.empty());

        RemovabilityResult removability =
                container.canRemove(new TransferableValueSet(List.of(descriptorFromEarlierSelectedCapture)));

        assertInstanceOf(RemovabilityResult.Success.class, removability);
        assertEquals(MinecraftPlayerInventoryContainer.Region.HOTBAR, container.region());
        assertEquals(64, hotbarAfterSelectedSlotDrifted.get(0).getCount());
        assertEquals(0, hotbarAfterSelectedSlotDrifted.get(2).getCount());
    }
}

package dev.coffer.minecraft.bindings.inventory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.List;
import net.minecraft.Bootstrap;
import net.minecraft.SharedConstants;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.StringNbtReader;
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
    void equivalentPlainValueElsewhereInHotbarCanSatisfyRemovalAfterSelectedSlotDrift() {
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

    @Test
    void equivalentEnchantedValueElsewhereInHotbarCanSatisfyRemovalAfterSelectedSlotDrift()
            throws CommandSyntaxException {
        String authoredSwordNbt = "{Enchantments:[{id:\"minecraft:sharpness\",lvl:3s}],Damage:5}";
        List<ItemStack> hotbarAfterSelectedSlotDrifted = List.of(
                ItemStack.EMPTY,
                ItemStack.EMPTY,
                ItemStack.EMPTY,
                ItemStack.EMPTY,
                stackWithNbt(Items.IRON_SWORD, 1, authoredSwordNbt),
                ItemStack.EMPTY,
                ItemStack.EMPTY,
                ItemStack.EMPTY,
                ItemStack.EMPTY);
        MinecraftPlayerInventoryContainer container = new MinecraftPlayerInventoryContainer(
                java.util.UUID.fromString("00000000-0000-0000-0000-000000000904"),
                MinecraftPlayerInventoryContainer.Region.HOTBAR,
                hotbarAfterSelectedSlotDrifted);

        MinecraftItemDescriptor descriptorFromEarlierSelectedCapture =
                new MinecraftItemDescriptor("minecraft:iron_sword", 1, java.util.Optional.of(authoredSwordNbt));

        RemovabilityResult removability =
                container.canRemove(new TransferableValueSet(List.of(descriptorFromEarlierSelectedCapture)));

        assertInstanceOf(RemovabilityResult.Success.class, removability);
    }

    @Test
    void plainMaterialElsewhereCannotSatisfyEnchantedSelectedOffer() throws CommandSyntaxException {
        String authoredSwordNbt = "{Enchantments:[{id:\"minecraft:sharpness\",lvl:3s}],Damage:5}";
        List<ItemStack> hotbarAfterSelectedSlotDrifted = List.of(
                ItemStack.EMPTY,
                ItemStack.EMPTY,
                ItemStack.EMPTY,
                ItemStack.EMPTY,
                new ItemStack(Items.IRON_SWORD, 1),
                ItemStack.EMPTY,
                ItemStack.EMPTY,
                ItemStack.EMPTY,
                ItemStack.EMPTY);
        MinecraftPlayerInventoryContainer container = new MinecraftPlayerInventoryContainer(
                java.util.UUID.fromString("00000000-0000-0000-0000-000000000904"),
                MinecraftPlayerInventoryContainer.Region.HOTBAR,
                hotbarAfterSelectedSlotDrifted);

        MinecraftItemDescriptor descriptorFromEarlierSelectedCapture =
                new MinecraftItemDescriptor("minecraft:iron_sword", 1, java.util.Optional.of(authoredSwordNbt));

        RemovabilityResult removability =
                container.canRemove(new TransferableValueSet(List.of(descriptorFromEarlierSelectedCapture)));

        RemovabilityResult.Failed failed = assertInstanceOf(RemovabilityResult.Failed.class, removability);
        assertEquals(MinecraftPlayerInventoryContainer.VALUE_NOT_REMOVABLE, failed.reasonCode());
    }

    @Test
    void differentDurabilityElsewhereCannotSatisfySelectedOffer() throws CommandSyntaxException {
        String authoredSwordNbt = "{Damage:5}";
        String differentDurabilityNbt = "{Damage:1}";
        List<ItemStack> hotbarAfterSelectedSlotDrifted = List.of(
                ItemStack.EMPTY,
                ItemStack.EMPTY,
                ItemStack.EMPTY,
                ItemStack.EMPTY,
                stackWithNbt(Items.IRON_SWORD, 1, differentDurabilityNbt),
                ItemStack.EMPTY,
                ItemStack.EMPTY,
                ItemStack.EMPTY,
                ItemStack.EMPTY);
        MinecraftPlayerInventoryContainer container = new MinecraftPlayerInventoryContainer(
                java.util.UUID.fromString("00000000-0000-0000-0000-000000000904"),
                MinecraftPlayerInventoryContainer.Region.HOTBAR,
                hotbarAfterSelectedSlotDrifted);

        MinecraftItemDescriptor descriptorFromEarlierSelectedCapture =
                new MinecraftItemDescriptor("minecraft:iron_sword", 1, java.util.Optional.of(authoredSwordNbt));

        RemovabilityResult removability =
                container.canRemove(new TransferableValueSet(List.of(descriptorFromEarlierSelectedCapture)));

        RemovabilityResult.Failed failed = assertInstanceOf(RemovabilityResult.Failed.class, removability);
        assertEquals(MinecraftPlayerInventoryContainer.VALUE_NOT_REMOVABLE, failed.reasonCode());
    }

    private static ItemStack stackWithNbt(ItemConvertible item, int count, String nbt)
            throws CommandSyntaxException {
        ItemStack stack = new ItemStack(item, count);
        stack.setNbt(StringNbtReader.parse(nbt));
        return stack;
    }
}

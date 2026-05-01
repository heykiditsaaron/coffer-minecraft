package dev.coffer.minecraft.bindings.inventory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
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

class MinecraftPlayerInventoryContainerTest {
    private static final UUID PLAYER_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");

    @BeforeAll
    static void bootstrapMinecraftRegistries() {
        SharedConstants.createGameVersion();
        Bootstrap.initialize();
    }

    @Test
    void singleStackSufficientReturnsSuccess() {
        MinecraftPlayerInventoryContainer container = container(List.of(new ItemStack(Items.STONE, 8)));

        RemovabilityResult result = container.canRemove(values(descriptor("minecraft:stone", 4)));

        assertInstanceOf(RemovabilityResult.Success.class, result);
    }

    @Test
    void multipleStacksCombineReturnsSuccess() {
        MinecraftPlayerInventoryContainer container =
                container(List.of(new ItemStack(Items.STONE, 3), new ItemStack(Items.STONE, 4)));

        RemovabilityResult result = container.canRemove(values(descriptor("minecraft:stone", 7)));

        assertInstanceOf(RemovabilityResult.Success.class, result);
    }

    @Test
    void insufficientQuantityFails() {
        MinecraftPlayerInventoryContainer container = container(List.of(new ItemStack(Items.STONE, 3)));

        RemovabilityResult result = container.canRemove(values(descriptor("minecraft:stone", 4)));

        RemovabilityResult.Failed failed = assertInstanceOf(RemovabilityResult.Failed.class, result);
        assertEquals(MinecraftPlayerInventoryContainer.VALUE_NOT_REMOVABLE, failed.reasonCode());
    }

    @Test
    void noMatchingItemsFails() {
        MinecraftPlayerInventoryContainer container = container(List.of(new ItemStack(Items.DIRT, 8)));

        RemovabilityResult result = container.canRemove(values(descriptor("minecraft:stone", 1)));

        RemovabilityResult.Failed failed = assertInstanceOf(RemovabilityResult.Failed.class, result);
        assertEquals(MinecraftPlayerInventoryContainer.VALUE_NOT_REMOVABLE, failed.reasonCode());
    }

    @Test
    void nbtMismatchPreventsMatching() throws CommandSyntaxException {
        MinecraftPlayerInventoryContainer container =
                container(List.of(stackWithNbt(Items.STONE, 8, "{custom:1b}")));

        RemovabilityResult result = container.canRemove(values(
                new MinecraftItemDescriptor("minecraft:stone", 1, Optional.of("{custom:2b}"))));

        RemovabilityResult.Failed failed = assertInstanceOf(RemovabilityResult.Failed.class, result);
        assertEquals(MinecraftPlayerInventoryContainer.VALUE_NOT_REMOVABLE, failed.reasonCode());
    }

    @Test
    void emptyInventoryFails() {
        MinecraftPlayerInventoryContainer container = container(List.of());

        RemovabilityResult result = container.canRemove(values(descriptor("minecraft:stone", 1)));

        RemovabilityResult.Failed failed = assertInstanceOf(RemovabilityResult.Failed.class, result);
        assertEquals(MinecraftPlayerInventoryContainer.VALUE_NOT_REMOVABLE, failed.reasonCode());
    }

    @Test
    void canRemoveDoesNotMutateInventory() throws CommandSyntaxException {
        ItemStack first = new ItemStack(Items.STONE, 3);
        ItemStack second = stackWithNbt(Items.STONE, 4, "{custom:1b}");
        String secondNbtBefore = second.getNbt().toString();
        MinecraftPlayerInventoryContainer container = container(List.of(first, second));

        container.canRemove(values(descriptor("minecraft:stone", 2)));

        assertEquals(3, first.getCount());
        assertEquals(4, second.getCount());
        assertEquals(secondNbtBefore, second.getNbt().toString());
    }

    private static MinecraftPlayerInventoryContainer container(List<ItemStack> slots) {
        return new MinecraftPlayerInventoryContainer(
                PLAYER_ID,
                MinecraftPlayerInventoryContainer.Region.MAIN,
                slots);
    }

    private static TransferableValueSet values(MinecraftItemDescriptor descriptor) {
        return new TransferableValueSet(List.of(descriptor));
    }

    private static MinecraftItemDescriptor descriptor(String itemId, long quantity) {
        return new MinecraftItemDescriptor(itemId, quantity, Optional.empty());
    }

    private static ItemStack stackWithNbt(ItemConvertible item, int count, String nbt)
            throws CommandSyntaxException {
        ItemStack stack = new ItemStack(item, count);
        stack.setNbt(StringNbtReader.parse(nbt));
        return stack;
    }
}

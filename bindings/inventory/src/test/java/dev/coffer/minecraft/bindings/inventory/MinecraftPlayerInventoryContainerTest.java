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
import org.coffer.firstparty.authority.transferablevalue.port.ReceivabilityResult;
import org.coffer.firstparty.authority.transferablevalue.port.RemovabilityResult;
import org.coffer.firstparty.authority.transferablevalue.port.SimulationResult;
import org.coffer.firstparty.authority.transferablevalue.port.TransferableValueDescriptor;
import org.coffer.firstparty.authority.transferablevalue.port.TransferableValueSet;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertThrows;

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

    @Test
    void emptySlotCanReceiveItem() {
        MinecraftPlayerInventoryContainer container = container(List.of(ItemStack.EMPTY));

        ReceivabilityResult result = container.canReceive(values(descriptor("minecraft:stone", 64)));

        assertInstanceOf(ReceivabilityResult.Success.class, result);
    }

    @Test
    void partialMatchingStackCanReceiveMore() {
        MinecraftPlayerInventoryContainer container = container(List.of(new ItemStack(Items.STONE, 60)));

        ReceivabilityResult result = container.canReceive(values(descriptor("minecraft:stone", 4)));

        assertInstanceOf(ReceivabilityResult.Success.class, result);
    }

    @Test
    void fullMatchingStackPlusEmptySlotCanReceiveRemainder() {
        MinecraftPlayerInventoryContainer container =
                container(List.of(new ItemStack(Items.STONE, 64), ItemStack.EMPTY));

        ReceivabilityResult result = container.canReceive(values(descriptor("minecraft:stone", 1)));

        assertInstanceOf(ReceivabilityResult.Success.class, result);
    }

    @Test
    void fullInventoryCannotReceive() {
        MinecraftPlayerInventoryContainer container =
                container(List.of(new ItemStack(Items.STONE, 64), new ItemStack(Items.DIRT, 64)));

        ReceivabilityResult result = container.canReceive(values(descriptor("minecraft:diamond", 1)));

        ReceivabilityResult.Failed failed = assertInstanceOf(ReceivabilityResult.Failed.class, result);
        assertEquals(MinecraftPlayerInventoryContainer.VALUE_NOT_RECEIVABLE, failed.reasonCode());
    }

    @Test
    void nbtMismatchDoesNotMergeIntoOccupiedStack() throws CommandSyntaxException {
        MinecraftPlayerInventoryContainer container =
                container(List.of(stackWithNbt(Items.STONE, 63, "{custom:1b}")));

        ReceivabilityResult result = container.canReceive(values(
                new MinecraftItemDescriptor("minecraft:stone", 1, Optional.of("{custom:2b}"))));

        ReceivabilityResult.Failed failed = assertInstanceOf(ReceivabilityResult.Failed.class, result);
        assertEquals(MinecraftPlayerInventoryContainer.VALUE_NOT_RECEIVABLE, failed.reasonCode());
    }

    @Test
    void multipleIncomingValuesAreEvaluatedTogether() {
        MinecraftPlayerInventoryContainer container = container(List.of(ItemStack.EMPTY));

        ReceivabilityResult result = container.canReceive(values(
                descriptor("minecraft:stone", 64),
                descriptor("minecraft:dirt", 64)));

        ReceivabilityResult.Failed failed = assertInstanceOf(ReceivabilityResult.Failed.class, result);
        assertEquals(MinecraftPlayerInventoryContainer.VALUE_NOT_RECEIVABLE, failed.reasonCode());
    }

    @Test
    void canReceiveDoesNotMutateInventory() throws CommandSyntaxException {
        ItemStack first = new ItemStack(Items.STONE, 60);
        ItemStack second = stackWithNbt(Items.DIRT, 10, "{custom:1b}");
        String secondNbtBefore = second.getNbt().toString();
        MinecraftPlayerInventoryContainer container = container(List.of(first, second, ItemStack.EMPTY));

        container.canReceive(values(descriptor("minecraft:stone", 10)));

        assertEquals(60, first.getCount());
        assertEquals(10, second.getCount());
        assertEquals(secondNbtBefore, second.getNbt().toString());
    }

    @Test
    void nonMinecraftDescriptorFailsSafely() {
        MinecraftPlayerInventoryContainer container = container(List.of(ItemStack.EMPTY));

        ReceivabilityResult result = container.canReceive(values(new TransferableValueDescriptor() {
            @Override
            public long quantity() {
                return 1;
            }
        }));

        ReceivabilityResult.Failed failed = assertInstanceOf(ReceivabilityResult.Failed.class, result);
        assertEquals(MinecraftPlayerInventoryContainer.VALUE_NOT_RECEIVABLE, failed.reasonCode());
    }

    @Test
    void successfulSimulationReturnsSuccess() {
        MinecraftPlayerInventoryContainer first =
                container(List.of(new ItemStack(Items.STONE, 5), ItemStack.EMPTY));
        MinecraftPlayerInventoryContainer second =
                container(List.of(new ItemStack(Items.DIRT, 5), ItemStack.EMPTY));

        SimulationResult result = first.simulateAtomicSwap(
                second,
                values(descriptor("minecraft:stone", 3)),
                values(descriptor("minecraft:dirt", 3)));

        assertInstanceOf(SimulationResult.Success.class, result);
    }

    @Test
    void simulationDoesNotMutateEitherInventory() throws CommandSyntaxException {
        ItemStack firstStone = new ItemStack(Items.STONE, 5);
        ItemStack firstTagged = stackWithNbt(Items.DIRT, 2, "{custom:1b}");
        ItemStack secondDirt = new ItemStack(Items.DIRT, 5);
        MinecraftPlayerInventoryContainer first = container(List.of(firstStone, firstTagged, ItemStack.EMPTY));
        MinecraftPlayerInventoryContainer second = container(List.of(secondDirt, ItemStack.EMPTY));
        String firstTaggedNbtBefore = firstTagged.getNbt().toString();

        first.simulateAtomicSwap(
                second,
                values(descriptor("minecraft:stone", 3)),
                values(descriptor("minecraft:dirt", 3)));

        assertEquals(5, firstStone.getCount());
        assertEquals(2, firstTagged.getCount());
        assertEquals(firstTaggedNbtBefore, firstTagged.getNbt().toString());
        assertEquals(5, secondDirt.getCount());
    }

    @Test
    void simulationFailsWhenThisContainerLacksOutgoingValues() {
        MinecraftPlayerInventoryContainer first = container(List.of(new ItemStack(Items.DIRT, 5), ItemStack.EMPTY));
        MinecraftPlayerInventoryContainer second = container(List.of(new ItemStack(Items.STONE, 5), ItemStack.EMPTY));

        SimulationResult result = first.simulateAtomicSwap(
                second,
                values(descriptor("minecraft:stone", 3)),
                values(descriptor("minecraft:stone", 3)));

        SimulationResult.Failed failed = assertInstanceOf(SimulationResult.Failed.class, result);
        assertEquals(MinecraftPlayerInventoryContainer.VALUE_NOT_REMOVABLE, failed.reasonCode());
    }

    @Test
    void simulationFailsWhenOtherContainerLacksOutgoingValues() {
        MinecraftPlayerInventoryContainer first = container(List.of(new ItemStack(Items.STONE, 5), ItemStack.EMPTY));
        MinecraftPlayerInventoryContainer second = container(List.of(new ItemStack(Items.DIRT, 5), ItemStack.EMPTY));

        SimulationResult result = first.simulateAtomicSwap(
                second,
                values(descriptor("minecraft:stone", 3)),
                values(descriptor("minecraft:stone", 3)));

        SimulationResult.Failed failed = assertInstanceOf(SimulationResult.Failed.class, result);
        assertEquals(MinecraftPlayerInventoryContainer.VALUE_NOT_REMOVABLE, failed.reasonCode());
    }

    @Test
    void simulationFailsWhenThisContainerCannotReceiveIncomingValues() {
        MinecraftPlayerInventoryContainer first = container(List.of(new ItemStack(Items.STONE, 64)));
        MinecraftPlayerInventoryContainer second =
                container(List.of(new ItemStack(Items.DIRT, 64), new ItemStack(Items.DIAMOND, 64), ItemStack.EMPTY));

        SimulationResult result = first.simulateAtomicSwap(
                second,
                values(descriptor("minecraft:stone", 1)),
                values(descriptor("minecraft:dirt", 64), descriptor("minecraft:diamond", 64)));

        SimulationResult.Failed failed = assertInstanceOf(SimulationResult.Failed.class, result);
        assertEquals(MinecraftPlayerInventoryContainer.VALUE_NOT_RECEIVABLE, failed.reasonCode());
    }

    @Test
    void simulationFailsWhenOtherContainerCannotReceiveIncomingValues() {
        MinecraftPlayerInventoryContainer first =
                container(List.of(new ItemStack(Items.DIRT, 64), new ItemStack(Items.DIAMOND, 64), ItemStack.EMPTY));
        MinecraftPlayerInventoryContainer second = container(List.of(new ItemStack(Items.STONE, 64)));

        SimulationResult result = first.simulateAtomicSwap(
                second,
                values(descriptor("minecraft:dirt", 64), descriptor("minecraft:diamond", 64)),
                values(descriptor("minecraft:stone", 1)));

        SimulationResult.Failed failed = assertInstanceOf(SimulationResult.Failed.class, result);
        assertEquals(MinecraftPlayerInventoryContainer.VALUE_NOT_RECEIVABLE, failed.reasonCode());
    }

    @Test
    void nbtMismatchPreventsSimulatedRemoval() throws CommandSyntaxException {
        MinecraftPlayerInventoryContainer first =
                container(List.of(stackWithNbt(Items.STONE, 5, "{custom:1b}"), ItemStack.EMPTY));
        MinecraftPlayerInventoryContainer second =
                container(List.of(new ItemStack(Items.DIRT, 5), ItemStack.EMPTY));

        SimulationResult result = first.simulateAtomicSwap(
                second,
                values(new MinecraftItemDescriptor("minecraft:stone", 1, Optional.of("{custom:2b}"))),
                values(descriptor("minecraft:dirt", 1)));

        SimulationResult.Failed failed = assertInstanceOf(SimulationResult.Failed.class, result);
        assertEquals(MinecraftPlayerInventoryContainer.VALUE_NOT_REMOVABLE, failed.reasonCode());
    }

    @Test
    void multiValueSwapSucceedsOnlyWhenAllValuesFit() {
        MinecraftPlayerInventoryContainer first =
                container(List.of(new ItemStack(Items.STONE, 64), new ItemStack(Items.DIAMOND, 64), ItemStack.EMPTY));
        MinecraftPlayerInventoryContainer second =
                container(List.of(new ItemStack(Items.DIRT, 64), ItemStack.EMPTY));

        SimulationResult success = first.simulateAtomicSwap(
                second,
                values(descriptor("minecraft:stone", 64), descriptor("minecraft:diamond", 64)),
                values(descriptor("minecraft:dirt", 64)));

        assertInstanceOf(SimulationResult.Success.class, success);

        MinecraftPlayerInventoryContainer constrainedSecond =
                container(List.of(new ItemStack(Items.DIRT, 64)));
        SimulationResult failedResult = first.simulateAtomicSwap(
                constrainedSecond,
                values(descriptor("minecraft:stone", 64), descriptor("minecraft:diamond", 64)),
                values(descriptor("minecraft:dirt", 64)));

        SimulationResult.Failed failed = assertInstanceOf(SimulationResult.Failed.class, failedResult);
        assertEquals(MinecraftPlayerInventoryContainer.VALUE_NOT_RECEIVABLE, failed.reasonCode());
    }

    @Test
    void applyAtomicSwapRemainsUnsupported() {
        MinecraftPlayerInventoryContainer first = container(List.of(new ItemStack(Items.STONE, 1)));
        MinecraftPlayerInventoryContainer second = container(List.of(new ItemStack(Items.DIRT, 1)));

        assertThrows(
                UnsupportedOperationException.class,
                () -> first.applyAtomicSwap(
                        second,
                        values(descriptor("minecraft:stone", 1)),
                        values(descriptor("minecraft:dirt", 1))));
    }

    private static MinecraftPlayerInventoryContainer container(List<ItemStack> slots) {
        return new MinecraftPlayerInventoryContainer(
                PLAYER_ID,
                MinecraftPlayerInventoryContainer.Region.MAIN,
                slots);
    }

    private static TransferableValueSet values(TransferableValueDescriptor... descriptors) {
        return new TransferableValueSet(List.of(descriptors));
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

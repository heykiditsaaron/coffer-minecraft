package dev.coffer.minecraft.bindings.inventory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.Bootstrap;
import net.minecraft.SharedConstants;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import org.coffer.core.model.id.ActorRef;
import org.coffer.core.model.id.AuthorityIdentifier;
import org.coffer.core.model.id.ValueRef;
import org.coffer.core.model.request.ActorDeclaration;
import org.coffer.core.model.request.ValueDeclaration;
import org.coffer.core.model.support.OpaqueObject;
import org.coffer.firstparty.authority.transferablevalue.port.TransferableValueContainer;
import org.coffer.firstparty.authority.transferablevalue.port.TransferableValueDescriptor;
import org.coffer.firstparty.authority.transferablevalue.port.TransferableValueSet;
import org.coffer.firstparty.authority.transferablevalue.runtime.TransferableValueRuntimeValueEntry;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class MinecraftCofferCollaboratorsTest {
    private static final AuthorityIdentifier AUTHORITY = new AuthorityIdentifier("TransferableValueAuthority");
    private static final UUID PLAYER_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final ActorRef PLAYER_ACTOR =
            new ActorRef("player:" + PLAYER_ID + ":inventory:main");

    @BeforeAll
    static void bootstrapMinecraftRegistries() {
        SharedConstants.createGameVersion();
        Bootstrap.initialize();
    }

    @Test
    void validDescriptorCreatesMinecraftItemDescriptor() {
        MinecraftDescriptorFactory factory = new MinecraftDescriptorFactory();

        Optional<TransferableValueDescriptor> descriptor = factory.create(value(Map.of(
                MinecraftDescriptorFactory.ITEM_ID, "minecraft:stone",
                MinecraftDescriptorFactory.QUANTITY, 4,
                MinecraftDescriptorFactory.NBT_PAYLOAD, "{custom:1b}")), "minecraft");

        MinecraftItemDescriptor minecraftDescriptor =
                assertInstanceOf(MinecraftItemDescriptor.class, descriptor.orElseThrow());
        assertEquals("minecraft:stone", minecraftDescriptor.itemId());
        assertEquals(4, minecraftDescriptor.quantity());
        assertEquals("{custom:1b}", minecraftDescriptor.nbtPayload().orElseThrow());
    }

    @Test
    void malformedDescriptorReturnsEmpty() {
        MinecraftDescriptorFactory factory = new MinecraftDescriptorFactory();

        Optional<TransferableValueDescriptor> descriptor = factory.create(value(Map.of(
                MinecraftDescriptorFactory.QUANTITY, 4)), "minecraft");

        assertTrue(descriptor.isEmpty());
    }

    @Test
    void validActorResolvesContainer() {
        List<ItemStack> slots = List.of(new ItemStack(Items.STONE, 1));
        MinecraftContainerResolver resolver =
                new MinecraftContainerResolver((playerId, region) -> Optional.of(slots));

        Optional<TransferableValueContainer> container = resolver.resolve(actor(
                PLAYER_ACTOR,
                MinecraftContainerResolver.PLAYER_INVENTORY_KIND), null, "minecraft");

        MinecraftPlayerInventoryContainer minecraftContainer =
                assertInstanceOf(MinecraftPlayerInventoryContainer.class, container.orElseThrow());
        assertEquals(PLAYER_ACTOR.value(), minecraftContainer.containerId());
        assertEquals(MinecraftPlayerInventoryContainer.Region.MAIN, minecraftContainer.region());
    }

    @Test
    void unsupportedActorReturnsEmpty() {
        MinecraftContainerResolver resolver =
                new MinecraftContainerResolver((playerId, region) -> Optional.of(List.of(ItemStack.EMPTY)));

        Optional<TransferableValueContainer> container = resolver.resolve(actor(
                PLAYER_ACTOR,
                "minecraft.block.inventory"), null, "minecraft");

        assertTrue(container.isEmpty());
    }

    @Test
    void descriptorSetConvertsRuntimeEntries() {
        MinecraftRuntimeValueSetResolver resolver = new MinecraftRuntimeValueSetResolver();
        TransferableValueRuntimeValueEntry first = runtimeEntry("value-1", 2, Map.of(
                MinecraftDescriptorFactory.ITEM_ID, "minecraft:stone"));
        TransferableValueRuntimeValueEntry second = runtimeEntry("value-2", 3, Map.of(
                MinecraftDescriptorFactory.ITEM_ID, "minecraft:dirt",
                MinecraftDescriptorFactory.NBT_PAYLOAD, "{custom:1b}"));

        Optional<TransferableValueSet> valueSet =
                resolver.resolve(List.of(first, second), "minecraft", Map.of(), null);

        assertTrue(valueSet.isPresent());
        assertEquals(2, valueSet.get().values().size());
        MinecraftItemDescriptor firstDescriptor =
                assertInstanceOf(MinecraftItemDescriptor.class, valueSet.get().values().get(0));
        MinecraftItemDescriptor secondDescriptor =
                assertInstanceOf(MinecraftItemDescriptor.class, valueSet.get().values().get(1));
        assertEquals("minecraft:stone", firstDescriptor.itemId());
        assertEquals(2, firstDescriptor.quantity());
        assertEquals("minecraft:dirt", secondDescriptor.itemId());
        assertEquals("{custom:1b}", secondDescriptor.nbtPayload().orElseThrow());
    }

    @Test
    void collaboratorsDoNotMutateOrInterpretBeyondBindingScope() {
        MinecraftDescriptorFactory descriptorFactory = new MinecraftDescriptorFactory();
        Optional<TransferableValueDescriptor> descriptor = descriptorFactory.create(value(Map.of(
                MinecraftDescriptorFactory.ITEM_ID, "minecraft:stone",
                MinecraftDescriptorFactory.QUANTITY, 1,
                MinecraftDescriptorFactory.NBT_PAYLOAD, "{invalid:")), "minecraft");
        assertTrue(descriptor.isPresent());

        ItemStack stack = new ItemStack(Items.STONE, 5);
        MinecraftContainerResolver containerResolver =
                new MinecraftContainerResolver((playerId, region) -> Optional.of(List.of(stack)));
        Optional<TransferableValueContainer> container = containerResolver.resolve(actor(
                PLAYER_ACTOR,
                MinecraftContainerResolver.PLAYER_INVENTORY_KIND), null, "minecraft");

        MinecraftRuntimePayloadInterpreter interpreter = new MinecraftRuntimePayloadInterpreter();
        assertTrue(interpreter.interpret("minecraft", Map.of(MinecraftRuntimePayloadFactory.BINDING_ID, "minecraft"), null).isPresent());
        assertFalse(interpreter.interpret("minecraft", Map.of(MinecraftRuntimePayloadFactory.BINDING_ID, "other"), null).isPresent());
        assertTrue(container.isPresent());
        assertEquals(5, stack.getCount());
    }

    private static ValueDeclaration value(Map<String, Object> descriptor) {
        return new ValueDeclaration(
                new ValueRef("value-1"),
                AUTHORITY,
                new OpaqueObject(descriptor));
    }

    private static ActorDeclaration actor(ActorRef actorRef, String kind) {
        return new ActorDeclaration(actorRef, kind, new OpaqueObject(Map.of()));
    }

    private static TransferableValueRuntimeValueEntry runtimeEntry(
            String valueRef,
            long quantity,
            Map<String, Object> runtimeDescriptor) {
        return new TransferableValueRuntimeValueEntry(
                new ValueRef(valueRef),
                quantity,
                runtimeDescriptor);
    }
}

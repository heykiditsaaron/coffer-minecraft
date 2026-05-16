package dev.coffer.minecraft.bindings.inventory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.Bootstrap;
import net.minecraft.SharedConstants;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.StringNbtReader;
import org.coffer.core.arbitration.ArbitrationResult;
import org.coffer.core.arbitration.CofferCore;
import org.coffer.core.authority.ResolutionResult;
import org.coffer.core.model.id.ActorRef;
import org.coffer.core.model.id.MutationPlanId;
import org.coffer.core.model.id.MutationRequirementRef;
import org.coffer.core.model.id.OfferRef;
import org.coffer.core.model.id.OutcomeId;
import org.coffer.core.model.id.PayloadId;
import org.coffer.core.model.id.ReasonId;
import org.coffer.core.model.id.TruthRef;
import org.coffer.core.model.id.ValueRef;
import org.coffer.core.model.outcome.Decision;
import org.coffer.core.model.request.ActorDeclaration;
import org.coffer.core.model.request.ExchangePayload;
import org.coffer.core.model.request.ValueDeclaration;
import org.coffer.core.model.support.OpaqueObject;
import org.coffer.firstparty.authority.transferablevalue.construction.TransferableValueAtomicSwapConstruction;
import org.coffer.firstparty.authority.transferablevalue.construction.TransferableValueAtomicSwapRefs;
import org.coffer.firstparty.authority.transferablevalue.construction.TransferableValueConstructionResult;
import org.coffer.firstparty.authority.transferablevalue.construction.TransferableValueConstructionResult.Success;
import org.coffer.firstparty.authority.transferablevalue.construction.TransferableValueExchangePayloadConstruction;
import org.coffer.firstparty.authority.transferablevalue.core.TransferableValueCoreAuthority;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class MinecraftSelectedExchangeCoreAuthorityTruthTest {
    private static final UUID FIRST_PLAYER_ID = UUID.fromString("00000000-0000-0000-0000-000000000931");
    private static final UUID SECOND_PLAYER_ID = UUID.fromString("00000000-0000-0000-0000-000000000932");

    @BeforeAll
    static void bootstrapMinecraftRegistries() {
        SharedConstants.createGameVersion();
        Bootstrap.initialize();
    }

    @Test
    void equivalentValueElsewhereInHotbarApprovesCoreArbitration() throws CommandSyntaxException {
        List<ItemStack> firstHotbar = mutableSlots(
                ItemStack.EMPTY,
                new ItemStack(Items.DIRT, 1),
                ItemStack.EMPTY,
                ItemStack.EMPTY,
                stackWithNbt(Items.IRON_SWORD, "{Enchantments:[{id:\"minecraft:sharpness\",lvl:3s}],Damage:5}"),
                ItemStack.EMPTY,
                ItemStack.EMPTY,
                ItemStack.EMPTY,
                ItemStack.EMPTY);
        List<ItemStack> secondHotbar = mutableSlots(
                ItemStack.EMPTY,
                ItemStack.EMPTY,
                new ItemStack(Items.DIRT, 2),
                ItemStack.EMPTY,
                ItemStack.EMPTY,
                ItemStack.EMPTY,
                ItemStack.EMPTY,
                ItemStack.EMPTY,
                ItemStack.EMPTY);

        ArbitrationResult arbitration = arbitrate(
                exchangePayload(
                        value("selected-first-value", "minecraft:iron_sword", 1,
                                "{Enchantments:[{id:\"minecraft:sharpness\",lvl:3s}],Damage:5}"),
                        value("selected-second-value", "minecraft:dirt", 2, null)),
                resolver(firstHotbar, secondHotbar));

        assertEquals(Decision.APPROVED, arbitration.outcome().decision());
        assertNotNull(arbitration.mutationPlan());

        @SuppressWarnings("unchecked")
        Map<String, Object> descriptor =
                new LinkedHashMap<>(arbitration.mutationPlan().mutations().get(0).descriptor().values());
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> firstValueSet = (List<Map<String, Object>>) descriptor.get("firstValueSet");
        @SuppressWarnings("unchecked")
        Map<String, Object> firstRuntimeDescriptor =
                new LinkedHashMap<>((Map<String, Object>) firstValueSet.get(0).get("runtimeDescriptor"));

        assertEquals("minecraft:iron_sword", firstRuntimeDescriptor.get("itemId"));
        assertEquals(
                "{Enchantments:[{id:\"minecraft:sharpness\",lvl:3s}],Damage:5}",
                firstRuntimeDescriptor.get("nbtPayload"));
        assertEquals(1, firstHotbar.get(4).getCount());
        assertEquals(2, secondHotbar.get(2).getCount());
    }

    @Test
    void missingEquivalentOwnedValueDeniesCoreArbitration() {
        List<ItemStack> firstHotbar = mutableSlots(
                ItemStack.EMPTY,
                ItemStack.EMPTY,
                ItemStack.EMPTY,
                ItemStack.EMPTY,
                new ItemStack(Items.DIRT, 1),
                ItemStack.EMPTY,
                ItemStack.EMPTY,
                ItemStack.EMPTY,
                ItemStack.EMPTY);
        List<ItemStack> secondHotbar = mutableSlots(
                new ItemStack(Items.SHIELD, 1),
                ItemStack.EMPTY,
                ItemStack.EMPTY,
                ItemStack.EMPTY,
                ItemStack.EMPTY,
                ItemStack.EMPTY,
                ItemStack.EMPTY,
                ItemStack.EMPTY,
                ItemStack.EMPTY);

        ArbitrationResult arbitration = arbitrate(
                exchangePayload(
                        value("selected-first-value", "minecraft:iron_sword", 1,
                                "{Enchantments:[{id:\"minecraft:sharpness\",lvl:3s}],Damage:5}"),
                        value("selected-second-value", "minecraft:shield", 1, null)),
                resolver(firstHotbar, secondHotbar));

        assertEquals(Decision.DENIED, arbitration.outcome().decision());
        assertNull(arbitration.mutationPlan());
        assertEquals(
                MinecraftPlayerInventoryContainer.VALUE_NOT_REMOVABLE,
                arbitration.outcome().reasons().get(0).detail().values().get("reasonCode"));
        assertEquals(1, firstHotbar.get(4).getCount());
        assertEquals(1, secondHotbar.get(0).getCount());
    }

    @Test
    void counterpartyCannotReceiveDeniesCoreArbitration() {
        List<ItemStack> firstHotbar = mutableSlots(
                new ItemStack(Items.STONE, 3),
                ItemStack.EMPTY,
                ItemStack.EMPTY,
                ItemStack.EMPTY,
                ItemStack.EMPTY,
                ItemStack.EMPTY,
                ItemStack.EMPTY,
                ItemStack.EMPTY,
                ItemStack.EMPTY);
        List<ItemStack> secondHotbar = mutableSlots(
                new ItemStack(Items.DIRT, 64),
                new ItemStack(Items.DIRT, 64),
                new ItemStack(Items.DIRT, 64),
                new ItemStack(Items.DIRT, 64),
                new ItemStack(Items.DIRT, 64),
                new ItemStack(Items.DIRT, 64),
                new ItemStack(Items.DIRT, 64),
                new ItemStack(Items.DIRT, 64),
                new ItemStack(Items.DIRT, 1));

        ArbitrationResult arbitration = arbitrate(
                exchangePayload(
                        value("selected-first-value", "minecraft:stone", 3, null),
                        value("selected-second-value", "minecraft:dirt", 1, null)),
                resolver(firstHotbar, secondHotbar));

        assertEquals(Decision.DENIED, arbitration.outcome().decision());
        assertNull(arbitration.mutationPlan());
        assertEquals(
                MinecraftPlayerInventoryContainer.VALUE_NOT_RECEIVABLE,
                arbitration.outcome().reasons().get(0).detail().values().get("reasonCode"));
        assertEquals(3, firstHotbar.get(0).getCount());
        assertEquals(1, secondHotbar.get(8).getCount());
    }

    private static ArbitrationResult arbitrate(
            ExchangePayload payload,
            MinecraftContainerResolver resolver) {
        TransferableValueCoreAuthority coreAuthority = new TransferableValueCoreAuthority(
                resolver,
                new MinecraftDescriptorFactory(),
                new MinecraftRuntimePayloadFactory());
        return CofferCore.arbitrate(
                payload,
                ignored -> new ResolutionResult.Resolved(coreAuthority),
                new OutcomeId("selected-authority-outcome-1"),
                new MutationPlanId("selected-authority-mutation-plan-1"),
                denialReasonIds());
    }

    private static MinecraftContainerResolver resolver(List<ItemStack> firstHotbar, List<ItemStack> secondHotbar) {
        return new MinecraftContainerResolver((playerId, region) -> {
            if (region != MinecraftPlayerInventoryContainer.Region.HOTBAR) {
                return Optional.empty();
            }
            if (FIRST_PLAYER_ID.equals(playerId)) {
                return Optional.of(firstHotbar);
            }
            if (SECOND_PLAYER_ID.equals(playerId)) {
                return Optional.of(secondHotbar);
            }
            return Optional.empty();
        });
    }

    private static ExchangePayload exchangePayload(ValueDeclaration firstValue, ValueDeclaration secondValue) {
        List<ValueDeclaration> firstValues = List.of(firstValue);
        List<ValueDeclaration> secondValues = List.of(secondValue);
        TransferableValueConstructionResult construction =
                TransferableValueExchangePayloadConstruction.constructAtomicSwap(
                        new TransferableValueAtomicSwapConstruction(
                                new TransferableValueAtomicSwapRefs(
                                        new PayloadId("selected-authority-payload-1"),
                                        truthRefs(firstValues, "selected-authority-first-truth-"),
                                        truthRefs(secondValues, "selected-authority-second-truth-"),
                                        new TruthRef("selected-authority-first-can-receive"),
                                        new TruthRef("selected-authority-second-can-receive"),
                                        new MutationRequirementRef("selected-authority-mutation-requirement-1")),
                                actor(FIRST_PLAYER_ID),
                                actor(SECOND_PLAYER_ID),
                                new OfferRef("selected-authority-offer-1"),
                                new OfferRef("selected-authority-offer-2"),
                                firstValues,
                                secondValues,
                                "minecraft-inventory"));
        Success success = (Success) construction;
        return success.payload();
    }

    private static Map<ValueRef, TruthRef> truthRefs(List<ValueDeclaration> values, String prefix) {
        Map<ValueRef, TruthRef> truthRefs = new LinkedHashMap<>();
        for (ValueDeclaration value : values) {
            truthRefs.put(value.valueRef(), new TruthRef(prefix + value.valueRef().value()));
        }
        return Map.copyOf(truthRefs);
    }

    private static ActorDeclaration actor(UUID playerId) {
        return new ActorDeclaration(
                new ActorRef("player:" + playerId + ":inventory:hotbar"),
                MinecraftContainerResolver.PLAYER_INVENTORY_KIND,
                new OpaqueObject(Map.of()));
    }

    private static ValueDeclaration value(String valueRef, String itemId, long quantity, String nbtPayload) {
        Map<String, Object> descriptor = new LinkedHashMap<>();
        descriptor.put(MinecraftDescriptorFactory.ITEM_ID, itemId);
        descriptor.put(MinecraftDescriptorFactory.QUANTITY, quantity);
        if (nbtPayload != null) {
            descriptor.put(MinecraftDescriptorFactory.NBT_PAYLOAD, nbtPayload);
        }
        return new ValueDeclaration(
                new ValueRef(valueRef),
                TransferableValueCoreAuthority.AUTHORITY_ID,
                new OpaqueObject(Map.copyOf(descriptor)));
    }

    private static List<ItemStack> mutableSlots(ItemStack... slots) {
        return new ArrayList<>(List.of(slots));
    }

    private static ItemStack stackWithNbt(net.minecraft.item.ItemConvertible item, String nbt)
            throws CommandSyntaxException {
        ItemStack stack = new ItemStack(item, 1);
        stack.setNbt(StringNbtReader.parse(nbt));
        return stack;
    }

    private static List<ReasonId> denialReasonIds() {
        List<ReasonId> reasonIds = new ArrayList<>();
        for (int index = 0; index < 8; index++) {
            reasonIds.add(new ReasonId("selected-authority-reason-" + index));
        }
        return List.copyOf(reasonIds);
    }
}

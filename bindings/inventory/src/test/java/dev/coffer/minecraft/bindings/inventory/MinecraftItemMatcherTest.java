package dev.coffer.minecraft.bindings.inventory;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.Optional;
import net.minecraft.Bootstrap;
import net.minecraft.SharedConstants;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.StringNbtReader;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class MinecraftItemMatcherTest {
    @BeforeAll
    static void bootstrapMinecraftRegistries() {
        SharedConstants.createGameVersion();
        Bootstrap.initialize();
    }

    @Test
    void matchingItemWithoutNbtMatchesDescriptorWithoutNbt() {
        ItemStack stack = new ItemStack(Items.STONE, 1);
        MinecraftItemDescriptor descriptor =
                new MinecraftItemDescriptor("minecraft:stone", 1, Optional.empty());

        assertTrue(MinecraftItemMatcher.matches(stack, descriptor));
    }

    @Test
    void differentItemDoesNotMatch() {
        ItemStack stack = new ItemStack(Items.DIRT, 1);
        MinecraftItemDescriptor descriptor =
                new MinecraftItemDescriptor("minecraft:stone", 1, Optional.empty());

        assertFalse(MinecraftItemMatcher.matches(stack, descriptor));
    }

    @Test
    void emptyStackDoesNotMatch() {
        MinecraftItemDescriptor descriptor =
                new MinecraftItemDescriptor("minecraft:stone", 1, Optional.empty());

        assertFalse(MinecraftItemMatcher.matches(ItemStack.EMPTY, descriptor));
    }

    @Test
    void descriptorWithoutNbtDoesNotMatchStackWithNbt() throws CommandSyntaxException {
        ItemStack stack = stackWithNbt(Items.STONE, "{custom:1b}");
        MinecraftItemDescriptor descriptor =
                new MinecraftItemDescriptor("minecraft:stone", 1, Optional.empty());

        assertFalse(MinecraftItemMatcher.matches(stack, descriptor));
    }

    @Test
    void descriptorWithNbtDoesNotMatchStackWithoutNbt() {
        ItemStack stack = new ItemStack(Items.STONE, 1);
        MinecraftItemDescriptor descriptor =
                new MinecraftItemDescriptor("minecraft:stone", 1, Optional.of("{custom:1b}"));

        assertFalse(MinecraftItemMatcher.matches(stack, descriptor));
    }

    @Test
    void descriptorWithExactNbtMatchesStackWithSameNbt() throws CommandSyntaxException {
        String nbt = "{display:{Name:'{\"text\":\"Exact\"}'},custom:1b}";
        ItemStack stack = stackWithNbt(Items.STONE, nbt);
        MinecraftItemDescriptor descriptor =
                new MinecraftItemDescriptor("minecraft:stone", 1, Optional.of(nbt));

        assertTrue(MinecraftItemMatcher.matches(stack, descriptor));
    }

    @Test
    void descriptorWithDifferentNbtDoesNotMatch() throws CommandSyntaxException {
        ItemStack stack = stackWithNbt(Items.DIAMOND_SWORD, "{custom:1b}");
        MinecraftItemDescriptor descriptor =
                new MinecraftItemDescriptor("minecraft:diamond_sword", 1, Optional.of("{custom:2b}"));

        assertFalse(MinecraftItemMatcher.matches(stack, descriptor));
    }

    @Test
    void quantityDifferenceDoesNotAffectIdentityMatch() {
        ItemStack stack = new ItemStack(Items.STONE, 64);
        MinecraftItemDescriptor descriptor =
                new MinecraftItemDescriptor("minecraft:stone", 1, Optional.empty());

        assertTrue(MinecraftItemMatcher.matches(stack, descriptor));
    }

    @Test
    void invalidDescriptorNbtFailsSafely() throws CommandSyntaxException {
        ItemStack stack = stackWithNbt(Items.STONE, "{custom:1b}");
        MinecraftItemDescriptor descriptor =
                new MinecraftItemDescriptor("minecraft:stone", 1, Optional.of("{custom:"));

        assertFalse(MinecraftItemMatcher.matches(stack, descriptor));
    }

    private static ItemStack stackWithNbt(net.minecraft.item.ItemConvertible item, String nbt)
            throws CommandSyntaxException {
        ItemStack stack = new ItemStack(item, 1);
        stack.setNbt(StringNbtReader.parse(nbt));
        return stack;
    }
}

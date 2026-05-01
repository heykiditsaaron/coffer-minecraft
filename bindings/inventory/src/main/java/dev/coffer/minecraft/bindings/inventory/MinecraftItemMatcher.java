package dev.coffer.minecraft.bindings.inventory;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.Objects;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.StringNbtReader;
import net.minecraft.registry.Registries;

public final class MinecraftItemMatcher {
    private MinecraftItemMatcher() {
    }

    public static boolean matches(ItemStack stack, MinecraftItemDescriptor descriptor) {
        Objects.requireNonNull(stack, "stack");
        Objects.requireNonNull(descriptor, "descriptor");

        if (stack.isEmpty()) {
            return false;
        }
        if (!Registries.ITEM.getId(stack.getItem()).toString().equals(descriptor.itemId())) {
            return false;
        }

        boolean stackHasNbt = stack.hasNbt();
        boolean descriptorHasNbt = descriptor.nbtPayload().isPresent();
        if (stackHasNbt != descriptorHasNbt) {
            return false;
        }
        if (!descriptorHasNbt) {
            return true;
        }

        NbtCompound descriptorNbt;
        try {
            descriptorNbt = StringNbtReader.parse(descriptor.nbtPayload().orElseThrow());
        } catch (CommandSyntaxException exception) {
            return false;
        }

        return descriptorNbt.equals(stack.getNbt());
    }
}

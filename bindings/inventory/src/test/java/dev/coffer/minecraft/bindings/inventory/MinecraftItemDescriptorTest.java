package dev.coffer.minecraft.bindings.inventory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class MinecraftItemDescriptorTest {
    @Test
    void validDescriptorExposesQuantity() {
        MinecraftItemDescriptor descriptor =
                new MinecraftItemDescriptor("minecraft:diamond", 3, Optional.empty());

        assertEquals(3, descriptor.quantity());
    }

    @Test
    void rejectsBlankItemId() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new MinecraftItemDescriptor(" ", 1, Optional.empty()));
    }

    @Test
    void rejectsNonPositiveQuantity() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new MinecraftItemDescriptor("minecraft:diamond", 0, Optional.empty()));
    }

    @Test
    void preservesFullNbtPayloadExactly() {
        String nbt = "{display:{Name:'{\"text\":\"Exact Name\"}'},CustomModelData:7}";

        MinecraftItemDescriptor descriptor =
                new MinecraftItemDescriptor("minecraft:diamond_sword", 1, Optional.of(nbt));

        assertEquals(nbt, descriptor.nbtPayload().orElseThrow());
    }

    @Test
    void allowsAbsentNbtPayload() {
        MinecraftItemDescriptor descriptor =
                new MinecraftItemDescriptor("minecraft:stone", 64, Optional.empty());

        assertTrue(descriptor.nbtPayload().isEmpty());
    }

    @Test
    void rejectsBlankNbtPayloadIfPresent() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new MinecraftItemDescriptor("minecraft:stone", 1, Optional.of(" ")));
    }

    @Test
    void descriptorContainsNoDisplayTextFieldOrBehavior() {
        assertFalse(declaredFieldOrMethodContains("display"));
        assertFalse(declaredFieldOrMethodContains("lore"));
        assertFalse(declaredFieldOrMethodContains("text"));
    }

    private static boolean declaredFieldOrMethodContains(String token) {
        return Arrays.stream(MinecraftItemDescriptor.class.getDeclaredFields())
                        .map(Field::getName)
                        .anyMatch(name -> name.toLowerCase().contains(token))
                || Arrays.stream(MinecraftItemDescriptor.class.getDeclaredMethods())
                        .map(Method::getName)
                        .anyMatch(name -> name.toLowerCase().contains(token));
    }
}

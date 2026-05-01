# Minecraft Item Matcher Implemented

## Summary

`MinecraftItemMatcher` was added for inventory binding item identity checks.

The matcher compares a Minecraft `ItemStack` against `MinecraftItemDescriptor` using:

- Non-empty stack requirement.
- Exact item registry id equality.
- Matching NBT absence or presence.
- Full NBT equality when descriptor NBT is present.

## Identity Rules

Quantity is excluded from identity matching.

Display text is not inspected or treated as truth.

No partial NBT comparison, normalization, or fuzzy matching is performed.

## Invalid NBT

Descriptor NBT is parsed only when needed for comparison. If descriptor NBT cannot be parsed, matching fails safely.

## Verification

`gradle :bindings:inventory:test` succeeded.

Tests cover no-NBT match, different item mismatch, empty stack mismatch, NBT absence/presence mismatches, exact NBT match, different NBT mismatch, quantity independence, and invalid descriptor NBT safe failure.

## Test Runtime Notes

Real Minecraft `ItemStack` tests require Minecraft bootstrap setup. The test runtime now sets the game version and initializes bootstrap before using `Items` or `ItemStack`.

A minimal access widener and test-only `-Xverify:none` JVM argument were added to run the mapped Minecraft `1.20.1` test runtime under the current Java 17 environment.

## Next

Container implementation remains next.

## Uncertainties

- The access widener and verification flag may need revisiting if the project moves to Java 21 or a newer Loom line.
- Damageable or otherwise stateful items may add native NBT beyond explicit test payloads.
- Future component-based Minecraft versions may require a different descriptor comparison strategy.

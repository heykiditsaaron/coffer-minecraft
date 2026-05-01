# Minecraft Item Descriptor Mapping - Initial Definition

This is a first-pass definition for representing Minecraft `ItemStack` identity in TransferableValueAuthority descriptors.

## 1. Purpose

The descriptor maps Minecraft `ItemStack` identity and quantity into a binding-owned payload that TransferableValueAuthority can carry through authority workflows without interpreting Minecraft-specific data.

## 2. Descriptor Contents

A descriptor should carry:

- Item identifier.
- Quantity.
- Full NBT payload when present.

Display text, including item name, lore, and formatted labels, is not truth for descriptor identity.

## 3. Equivalence Rule

v1 equivalence uses strict item identity plus full NBT equality.

- No partial matching.
- No display-name-only matching.
- Quantity is required for value amount but is not part of item identity.

## 4. Serialization Expectations

The descriptor must be deterministic enough for audit and runtime reconstruction.

NBT must be preserved without splitting, normalizing, or lossy parsing. The descriptor shape may later become schema-versioned if compatibility or migration needs require it.

## 5. Binding Responsibility

The Minecraft binding interprets descriptor bodies.

TransferableValueAuthority domain behavior sees only the quantity and carries the descriptor as opaque binding data. Core does not interpret descriptor bodies.

## 6. Explicit Non-Goals

- No valuation.
- No fuzzy matching.
- No cross-item substitution.
- No player-facing formatting contract.

## 7. Uncertainties

- Final serialized descriptor format.
- Minecraft version differences around NBT and components.
- Whether future versions need normalized or component-based descriptors.

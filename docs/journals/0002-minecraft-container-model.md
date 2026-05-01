# Minecraft Container Model - Initial Definition

This is a first-pass definition for Minecraft inventory binding work. It records the current model direction, not final truth.

## 1. Scope Decision

Initially supported containers:

- Player main inventory.
- Player hotbar.
- Player armor slots.
- Player offhand slot.

Explicitly deferred:

- Chests, barrels, hoppers, furnaces, and other block inventories.
- Villager, merchant, anvil, crafting, enchanting, brewing, and other screen-specific containers.
- Cross-container workflows.
- Player trade gameplay flows.
- Loader-specific integration.

## 2. Container Identity Model

A container is uniquely identified by a stable Minecraft container kind plus an owner identity when applicable.

For the initial player-owned scope, identity is expected to be:

- Container kind.
- Player identity.
- Logical inventory region when needed, such as main, hotbar, armor, or offhand.

Canonical string format:

```text
player:<uuid>:inventory:<region>
```

Example regions: `main`, `hotbar`, `armor`, and `offhand`.

## 3. Slot vs Aggregate Model

The binding should retain slot-level awareness internally because Minecraft inventory mutation depends on concrete slots.

External authority interactions should expose only the level of slot detail required by the confirmed TransferableValueAuthority ports. Slot control should not become gameplay policy unless the authority contract requires it.

## 4. Item Identity Model

Initial item equivalence is based on native Minecraft item identity plus full NBT equality.

Expected equivalence inputs:

- Item type.
- Full NBT payload.

Pure stack count is not item identity.

There is no partial item matching in v1. If two stacks differ by NBT, they are not equivalent.

## 5. Stack Behavior

Minecraft stack rules are handled by the binding as native container constraints.

The binding is responsible for respecting:

- Maximum stack sizes.
- Slot acceptance rules.
- Existing partial stacks.
- Empty slot availability.
- Item-specific stackability.

Minecraft enforces these stack rules. The binding observes and reports them; it does not override native behavior.

## 6. Simulation vs Application

Simulation is expected to be non-mutating and should answer whether the requested transfer can be applied under current container state.

Simulation should be computed from observed inventory state, not by cloning or mutating a temporary inventory.

Application is expected to mutate only after simulation-compatible feasibility is established. If state changes between simulation and application, application must report the actual outcome rather than assuming the prior simulation remains valid.

## 7. Failure vs Unknown

Known Minecraft constraints should produce specific failure reasons when the binding can determine them.

Examples of `FAILED`:

- Requested item is not present.
- Destination has no space.
- Slot rejects the item.
- Requested quantity exceeds available quantity.
- Item NBT does not match the descriptor.

`UNKNOWN` should be reserved for cases where the binding cannot reliably classify the result, such as:

- Unsupported container type.
- Unrecognized item state required for equivalence.
- Missing or inconsistent container identity.
- Authority port contract gaps not yet confirmed.

`UNKNOWN` should not be used as a catch-all for ordinary inventory full, missing item, slot rejection, or non-stackable cases once those cases are modeled.

Examples of `UNKNOWN`:

- Container identity cannot be resolved.
- Descriptor shape is not understood by the binding.
- Inventory state cannot be read reliably.
- A required authority contract detail is unavailable.

## 8. Descriptor Expectations

A descriptor must carry:

- Item type.
- Quantity.
- Full NBT payload.

The binding interprets Minecraft item descriptors. The platform-neutral domain does not interpret Minecraft item type strings or NBT.

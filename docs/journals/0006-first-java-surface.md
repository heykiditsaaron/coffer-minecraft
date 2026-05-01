# First Java Surface

This note defines the minimal first Java surface for the Minecraft inventory binding. It is design only.

## Confirmed Authority Ports

From `../coffer`, the binding must implement:

- `TransferableValueDescriptor`, currently exposing `long quantity()`.
- `TransferableValueContainer`, exposing `canRemove`, `canReceive`, `simulateAtomicSwap`, and `applyAtomicSwap`.

The authority result types accept binding-owned reason-code strings through `Failed(reasonCode)` and `Unknown(reasonCode)`.

## Proposed Java Types

### `MinecraftItemDescriptor`

Concrete `TransferableValueDescriptor` implementation.

Binding-owned fields:

- `itemId`: canonical Minecraft item identifier.
- `quantity`: positive transfer quantity exposed through `quantity()`.
- `nbtPayload`: full serialized NBT payload when present, absent when the stack has no NBT.

Rules:

- Quantity is required for transfer amount but is not item identity.
- Identity is `itemId` plus full NBT equality.
- No partial matching and no display-name-only matching.
- The descriptor body is interpreted only by the Minecraft binding.

### `MinecraftPlayerInventoryContainer`

Concrete `TransferableValueContainer` implementation for player inventory-only v1.

Binding-owned fields:

- `containerId`: canonical `player:<uuid>:inventory:<region>`.
- `region`: `main`, `hotbar`, `armor`, or `offhand`.
- Inventory access abstraction for reading slot state and applying slot mutations.

Responsibilities:

- Retain slot-level awareness internally.
- Implement removability by summing matching stacks across allowed slots.
- Implement receivability by checking native stack limits, existing partial stacks, empty slots, and slot acceptance.
- Compute simulation from observed state without cloning or mutating inventory.
- Apply atomic swap only after current-state checks pass.
- Mutate only on success and report actual outcome if state changed after simulation.

## Reason Codes

Create stable binding-owned reason-code constants before behavior grows.

Initial `FAILED` candidates:

- `minecraft.item.not_present`
- `minecraft.item.insufficient_quantity`
- `minecraft.item.nbt_mismatch`
- `minecraft.container.no_space`
- `minecraft.slot.rejects_item`
- `minecraft.swap.precondition_failed`

Initial `UNKNOWN` candidates:

- `minecraft.container.unresolved`
- `minecraft.container.unsupported`
- `minecraft.descriptor.unsupported`
- `minecraft.inventory.unreadable`
- `minecraft.authority.contract_gap`

## Boundaries

- No Fabric loader glue yet.
- No player-trade adapter.
- No block inventories.
- No Core, Runtime, or TransferableValueAuthority wrapper changes.
- No valuation, fuzzy matching, or cross-item substitution.

## Tests To Add First

- Descriptor preserves item id and full NBT payload.
- Named item does not match otherwise-identical unnamed item.
- Insufficient quantity fails.
- Full inventory cannot receive.
- Atomic swap simulation does not mutate.
- Atomic swap application mutates only on success.

## Uncertainties

- Exact package names for the binding classes.
- Exact NBT serialization type: string, byte array, or structured opaque payload.
- Whether Minecraft version differences require component-aware descriptors later.
- Shape of the inventory access abstraction before Minecraft dependencies are introduced.
- Whether reason-code naming should later align with a wider Coffer naming convention.

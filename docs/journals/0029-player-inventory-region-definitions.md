# Player Inventory Region Definitions

## Decision

The v1 player inventory container regions are:

- `main`
- `hotbar`
- `armor`
- `offhand`

No combined or `all` region is part of v1.

## Slot Membership

`main` contains the player's non-hotbar main inventory slots.

`hotbar` contains the player's hotbar slots.

`armor` contains the player's armor slots.

`offhand` contains the player's offhand slots.

`main` explicitly excludes hotbar slots. A request for `player:<uuid>:inventory:main` must not implicitly include `player:<uuid>:inventory:hotbar`.

## Default Region Policy

Adapters should require an explicit inventory region in v1.

There is no default region for a generic player inventory request. If future adapter APIs accept a player inventory without a region, they should reject it or require the caller to choose a region before constructing the Coffer actor reference.

This avoids hidden scope expansion and prevents adapter code from accidentally treating a partial inventory as the whole inventory.

## Reasoning

Minecraft player inventory storage has distinct gameplay semantics even when the backing data lives near the same player inventory object. Hotbar slots are active-use slots, armor slots have equipment semantics, offhand has separate interaction semantics, and non-hotbar main inventory slots are ordinary carried storage.

Keeping regions separate preserves explicit authority scope:

- descriptors continue to identify values, not gameplay slot policy
- runtime payloads do not need hidden expansion rules
- authority checks operate over a clearly bounded container
- future adapters cannot accidentally remove or receive values from slots they did not name

This matters for descriptor and runtime safety because Coffer mutation plans carry actor/container references through Core approval and Runtime execution. If a region name could secretly expand from `main` to `main + hotbar`, Core and Runtime could disagree with adapter intent without any descriptor change.

## Combined Regions

A combined region may be designed later if a real adapter requires whole-player carried inventory semantics.

If added, it should use an explicit region name rather than changing `main`. Candidate names include:

- `all`
- `carried`
- `storage`

No combined region is defined or implemented in v1.

## Non-Goals

- No adapter behavior.
- No UI or command behavior.
- No implementation change.
- No slot-specific gameplay policy beyond region membership.
- No offline inventory loading.

## Uncertainties

- Minecraft/Yarn slot numbering assumptions should be rechecked when upgrading Minecraft versions.
- Future adapters may need a combined region, but that should be justified by a concrete workflow before implementation.
- Equipment slot ordering inside `armor` is delegated to Minecraft's current inventory list order unless a future adapter requires named armor subregions.

# Inventory Binding Contract

## Purpose

This document defines the current Minecraft-specific contract for
`bindings/inventory`.

The module owns Minecraft inventory binding semantics only. It does not own
TransferableValue authority law, Runtime orchestration, or gameplay policy.

## Descriptor Identity

Descriptor identity is Minecraft-item specific.

- `itemId` is required and must be a non-blank Minecraft item identifier.
- `quantity` is required when constructing a descriptor from a Core-side value
  declaration and must be a positive whole number.
- `nbtPayload` is optional and, when present, must be a non-blank serialized
  payload string.
- `bindingId` is not part of descriptor identity.

Two binding descriptors represent the same value kind only when they resolve to
the same item identity and the same NBT payload shape under the equivalence rule
below.

## Equivalence Rules

The binding uses exact equivalence, not fuzzy similarity.

- Item identifiers must match exactly.
- NBT payload presence must match.
- If NBT payload is present on both sides, the payload strings must match
  exactly.

The binding does not normalize, parse, or semantically compare NBT beyond this
exact payload equivalence rule.

## Quantity Semantics

Quantity is item-count quantity.

- Quantity must be positive at descriptor construction time.
- Runtime value-set reconstruction uses the Runtime-provided quantity together
  with the runtime descriptor fields.
- Quantity does not redefine stack-size law; native stackability and slot
  behavior remain Minecraft/container concerns.

## Container And Storage Boundaries

The current binding owns player inventory container interpretation only.

- Container kind is `minecraft.player.inventory`.
- Actor references are interpreted as `player:<uuid>:inventory:<region>`.
- The current regions are those exposed by
  `MinecraftPlayerInventoryContainer.Region`.
- Container semantics are bounded to the resolved slot list for that region.

The binding owns slot interpretation, extraction/deposit simulation, and
application for that container boundary. It does not own Fabric lifecycle,
thread scheduling, or gameplay workflow.

## Runtime Material Expectations

Runtime descriptor material must be sufficient to reconstruct Minecraft value
identity.

- Runtime descriptors are expected to carry `itemId`.
- Runtime descriptors may carry `nbtPayload`.
- Runtime value quantities are expected from Runtime entries, not from
  `runtimePayload`.

Runtime payload material currently carries cross-boundary compatibility metadata:

- runtime payload generation writes `bindingId`
- runtime payload interpretation accepts payloads with no `bindingId`
- runtime payload interpretation rejects payloads whose `bindingId` does not
  match the binding instance

`bindingId` is therefore payload-validation-only in the current binding.

## Binding-Owned Responsibilities

`bindings/inventory` owns:

- descriptor creation from Minecraft-specific declaration fields
- exact item/NBT matching
- container identity parsing for supported Minecraft inventory actors
- runtime descriptor reconstruction into binding descriptors
- simulation and application semantics for supported inventory containers
- Minecraft-specific reason codes produced inside those semantics

`bindings/inventory` does not own:

- Core arbitration or exchange law
- Runtime step orchestration
- TransferableValue authority contract definitions
- gameplay commands, UI, policy, or permissions
- Fabric-specific lifecycle or server-thread execution concerns

## Failure And Unknown Classification

The binding should classify outcomes explicitly at the Minecraft boundary.

- Return empty or absent resolution only when the binding cannot recognize or
  reconstruct the requested Minecraft material.
- Use binding-specific unknown classifications when the binding reaches the
  container boundary but native mutability/receivability cannot be established.
- Do not silently reinterpret malformed or mismatched runtime material into a
  different semantic result.

In particular, a mismatched runtime `bindingId` is treated as unrecognizable
runtime payload material, not as a partial execution hint.

## Core And Runtime Boundary Expectations

Core-facing declarations supply item identity and quantity through descriptor
values. Runtime-facing execution supplies:

- actor references for supported containers
- runtime value entries for post-arbitration value reconstruction
- runtime payload metadata for cross-boundary compatibility checks

The binding is responsible for translating between these Minecraft-specific
materials and the substrate TransferableValue ports. The substrate remains
authoritative for planning, arbitration, and execution sequencing.

## Binding-Id Direction

The current direction is to keep `bindingId` as payload-validation-only.

Reasons:

- it matches the current implementation
- it provides a cheap compatibility check at the Runtime payload boundary
- it does not incorrectly force descriptor identity or container semantics to
  depend on a second identifier path

Deferred alternatives:

- making `bindingId` execution-critical would require new routing or resolution
  semantics and stronger proof
- removing `bindingId` would require an explicit compatibility decision across
  Core and Runtime payload producers/consumers

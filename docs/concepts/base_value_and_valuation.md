# Base Value & Valuation — Conceptual Model

## PURPOSE

Base value exists to reduce administrative tedium.

Administrators should be able to:
- assign value broadly,
- avoid repetitive configuration,
- and refine only where necessary.

---

## TAG-BASED BASE VALUE

Minecraft’s native tag system enables:
- large-scale value declaration,
- minimal configuration effort,
- and consistent defaults.

Example:
- All `minecraft:logs` have a base value.

This is intentional.

---

## HIERARCHICAL OVERRIDES

More specific declarations may override broader ones:
- individual items,
- subsets,
- or admin-defined scopes.

Overrides are explicit.
Absence does not imply permission.

---

## POLICY, NOT BEHAVIOR

Valuation is evaluated through policy layers.
Policy may deny.
Policy never mutates.

If valuation cannot be determined honestly:
- the exchange is denied.

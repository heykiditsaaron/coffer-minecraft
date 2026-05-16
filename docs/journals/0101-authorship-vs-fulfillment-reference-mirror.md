# 0101 Authorship vs Fulfillment Reference Mirror

## Summary

Add a substrate-derived mirror of the authorship-surface vs lawful-fulfillment-
surface clarification into `coffer-minecraft` for repo-local reasoning
continuity.

This step does not redefine the distinction.
It does not alter Core Law, schemas, or Minecraft/Fabric behavior.

## Decision

Mirror the substrate clarification into `docs/architecture` as a local
reference document and keep the mirror explicitly non-canonical.

The source remains:

- `~/dev/coffer/docs/journals/0076-authorship-surfaces-and-lawful-fulfillment-surfaces-clarification.md`

The mirrored target is:

- `~/dev/coffer-minecraft/docs/architecture/authorship-surfaces-and-lawful-fulfillment-surfaces-reference.md`

## Rationale

`coffer-minecraft` already keeps several substrate-derived mirrors under
`docs/architecture` so repo-local reasoning can remain coherent even when
cross-repo access is absent or unreliable.

This distinction belongs in that same local continuity layer because current
Minecraft/Fabric work is already under pressure from:

- selected inventory authorship surfaces
- future modded inventory or storage-like pools
- participant-facing explanation and UX interpretation
- lawful fulfillment semantics at authority time

Without a local mirror, it is easier for Minecraft/Fabric reasoning to drift
in one of two directions:

- collapsing selected authorship context into fulfillment promises the
  substrate has not earned
- flattening authorship into fulfillment pools so aggressively that meaningful
  player intent context disappears too early

## Scope

Included:

- one substrate-derived architecture reference mirror
- one journal entry documenting why the mirror is intentional
- explicit non-canonical mirror labeling
- explicit source and target path recording

Excluded:

- Core Law changes
- schema changes
- gameplay or UX changes
- Fabric implementation behavior
- redefinition of the distinction

## Source And Target

Source path:

- `~/dev/coffer/docs/journals/0076-authorship-surfaces-and-lawful-fulfillment-surfaces-clarification.md`

Target path:

- `~/dev/coffer-minecraft/docs/architecture/authorship-surfaces-and-lawful-fulfillment-surfaces-reference.md`

## Why Duplication Is Intentional

This duplication is intentional because the target repository already maintains
repo-local substrate mirrors for high-pressure reasoning surfaces.

The mirror is not a second canon.
It is a continuity aid for:

- Minecraft/Fabric adaptation work
- future local journals
- local audits and proof-vessel reasoning
- agents or collaborators working without dependable cross-repo lookup

## What Must Not Diverge

The following must not diverge from substrate canon without a deliberate source
change first:

- authorship surfaces describe participant intent-expression context
- lawful fulfillment surfaces describe authority-facing lawful fulfillment
  pools
- selected-slot specificity may matter at authorship time without becoming an
  automatic exact-slot fulfillment promise
- Core/arbitration evaluates declared value plus explicit authority truth, not
  authorship mechanics directly
- the distinction is adaptation guidance, not grounds for Minecraft/Fabric
  redefinition

If future substrate canon changes, the mirror should be updated to match rather
than locally revised into a forked doctrine.

## Why This Matters Here

### Selected Inventory

Current Gate `#1` work depends on selected inventory reasoning.

This distinction matters because selected-slot or selected-surface identity may
be important at authorship time even when lawful fulfillment later occurs
against a broader attested pool.

### Future Modded Pools

Minecraft/Fabric work is likely to encounter future inventory-like or
storage-like modded surfaces that are not best modeled as exact-slot
fulfillment sites.

The mirrored distinction keeps room for those fulfillment pools without
discarding authorship context.

### UX

Future UX and explanation work may need to preserve what the participant meant
to offer, where they selected it from, and how stale or invalidation feedback
should be explained.

That is authorship pressure, not automatic fulfillment semantics.

### Fulfillment Semantics

Authority-facing lawful fulfillment must remain conservative and explicit.

This mirror helps keep Minecraft/Fabric reasoning aligned with the substrate:
selected authorship context does not by itself justify exact-slot fulfillment
guarantees, and broader fulfillment pools do not justify silent reinterpretation
of declared value.

## Verification

Verification for this step consists of:

- `git status --short`
- `git diff --check`

## Uncertainties

This mirror resolves local continuity, not future adaptation detail.

Still deferred:

- how selected authorship metadata should later be preserved across full
  accountability projection
- when a Minecraft/Fabric domain might need stricter fulfillment-site
  guarantees than the current substrate default
- how future modded pools should declare their lawful fulfillment surfaces in
  detail

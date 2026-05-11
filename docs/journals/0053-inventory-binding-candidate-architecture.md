# Summary

Documented the Minecraft inventory binding as current candidate architecture
rather than migration-only residue, and recorded the current `bindingId`
direction without changing runtime behavior.

# Decision

The repository now documents `bindings/inventory` as current candidate living
architecture for Minecraft-specific inventory semantics.

The current `bindingId` strategy is retained and documented as
payload-validation-only:

- runtime payload generation includes `bindingId`
- runtime payload interpretation accepts missing `bindingId`
- runtime payload interpretation rejects mismatched `bindingId`
- descriptor identity, container resolution, and runtime value reconstruction do
  not derive semantics from `bindingId`

# Rationale

Compile proof and passing inventory tests show that the binding is already doing
real semantic work: descriptor reconstruction, exact item/NBT equivalence,
container boundary interpretation, runtime payload interpretation, runtime
value-set reconstruction, and atomic swap simulation/application.

Treating that module as migration-only no longer matches the proven repository
state.

Keeping `bindingId` payload-validation-only is the narrowest correct decision
because it matches current behavior and avoids silently introducing new
execution-routing semantics or removing a compatibility check without explicit
cross-boundary proof.

# Scope

Included:

- repository and module documentation framing updates
- a new Minecraft-specific inventory binding contract document
- explicit documentation of current `bindingId` semantics
- a narrow test proving runtime payload interpreter behavior

Excluded:

- gameplay behavior
- new Fabric feature work
- fake inventory migration from `~/dev/coffer`
- substrate law or TransferableValue behavior changes
- any behavioral change to `bindingId`

# Verification

Planned verification for this step:

- `git diff --check`
- `./gradlew :bindings:inventory:test`

# Uncertainties

- Whether future cross-binding Runtime execution needs a stricter
  execution-critical binding discriminator remains unresolved.
- Whether `bindingId` can later be simplified away depends on explicit substrate
  boundary decisions and compatibility proof, not on current binding behavior
  alone.

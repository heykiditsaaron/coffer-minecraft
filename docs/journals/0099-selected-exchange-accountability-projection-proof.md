## Summary

Add a narrowly scoped proof that the selected exchange chain can project truthful accountability records across construction refusal, Core contact, and Runtime contact using the current Fabric lifecycle JSONL conventions.

This step does not prove the selected inventory snapshot -> lawful authority/Core handoff bridge from `0094`. It starts after the selected value has already been assembled into the selected-offer exchange path proven in later journals.

## Decision

Use the existing Fabric lifecycle accountability shape and extend it only enough to distinguish Runtime success and Runtime failure in addition to the already-proven Runtime unknown path.

Add an adapter-side selected-exchange accountability projection that maps:

- pre-Core assembly refusal to `SER` construction refusal
- Core denial to `CER` Core denial
- Core-approved Runtime participation to ordered `CER` Core approval followed by the earned Runtime outcome

Do not treat this as closure of the selected snapshot handoff gap. The selected snapshot -> authority/Core bridge remains whatever was proven or deferred in earlier Gate `#1` journals, and this entry only proves truthful accountability projection for a chain that already reaches Core and Runtime.

## Rationale

The repository already had the flat ordered record shape, record-type distinction, and seam gating needed for this proof. The missing bridge was not schema ownership or new exchange mechanics. It was a narrow adapter-side projection from the already-proven selected assembly and Runtime participation results into those existing accountability records.

This detour is still valuable because once selected-offer assembly, Core arbitration, and Runtime participation exist, the repo still needs truthful accountability records that do not counterfeit deeper participation than actually occurred. That accountability proof is useful independently of the unresolved question from `0094`, but it does not answer that question.

This keeps accountability truthful:

- pre-Core refusal does not counterfeit Core contact
- Core approval does not counterfeit Runtime execution
- Runtime failure does not counterfeit Core denial
- Runtime unknown remains unknown
- Runtime success appears only after Runtime participation is earned

## Scope

Included:

- Fabric lifecycle accountability support for `fabric_runtime_succeeded`
- Fabric lifecycle accountability support for `fabric_runtime_failed`
- selected-exchange accountability projection tests covering:
  - empty selected capture refusal before Core contact
  - Core denial without Runtime record
  - Core approval followed by Runtime success
  - Core approval followed by Runtime failure
  - Core approval followed by Runtime unknown

Excluded:

- proof that a raw selected snapshot can be lawfully handed to authority/Core without hidden widening
- receipt UX
- rollback tooling
- gameplay polish
- schema redesign
- release-readiness claims

## Verification

- `./gradlew :bindings:inventory:test :platforms:fabric:test :platforms:fabric:compileJava`
- `git diff --check`

## Uncertainties

The current Fabric lifecycle emitter continues to use one generated interaction id per appended record, which remains the current repo convention. This journal does not redesign that convention into a grouped exchange timeline.

Still unresolved relative to the original Gate `#1` bridge:

- whether the selected snapshot -> lawful authority/Core handoff was already fully proven by the selected-offer assembly/arbitration chain or still depends on assumptions outside `0094`
- whether any remaining handoff gap is best described as a binding-level identity bridge or as a narrower documentation mismatch between the earlier deferral and the later selected-offer proofs

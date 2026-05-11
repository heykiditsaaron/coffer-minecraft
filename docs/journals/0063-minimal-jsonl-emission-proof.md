# 0063 Minimal JSONL Emission Proof

## Summary

Applied the first append-only JSONL emission pressure to the ghost-adapter
accountability projection.

The proof stays test-only and uses temporary filesystem targets. It does not
introduce production logging infrastructure.

## Decision

Add a tiny ghost-adapter-local JSONL emitter that:

- writes one accountability record per line
- appends in call order
- emits only the current minimal field set
- writes to `logs/coffer/*.jsonl`-shaped paths

The emitter is intentionally narrow and should fail closed if unexpected fields
appear.

## Rationale

The projection shape had already survived semantic pressure. The next smallest
survivability step was to confirm that physical append-only serialization does
not force:

- schema growth
- placeholder participation sections
- contradictory identity emission
- unreadable field ordering

Stable field ordering matters here because the records are accountability
artifacts, not opaque telemetry blobs.

## Scope

Included:

- test-only append-only JSONL emission utility in ghost-adapter support
- proof of one record per line
- proof of append-only chronological emission
- proof of stable field ordering
- proof that omission survives serialization
- proof that canonical identity survives across emitted records
- proof that `UNKNOWN` survives serialization without structural expansion

Excluded:

- production log ownership
- deployment/configuration
- alternate sinks
- parsers or admin tooling
- final schema design

## Verification

Covered by tests:

- `SER` and `CER` records append as separate JSON lines
- successive emissions preserve chronological order
- omitted fields remain omitted after serialization
- `runtime_unknown` survives as the same tiny shape on disk
- path shaping stays under `logs/coffer`

## Uncertainties

This proof must not be read as:

- production logging readiness
- final JSONL filename strategy
- final escaping/formatting policy beyond this tiny record shape
- platform integration authority

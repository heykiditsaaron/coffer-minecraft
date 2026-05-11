# 0066 Minimal JSONL Raw Readability Pressure

## Summary

Applied raw left-to-right readability pressure to the minimal accountability
JSONL shape.

The current field order remained readable as:

- `interactionId`
- `recordType`
- `stage`
- optional `code`

without explanation fields or schema growth.

## Decision

Keep the current field-order discipline unchanged and continue treating raw
JSONL lines as compact accountability statements rather than explanatory
messages.

Do not add prose fields, null placeholders, metadata wrappers, or alternate
readability scaffolding in this step.

## Rationale

Before parsers or admin tooling exist, the raw JSONL file is the first human
inspection surface.

The pressure goal was to verify that each current line still reads coherently
left-to-right for:

- SER-only refusal
- CER denial without Runtime participation
- CER success
- CER failure
- CER unknown

The current shape survives that pressure without adding explanation text or
duplicating truth across extra fields.

## Scope

Included:

- exact-line readability proof for all current record types
- field-order proof across records with and without `code`
- proof that `code` appears only when current accountable meaning requires it
- proof that omitted fields remain omitted rather than serialized as null or
  empty placeholders

Excluded:

- explanation fields
- timestamps
- parser/admin tooling
- schema expansion for readability support

## Verification

Covered by tests:

- every current record type emits in the same left-to-right field order
- refusal, denial, success, failure, and unknown lines remain readable as raw
  JSONL
- optional `code` appears only on lines that currently need it
- no null, empty, or explanatory placeholders appear

## Uncertainties

This proof does not establish:

- final schema authority
- whether future operational usage will require timestamps
- whether future higher-layer inspection tooling changes readability needs

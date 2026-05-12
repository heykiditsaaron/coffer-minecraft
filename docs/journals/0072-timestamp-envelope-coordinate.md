# 0072 - Timestamp as Required Accountability Envelope Coordinate

## Summary
Added a required numeric `timestamp` as the leftmost field in the minimal SER/CER JSONL accountability envelope.

## Decision
Use UNIX epoch milliseconds for all emitted accountability records, ordered as:
`timestamp`, `interactionId`, `recordType`, `stage`, optional `code`.

## Rationale
The envelope now carries the temporal coordinate needed to read accountability as a routing receipt without expanding the schema into explanatory metadata. Millisecond UNIX time keeps the value numeric, compact, and machine-friendly while preserving the current omission discipline.

## Scope
Included:
- ghost-adapter projection records
- ghost-adapter JSONL emission
- Fabric lifecycle accountability records
- Fabric construction/Core contact proof tests
- raw-readability and append-order assertions

Excluded:
- human-readable timestamp strings
- timezone fields
- nested envelopes
- sequence fields
- explanatory metadata
- schema expansion beyond the required timestamp

## Verification
- `./gradlew :bindings:inventory:test :platforms:fabric:test :platforms:fabric:compileJava` passed.
- `git diff --check` passed.
- Live Fabric dev runtime produced new timestamped JSONL lines under `platforms/fabric/run/logs/coffer/fabric-lifecycle.jsonl`.

## Uncertainties
- Existing runtime log files in `platforms/fabric/run/logs/coffer/` may contain historical pre-timestamp records from earlier runs. This step proves the new envelope shape for current emissions; it does not backfill old generated output.

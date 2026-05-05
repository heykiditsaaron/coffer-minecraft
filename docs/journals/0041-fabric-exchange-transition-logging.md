# Fabric Exchange Transition Logging

## Summary

Expose platform-side exchange service transitions for submitted player-trade diagnosis.

## Decision

Add visible SLF4J logging around Fabric service discovery, submitExchange, server-thread scheduling, arbitration, runtime execution, and future completion.

## Rationale

The platform service currently uses `System.Logger` for initialization only, and those records do not appear in local Fabric logs. The adapter needs platform-side evidence to distinguish uninitialized service, unavailable server, scheduler non-entry, arbitration denial, runtime execution, and exceptional completion.

## Scope

Included:

- platform transition logging only
- no execution behavior changes
- no TransferableValueAuthority schema changes

Excluded:

- scheduler policy changes
- retry, rollback, or timeout behavior
- result semantics changes

## Verification

Run `PATH=/opt/gradle/gradle-8.14.3/bin:$PATH gradle test`.

## Uncertainties

The next live run must confirm which logged transition is the last one reached.

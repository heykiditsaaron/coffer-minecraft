# Command and Balance Surfaces — Concept

## Purpose

This document defines the conceptual command- and API-level surfaces exposed by Coffer.

Commands in Coffer are not merely execution triggers.
They are explicit intent surfaces that allow players, administrators, and integrations
to interact with the economy honestly and safely.

This document is conceptual only.
It defines meaning and responsibility, not implementation.

---

## Core Principle

Commands express intent.
Execution only occurs after truth is established and consent is given.

No command in Coffer is allowed to:
- guess outcomes
- infer missing information
- mutate state implicitly
- hide denial behind silence

---

## Categories of Command Surfaces

Coffer command surfaces fall into four conceptual categories:

1. Intent Exploration
2. Intent Confirmation
3. Authoritative Balance Adjustment
4. Balance Observation

Each category has strict boundaries.

---

## Intent Exploration

Intent Exploration commands allow a user to explore a possible action without risk.

They are:
- non-mutating
- safe to experiment with
- reversible by doing nothing

### /sell

Meaning:

“I would like to explore selling items I own.”

The `/sell` command does not mean:
- sell immediately
- commit to mutation
- agree to rules

It begins an exploration flow where:
- the player may select items they own
- the system evaluates honestly
- no mutation occurs without confirmation

The sell command is the primary trust handshake between player and system.

---

## Intent Confirmation

Intent Confirmation represents explicit consent to a previously previewed outcome.

Confirmation means exactly one thing:

“I understand and accept this exact outcome.”

Confirmation:
- is bound to a specific preview
- does not renegotiate
- does not re-evaluate
- becomes invalid if reality changes

If confirmation fails, nothing happens.

---

## Authoritative Balance Adjustment

These commands exist to prevent dishonest or out-of-band balance manipulation.

They represent server-authoritative facts, not exchanges.

They:
- do not involve valuation
- do not involve inventory
- do not invoke Core exchange evaluation

### Balance Credit

Conceptual form:

/balance credit <player> <currency> <amount>

Meaning:

“The server explicitly increases this player’s balance.”

Credits are:
- atomic
- auditable
- server-initiated
- explicit

---

### Balance Debit

Conceptual form:

/balance debit <player> <currency> <amount>

Meaning:

“The server explicitly decreases this player’s balance.”

Debits:
- are atomic
- must fail explicitly if insufficient balance exists
- do not imply wrongdoing

---

## Balance Observation

Balance observation commands provide transparency without control.

They:
- are read-only
- do not mutate state
- do not invoke Core
- never estimate or infer

Conceptual forms:

/balance  
/balance <currency>  
/balance <player>  
/balance <player> <currency>

Meaning:

“What is the current known balance?”

Balance observation exists to reduce anxiety and guesswork.

---

## Currency Semantics

### Currency Identity

Currency is an explicit concept in Coffer.

Each currency has:
- an admin-configured name
- a plural name
- an optional symbol
- a configured number of decimal places

Coffer does not assume:
- a single currency
- equivalence between currencies
- or conversion rules

---

### Default Currency

Each server must configure one default currency.

The default currency:
- is used when no currency is specified
- exists for usability
- does not imply exclusivity

---

### Currency Conversion

Currency conversion is explicitly out of scope for Coffer.

Admins may implement conversion via:
- items
- shops
- mechanics
- or external mods

Coffer enforces honesty within currencies, not between them.

---

## Relationship to Core

The command surfaces described here:
- do not modify Core law
- do not bypass Core
- do not reinterpret Core results

Core remains responsible only for exchange evaluation.

---

## Design Summary

These command and balance surfaces exist to ensure that:
- players can explore safely
- admins can act without hacks
- integrations remain honest
- future adapters remain consistent

Coffer prefers refusal over assumption,
and clarity over cleverness.

That preference applies equally to commands.

COFFER WORKFLOW — EXECUTION & COLLABORATION MODEL
===============================================

PURPOSE
-------
This workflow exists to:
- prevent guessing,
- prevent semantic drift,
- preserve honesty over convenience,
- and allow narrow, local thinking without re-explaining the entire system.

It is optimized for:
- long-running projects,
- intermittent contributor availability,
- mobile / low-context work sessions,
- and future handoff to new collaborators.

------------------------------------------------

CORE PRINCIPLES
---------------

1. LAW BEFORE CODE
   - Core meaning is written, frozen, and referenced.
   - Code must conform to law, never the reverse.
   - If code and law disagree, code is wrong.

2. NO GUESSING
   - If something is unclear, stop.
   - Clarify intent before implementing.
   - Compiler errors are signals, not annoyances.

3. FAIL CLOSED
   - Absence of truth results in refusal, not defaults.
   - System must boot even when functionality is absent.
   - Refusal is always preferable to pretending.

4. PLACEHOLDERS MUST CONFESS
   - Any temporary behavior must:
     - be explicit,
     - be documented,
     - and be impossible to mistake for final behavior.

5. BREADTH FIRST, THEN DEPTH
   - Establish structure and boundaries first.
   - Defer detail until truth requires it.
   - Never lock the future prematurely.

------------------------------------------------

PHASE-BASED EXECUTION
---------------------

Work is divided into PHASES.
Each phase:
- has a narrow scope,
- introduces one category of truth,
- and freezes when complete.

A phase is NOT:
- a feature list,
- a UX promise,
- or a guarantee of usefulness.

A phase IS:
- a proof that a truth boundary holds.

------------------------------------------------

ATOMIC STEPS
------------

All work proceeds in ATOMIC STEPS.

An atomic step:
- touches the minimum number of files,
- does one conceptual thing,
- and is reversible.

Never:
- bundle unrelated changes,
- “just fix one more thing,”
- or assume a later step will clean it up.

------------------------------------------------

BRANCHING RITUAL
----------------

For every phase or sub-phase:

1. Create a new branch BEFORE code changes
   Example:
     git checkout -b phase-3c1-fabric-valuation

2. All work happens on that branch.
3. main is never used for active work.

Branches represent:
- intent,
- scope,
- and the right to abandon safely.

------------------------------------------------

EDITING RULES
-------------

1. FULL FILES ONLY
   - No snippets.
   - Entire file replacements only.

2. EXPLICIT FILE PATHS
   - Every change names the full path and filename.

3. ONE FILE = ONE CODE BLOCK
   - Especially important for mobile workflows.

4. NO SILENT BEHAVIOR
   - If behavior exists, it must be visible in code or docs.

------------------------------------------------

COMPILE & RUNTIME CHECKS
------------------------

After any meaningful change:

1. Compile the relevant module
   - Compilation failures are alignment signals.
   - Fix by conforming to existing contracts, not inventing APIs.

2. Run minimal runtime verification
   - Confirm only the intended behavior.
   - Do NOT expand scope during testing.

------------------------------------------------

PLACEHOLDER MANAGEMENT
----------------------

If placeholder behavior exists:

- It must be:
  - named as placeholder,
  - scoped to a phase,
  - and removed or replaced in a later phase.

- Placeholder behavior must NEVER:
  - mutate real state,
  - grant value,
  - or imply completion.

------------------------------------------------

CHRONICLING
-----------

At the end of each phase:

1. Write a chronicle entry.
2. Chronicle MUST include:
   - what was proven,
   - what was intentionally NOT done,
   - any behavior that could be misread,
   - explicit boundaries to the next phase.

Chronicles exist to:
- externalize memory,
- prevent re-litigation,
- and allow future contributors to resume safely.

------------------------------------------------

GIT RITUAL (END OF PHASE)
------------------------

1. Verify git status (only intended files changed).
2. Stage only those files.
3. Commit with:
   - phase identifier,
   - truthful scope description.
4. Push branch.
5. Open PR via GitHub UI.
6. Squash and merge.
7. Delete branch (remote).
8. Return local repo to main.
9. Confirm clean working tree.

No exceptions.

------------------------------------------------

DECISION CHECKPOINTS
--------------------

At any point, if something:
- feels wrong,
- creates discomfort,
- or appears to introduce dishonesty,

The correct action is:
- STOP,
- articulate the concern,
- resolve it in law or contract,
- then continue.

Intuition is treated as an early-warning system,
not a nuisance.

------------------------------------------------

HANDOFF GUARANTEE
-----------------

If this workflow is followed:

- A new collaborator can:
  - read the law,
  - read the chronicles,
  - inspect the phase structure,
  - and resume work without oral tradition.

The system does not depend on memory.
It depends on written truth.

------------------------------------------------

### Running the Fabric dev server
./gradlew :src:adapter-fabric:runServer

CLOSING STATEMENT
-----------------

We do not optimize for speed.
We optimize for correctness and survivability.

Progress that lies is debt.
Refusal that is honest is success.

This workflow exists to make "boring"
the most exciting outcome possible.

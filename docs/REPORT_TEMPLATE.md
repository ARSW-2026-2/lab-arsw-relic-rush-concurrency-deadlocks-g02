# ARSW Lab 3 - Relic Rush - Delivery Report

## Team

| Student | ID | GitHub |
|---|---|---|
| Cristian Aristizabal | 1000104617 | Cristian-Aristi |
| Santiago Pinzon | 1000103871 | els4nty |
| Daniel Peña | 1000099589 | KronorCR |

Repository: `https://github.com/ARSW-2026-2/lab-arsw-relic-rush-concurrency-deadlocks-g02.git`

Final commit: `SHA`

## 1. Baseline observations

- Command(s) executed:
- What happened?
- Was the round invariant always preserved?
- Did the game stop unexpectedly?

Evidence:

```text
PASTE RELEVANT OUTPUT
```

## 2. Coordination analysis

Explain the responsibility of both barriers:

- `roundStart`: First of all, `roundStart` is essentially a `CyclicBarrier` set to the number of adventurers + 1. The problem it solves is that no adventurer can start the next round unless all adventurers are in the same round, and furthermore, no adventurer can start until the coordinator allows it. This ensures that the invariant is maintained as it should be.
- `roundEnd`: The `roundEnd` function helps us with the game's end barrier; every adventurer passes through this section after `playTurn()`, and the coordinator also passes through this barrier. This solves the problem of correctly detecting the end of the game, since by that point the coordinator has already entered all the scores, and all game values are up to date. This prevents the coordinator from posting a message midway through the game that contains data that isn't the latest version.

3. Why is `Thread.sleep(...)` not a valid replacement for a barrier?
> Ans: We can't do this, because Thread.sleep() pauses all threads for the amount of time we specify as an argument, and even if we happen to match the time of a particular adventurer, it can vary in every case. You can't use this implementation because you don't know how long the adventurers will take on their turn—some may take longer, and others may take less time.

4. What memory-consistency benefit do you obtain by reading the snapshot after the barrier?
> Ans: By reading the snapshot after the barrier, we ensure that the changes made by the other threads before reaching `await()` are visible. This way, the coordinator obtains the updated values without needing `volatile` or `synchronized`. So the data will be up to date—the latest version of each adventurer.

## 3. Thread-safety problems

| Shared state | Problem | Invariant at risk | Solution | Why this solution? |
|---|---|---|---|---|
| | | | | |
| | | | | |

## 4. Deadlock diagnosis

### 4.1 Evidence

```text
PASTE DeadlockProbe OR jcmd/jstack EVIDENCE
```

### 4.2 Coffman conditions in Relic Rush

- Mutual exclusion:
- Hold and wait:
- No preemption:
- Circular wait:

### 4.3 Wait-for graph

Describe or add a diagram.

### 4.4 Fix

What condition did you break?

How did you preserve concurrency between independent forge operations?

## 5. Verification

| Players | Stations | Rounds | Deadlock? | Invariant result |
|---:|---:|---:|---|---|
| 8 | 6 | 50 | | |
| 32 | 8 | 100 | | |
| 128 | 8 | 100 | | |

## 6. Architectural trade-offs

Discuss:

- Correctness / reliability
- Performance / throughput
- Contention
- Maintainability
- Scalability

## 7. Mini ADR

### Context

### Decision

### Alternatives considered

### Consequences

### Evidence

## 8. Conclusions

1.
2.
3.

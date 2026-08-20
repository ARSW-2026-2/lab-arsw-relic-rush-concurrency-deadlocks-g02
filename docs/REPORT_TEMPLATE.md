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

### Evidence Part III
Found one Java-level deadlock:
=============================
"probe-A-anvil-then-furnace":
waiting to lock monitor 0x000002293c31f8d0 (object 0x00000000a6c96af0, a edu.eci.arsw.relicrush.model.ForgeStation),
which is held by "probe-B-furnace-then-anvil"

"probe-B-furnace-then-anvil":
waiting to lock monitor 0x000002293c321150 (object 0x00000000a6c96aa0, a edu.eci.arsw.relicrush.model.ForgeStation),
which is held by "probe-A-anvil-then-furnace"

Java stack information for the threads listed above:
===================================================
...
Found 1 deadlock.

### 4.2 Coffman conditions in Relic Rush

- **Mutual exclusion:** It is evidenced by the use of the `synchronized (first)` and `synchronized (second)` blocks within the `withBoth` method in the `LockPair` class. These blocks act on the intrinsic monitor of each `ForgeStation` object, ensuring that only one thread at a time can own the station[cite: 1].
- **Hold and wait:** It occurs due to the nested locks in `LockPair.withBoth()`[cite: 1]. The thread successfully acquires the monitor of the first station and, without releasing it, goes into a waiting state trying to acquire the monitor of the second station[cite: 1].
- **No preemption:** This is an inherent characteristic of Java's intrinsic monitors (`synchronized`) used in the code[cite: 1]. The Java Virtual Machine (JVM) does not have a mechanism to forcibly take the lock away from a thread; the thread must finish executing its `synchronized` block to release the resource voluntarily.
- **Circular wait:** It materializes when two threads request the same stations but in reverse order. As evidenced in the *Thread Dump*, the `probe-A-anvil-then-furnace` thread holds one station and requests the other, while the `probe-B-furnace-then-anvil` thread does exactly the opposite[cite: 1]. Both get trapped in an infinite loop waiting for each other's resource.

### 4.3 Wait-for graph

The wait cycle is represented as follows:
- `probe-A` owns `Arcane Anvil` (ID 1) ---> requests `Dragon Furnace` (ID 2)
- `probe-B` owns `Dragon Furnace` (ID 2) ---> requests `Arcane Anvil` (ID 1)

This creates a graph with a closed loop where the nodes are the threads and the resources, completely blocking execution.

### 4.4 Fix

**What condition did you break?**
We broke the **Circular wait** condition. By establishing a deterministic global ordering based on the station IDs (always acquiring the monitor of the station with the lower ID first), we guarantee that all threads request the locks in the same direction, making it mathematically impossible for a wait cycle to form.

**How did you preserve concurrency between independent forge operations?**
Concurrency was preserved by using *fine-grained locking*. Instead of using a single global lock that would make the entire game sequential, the threads still only lock the monitors of the specific stations they need. This way, if two adventurers use different stations (disjoint pairs), they can perform their craft operations 100% concurrently.

## 5. Verification

| Players | Stations | Rounds | Deadlock? | Invariant result |
|---:|---:|---:|---|---|
| 8 | 6 | 50 | No deadlocks | Always "OK" |
| 32 | 8 | 100 | No deadlocks | Always "OK" |
| 128 | 8 | 100 | No deadlocks | Always "OK" |

For this part, we put in the ADR, the solution of the point and the necessary evidence.

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

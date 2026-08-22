# ARSW - Laboratory 3 - Relic Rush
## Thread Safety, Coordination and Deadlock Prevention - Java 21

**Course:** Arquitecturas de Software - ARSW  
**Period:** 2026-2  
**Technology:** Java 21 - Maven - JUnit 5  
**Suggested work mode:** teams of 2-3 students  
**Development time:** one week  

---

## 1. Purpose

In Laboratory 1 you measured the effect of concurrency. In Laboratory 2 you worked with race conditions and minimum critical regions. Laboratory 3 goes one step further:

> **How do we coordinate many threads, build thread-safe shared abstractions, detect deadlocks and prevent them without destroying concurrency?**

The focus is not simply to make the program "stop failing". You must explain the invariants, identify the coordination mechanism that each problem needs, and justify the trade-offs of the final design.

---

# 2. Game: Relic Rush

Relic Rush is a small concurrent game. Several adventurers compete to craft as many relics as possible.

Each adventurer is represented by a **platform thread**. During every round it must obtain **two exclusive forge stations** before it can craft a relic.

Examples of stations:

- Arcane Anvil
- Crystal Lens
- Rune Press
- Dragon Furnace
- Moon Altar
- Obsidian Table

When an adventurer gets both stations:

1. it crafts one relic;
2. its personal score increases;
3. a global event is registered in the `ForgeLedger`.

All adventurers play concurrently.

---

# 3. What is intentionally wrong in the starter?

The project contains concurrency defects on purpose.

You should expect to find at least these categories:

1. **Unsafe shared state** in the global ledger.
2. **A non-atomic read-modify-write operation**.
3. **A non-thread-safe collection** receiving concurrent writes.
4. **Nested lock acquisition in inconsistent order**, which can create deadlock.

The round coordination using `CyclicBarrier` is intentionally provided as a correct reference mechanism. Analyze it before modifying other parts.

---

# 4. Rules of the game and invariants

At the end of a completed round, the following values should agree:

```text
sum of all player scores
        ==
ForgeLedger.totalCrafted
        ==
number of ForgeEvent entries
```

Therefore, an important invariant is:

> **Every successfully crafted relic must be reflected exactly once in the player scores and exactly once in the global ledger.**

A second invariant is related to exclusive resources:

> **A forge station cannot be used simultaneously by two incompatible craft operations.**

Your solution must preserve these invariants without making the whole game sequential.

---

# 5. Requirements

- JDK 21
- Maven 3.9+
- Git

Verify:

```bash
java -version
mvn -version
```

---

# 6. Build

```bash
mvn clean test
```

If you only want to run the compiled classes:

```bash
mvn -q -DskipTests package
```

---

# 7. Run the game

Default configuration:

```bash
java -cp target/classes edu.eci.arsw.relicrush.app.RelicRushMain
```

Custom configuration:

```bash
java -cp target/classes edu.eci.arsw.relicrush.app.RelicRushMain <players> <stations> <rounds>
```

Example:

```bash
java -cp target/classes edu.eci.arsw.relicrush.app.RelicRushMain 16 6 50
```

A round snapshot looks like:

```text
ROUND 07 | scoreSum=56 | ledger=54 | events=55 | invariant=BROKEN
```

Do not assume that one correct run proves thread safety.

---

# 8. Part I - Understand the coordination model

Before changing code, study:

- `GameEngine`
- `Adventurer`
- `roundStart`
- `roundEnd`

The game uses two `CyclicBarrier` instances.

### Scenario A - Start gate

All workers wait until the coordinator releases the round.

### Scenario B - Round completion

The coordinator waits until every worker finishes its turn before reading the scoreboard.

Answer in `docs/REPORT_TEMPLATE.md`:

1. What problem does `roundStart` solve?
2. What problem does `roundEnd` solve?
3. Why would `Thread.sleep(...)` be an incorrect coordination strategy here?
4. What memory-consistency benefit do you obtain by reading the snapshot after the barrier?

### Solution:
> We answer this point in the REPORT_TEMPLATE.MD, in this point we put the answer according to the code that we have, and the analysis we do.

---

# 9. Part II - Make the shared ledger thread-safe

Study `ForgeLedger`.

The starter currently contains:

- a shared integer updated with a read-modify-write sequence;
- an `ArrayList` modified concurrently.

Run the isolated ledger probe first:

```bash
java -cp target/classes edu.eci.arsw.relicrush.app.LedgerRaceProbe
java -cp target/classes edu.eci.arsw.relicrush.app.LedgerRaceProbe 64 5000
```

This lets you study the shared-state problem independently from the deadlock exercise.

Your task:

1. Reproduce an invariant violation.
2. Identify the exact shared state.
3. Select appropriate Java concurrency mechanisms.
4. Make the ledger thread-safe.
5. Explain why your solution is preferable to synchronizing the entire game.

Possible tools you may evaluate:

- `AtomicInteger`
- concurrent collections
- `synchronized`
- explicit locks

You are expected to choose, not blindly use all of them.
### Explanation:
>We identified that the totalCrafted variable was using a counter but it was being updated with a read, modify and write the answer but as we know this doesn't work on multiple threads so we decided to use AtomicInteger to protect the thread safety.
>And the other thing that we identified was that the events were being handled by an ArraylList but modifying an ArrayList with concurrent threads like additions that may lead to lost elements or resizing the array may lead to a IndexOutOfBounds so we decided as in the workshop to use a ConcurrentLinkedQueue which allows us to make concurrent insertions.  
---

# 10. Part III - Reproduce and diagnose the deadlock

Run:

```bash
java -cp target/classes edu.eci.arsw.relicrush.app.DeadlockProbe
```

With the starter implementation, you should be able to obtain a result similar to:

```text
DEADLOCK DETECTED
- probe-A-anvil-then-furnace waiting on ... owned by probe-B-furnace-then-anvil
- probe-B-furnace-then-anvil waiting on ... owned by probe-A-anvil-then-furnace
```

If a deadlock is difficult to reproduce, execute the probe several times.

### Optional operating-system diagnosis

Find the Java process:

```bash
jps -l
```

Then inspect its threads:

```bash
jcmd <PID> Thread.print
```

or, when available:

```bash
jstack <PID>
```

Document evidence of the wait cycle.

---

# 11. Part IV - Explain the four deadlock conditions

Map the four Coffman conditions to the game:

1. **Mutual exclusion**
2. **Hold and wait**
3. **No preemption**
4. **Circular wait**

Your report must explain exactly where each one appears in the code.

---

# 12. Part V - Prevent the deadlock

Study:

```text
edu.eci.arsw.relicrush.concurrency.LockPair
```

The starter acquires two station monitors in the order requested by the player.

Your solution must prevent deadlocks **without using one global lock for every craft operation**.

Recommended direction:

> Define a deterministic global ordering for forge stations and always acquire locks in that order.

You may propose a different strategy if you can justify it technically.

After the fix:

```bash
java -cp target/classes edu.eci.arsw.relicrush.app.DeadlockProbe
```

Expected outcome:

```text
NO DEADLOCK DETECTED within 2 seconds.
```

---

# 13. Part VI - Stress verification

After fixing both thread safety and deadlocks, execute at least:

```bash
java -cp target/classes edu.eci.arsw.relicrush.app.InvariantProbe 8 6 50
java -cp target/classes edu.eci.arsw.relicrush.app.InvariantProbe 32 8 100
java -cp target/classes edu.eci.arsw.relicrush.app.InvariantProbe 128 8 100
```

For every completed round:

```text
invariant=OK
```

The game must finish normally.

---

# 14. Architectural analysis

The final report must connect the implementation with quality attributes.

Discuss at least:

### Correctness / reliability

- Which invariants are protected?
>The invariants that are protected are scoreSum, ledgerTotal and eventCount.
- What evidence demonstrates that they hold?
>The use of AtomicInteger and ConcurrentinkedQueue in ForgeLedger prevents lost updates during concurrent writes. The CyclicBarrier ensures that all threads finish before the program reads the snapshot and the InvariantProbe runs successfully without breaking the invariant.   
### Performance / throughput

- Where can lock contention appear?
>This happens when several adventurers randomly select the same stations at the exact same time.
- Why is a global lock undesirable?
>Because it would force multiple threads to being executed sequentially which isn't the objective of making a concurrent program.
- Which craft operations can still execute concurrently?
>For example if player 1 is using stations A/B and player 2 is using stations C/D it can perfectly be parallel.
### Maintainability

- Is lock ownership obvious?
>It is completely encapsulated inside LockPair.withBoth(). The adventurer doesn't need to manage synchronized blocks.
- Is the lock ordering rule explicit and easy to preserve?
>Yes it is, if some future developer would need to add new features. Then he'll only need to use de LockPair utility in this way we can reduce the reinsertion of deadlocks.
### Scalability

- What happens when the number of players grows while the number of stations stays constant?
>For example if we run 128 players with only 8 stations would scale safely but the performance will be very reduced because most of the threads will be locked waiting to execute.
---

# 15. Mini ADR

Create:

```text
docs/ADR-001-deadlock-prevention.md
```

Use this structure:


# ADR-001: Deadlock prevention strategy

# Part V - Prevent the deadlock:

## Context:
This part give us the next information:
The starter acquires two station monitors in the order requested by the player.

Your solution must prevent deadlocks without using one global lock for every craft operation.

Recommended direction:

> Define a deterministic global ordering for forge stations and always acquire locks in that order.

You may propose a different strategy if you can justify it technically.

## Decision:

We fixed the LockPair() class, removing the `sleepQuietly(2)` since we shouldn’t use it—it’s a workaround to force a deadlock. Therefore, to properly implement the architecture, we removed it. The “first then second” order also disappears due to an ID comparison, and we fixed a rule that applies if two entities are at the same station.


## Alternatives considered:

This is the best approach because it meets the requirements, uses the correct architecture, preserves the invariant, and uses `synchronized` properly—implementing it to prevent deadlocks, similar to the strategy we saw in the workshop, where we compared the IDs so that the adventurer could continue their action.

## Quality attributes affected:

Correctness/Liveness, Performance/Throughput, Maintainability and Scalability.

## Evidence:
After the fix we can see the next message in the terminal:
![Deadlock detectado con LockPair original](/docs/images/afterFix.png)

And then that we put the correct implementation and the solution:

- java -cp target/classes edu.eci.arsw.relicrush.app.InvariantProbe 8 6 50
  ![Prueba1 8 6 50](/docs/images/prueba1.png)

- java -cp target/classes edu.eci.arsw.relicrush.app.InvariantProbe 32 8 100
  ![Prueba2 32 8 100](/docs/images/prueba2.png)

- java -cp target/classes edu.eci.arsw.relicrush.app.InvariantProbe 128 8 100
  ![Prueba3 128 8 100](/docs/images/prueba3.png)


## Consequences:

On the positive side, we find that:
1. Deadlocks are eliminated by design, not by probability.
2. Zero concurrency cost.
3. Negligible runtime cost.

On the negative side, we find that:
1. An implicit dependency is created: “everyone must go through LockPair.”
2. Coupling to id() being stable and immutable

## Risks:

The risks we would face with this are that, in the future, the following could happen:

1. Someone could take two station locks without going through LockPair.
2. id() could cease to be stable or unique.
3. It could grow to more than 2 resources per operation.

---

---

# 16. Restrictions

- Java 21 only.
- Do not remove concurrency.
- Do not convert the game into sequential execution.
- Do not solve the whole game with one global lock.
- Do not delete the barriers to hide synchronization problems.
- Do not use arbitrary sleeps as a coordination mechanism.
- Do not replace the required reasoning with only screenshots.
- The final solution must pass repeated stress executions.

This lab deliberately uses **platform threads** instead of revisiting virtual threads. Virtual threads were already explored earlier in the course; here the learning objective is coordination, thread safety and deadlock behavior.

---

# 17. Deliverables

Your repository must include:

```text
README.md
pom.xml
src/
docs/REPORT.md
docs/ADR-001-deadlock-prevention.md
```

Use `docs/REPORT_TEMPLATE.md` as the starting point for `REPORT.md`.

---

# 18. Evaluation criteria

| Criterion | Weight |
|---|---:|
| Coordination analysis and correct use of barriers | 15% |
| Thread-safe shared state | 20% |
| Deadlock diagnosis and Coffman-condition analysis | 20% |
| Deadlock prevention without coarse global locking | 25% |
| Stress evidence and invariant verification | 10% |
| Architectural reasoning, ADR and code quality | 10% |

---

# 19. Learning sequence

```text
coordination
    -> thread-safe state
    -> invariant
    -> nested locks
    -> deadlock diagnosis
    -> prevention strategy
    -> stress evidence
    -> architectural trade-off
```

The objective is not merely to know `synchronized`.

> **The objective is to design a concurrent solution whose correctness and liveness can be explained and demonstrated.**

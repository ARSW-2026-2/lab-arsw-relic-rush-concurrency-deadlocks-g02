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
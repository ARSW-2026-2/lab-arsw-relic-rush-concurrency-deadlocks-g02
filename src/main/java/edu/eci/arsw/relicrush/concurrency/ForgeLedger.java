package edu.eci.arsw.relicrush.concurrency;

import edu.eci.arsw.relicrush.model.ForgeEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Global match ledger.
 *
 * Starter implementation is intentionally NOT thread-safe.
 */
public final class ForgeLedger {
    private final AtomicInteger totalCrafted = new AtomicInteger(0);
    private final Queue<ForgeEvent> events = new ConcurrentLinkedQueue<>();

    public void record(ForgeEvent event) {
        // TODO LAB 3: ++ is a read-modify-write operation and ArrayList is not
        // designed for concurrent writes. Fix both responsibilities without
        // serializing the entire game behind one global monitor.
        totalCrafted.incrementAndGet();
        events.add(event);
    }

    public int totalCrafted() {
        return totalCrafted.get();
    }

    public int eventCount() {
        return events.size();
    }

    public List<ForgeEvent> snapshot() {
        return List.copyOf(events);
    }
}

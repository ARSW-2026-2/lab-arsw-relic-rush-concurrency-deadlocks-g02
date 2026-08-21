package edu.eci.arsw.relicrush.concurrency;

import edu.eci.arsw.relicrush.model.ForgeEvent;

import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Global match ledger. Thread-safe: totalCrafted uses an atomic
 * read-modify-write and events uses a lock-free concurrent queue.
 */
public final class ForgeLedger {
    private final AtomicInteger totalCrafted = new AtomicInteger(0);
    private final Queue<ForgeEvent> events = new ConcurrentLinkedQueue<>();

    public void record(ForgeEvent event) {
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

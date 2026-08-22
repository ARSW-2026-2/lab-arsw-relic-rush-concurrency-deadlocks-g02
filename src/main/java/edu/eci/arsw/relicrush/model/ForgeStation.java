package edu.eci.arsw.relicrush.model;

import java.util.concurrent.atomic.AtomicReference;

/**
 * Exclusive resource used to craft a relic. The station itself is the monitor.
 *
 * GUI NOTE (bonus): {@code occupant} is a purely observational field used so
 * a graphical view can show which adventurer currently holds this station's
 * monitor. It is written/cleared by {@link edu.eci.arsw.relicrush.concurrency.LockPair}
 * strictly WHILE the station's monitor is already held (see LockPair.withBoth),
 * so it never introduces a new lock, a new acquisition order, or any extra
 * coordination point. Deleting it would not change the concurrency behavior
 * of the game at all -- only the GUI would lose this piece of visual feedback.
 */
public final class ForgeStation {
    private final int id;
    private final String name;
    private final AtomicReference<String> occupant = new AtomicReference<>(null);

    public ForgeStation(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public int id() {
        return id;
    }

    public String name() {
        return name;
    }

    /** GUI-only bookkeeping; must be called while holding this station's monitor. */
    public void markOccupied(String adventurerName) {
        occupant.set(adventurerName);
    }

    /** GUI-only bookkeeping; must be called while holding this station's monitor. */
    public void markFree() {
        occupant.set(null);
    }

    public String occupant() {
        return occupant.get();
    }

    public boolean isFree() {
        return occupant.get() == null;
    }

    @Override
    public String toString() {
        return name + "(#" + id + ")";
    }
}

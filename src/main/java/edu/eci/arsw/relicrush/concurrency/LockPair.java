package edu.eci.arsw.relicrush.concurrency;

import edu.eci.arsw.relicrush.model.ForgeStation;

/**
 * LAB 3 - Part V: deadlock prevention via deterministic lock ordering.
 * All forge stations are ordered globally by ForgeStation.id(); the
 * lower-id station's monitor is always acquired before the higher-id
 * one, regardless of the order the caller passed them in. This breaks
 * the "circular wait" Coffman condition without introducing any
 * global/game-wide lock.
 *
 * GUI NOTE (bonus): the markOccupied/markFree calls are purely observational
 * bookkeeping for the graphical view. They execute strictly inside the
 * synchronized blocks that already hold each station's monitor, so they do
 * not add any new lock, do not change acquisition order, and do not weaken
 * the deadlock-prevention guarantee proven by DeadlockProbe/InvariantProbe.
 */
public final class LockPair {

    private LockPair() {
    }

    public static void withBoth(ForgeStation first, ForgeStation second, Runnable action) {
        if (first.id() == second.id()) {
            synchronized (first) {
                first.markOccupied(Thread.currentThread().getName());
                try {
                    action.run();
                } finally {
                    first.markFree();
                }
            }
            return;
        }

        ForgeStation lower = first.id() < second.id() ? first : second;
        ForgeStation higher = first.id() < second.id() ? second : first;

        synchronized (lower) {
            lower.markOccupied(Thread.currentThread().getName());
            synchronized (higher) {
                higher.markOccupied(Thread.currentThread().getName());
                try {
                    action.run();
                } finally {
                    higher.markFree();
                }
            }
            lower.markFree();
        }
    }
}

package edu.eci.arsw.relicrush.concurrency;

import edu.eci.arsw.relicrush.model.ForgeStation;

/**
 * LAB 3 - Part V: deadlock prevention via deterministic lock ordering.
 * All forge stations are ordered globally by ForgeStation.id(); the
 * lower-id station's monitor is always acquired before the higher-id
 * one, regardless of the order the caller passed them in. This breaks
 * the "circular wait" Coffman condition without introducing any
 * global/game-wide lock.
 */
public final class LockPair {

    private LockPair() {
    }

    public static void withBoth(ForgeStation first, ForgeStation second, Runnable action) {
        // TODO LAB 3: Solved
        if (first.id() == second.id()) {
            synchronized (first) {
                action.run();
            }
            return;
        }

        ForgeStation lower = first.id() < second.id() ? first : second;
        ForgeStation higher = first.id() < second.id() ? second : first;

        synchronized (lower) {
            synchronized (higher) {
                action.run();
            }
        }

    }
}
package edu.eci.arsw.relicrush.game;

import edu.eci.arsw.relicrush.concurrency.ForgeLedger;
import edu.eci.arsw.relicrush.model.ForgeStation;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.concurrent.atomic.AtomicBoolean;

public final class GameEngine {
    private final GameConfig config;
    private final ForgeLedger ledger = new ForgeLedger();
    private final List<ForgeStation> stations;
    private final List<Adventurer> adventurers = new ArrayList<>();
    private final CyclicBarrier roundStart;
    private final CyclicBarrier roundEnd;
    private final AtomicBoolean finished = new AtomicBoolean(false);

    // --- GUI support (bonus). None of this touches roundStart/roundEnd's
    // party count, wait semantics, or LockPair's ordering rule. ---
    private volatile GameListener listener = GameListener.NONE;
    private final Object pauseLock = new Object();
    private volatile boolean paused = false;
    private volatile boolean stopRequested = false;

    public GameEngine(GameConfig config) {
        this.config = config;
        this.stations = createStations(config.stations());
        this.roundStart = new CyclicBarrier(config.adventurers() + 1);
        this.roundEnd = new CyclicBarrier(config.adventurers() + 1);

        for (int i = 1; i <= config.adventurers(); i++) {
            adventurers.add(new Adventurer(
                    i,
                    stations,
                    ledger,
                    roundStart,
                    roundEnd,
                    config.rounds()));
        }
    }

    public void setListener(GameListener listener) {
        this.listener = listener == null ? GameListener.NONE : listener;
    }

    public GameConfig config() {
        return config;
    }

    public List<ForgeStation> stations() {
        return stations;
    }

    public List<Adventurer> adventurers() {
        return List.copyOf(adventurers);
    }

    /**
     * Requests a pause before the NEXT round. Because roundStart is a
     * CyclicBarrier that needs (adventurers + 1) parties, simply having the
     * coordinator thread hold off calling roundStart.await() is enough to
     * freeze every adventurer thread at the barrier -- no extra flag needs
     * to be checked inside Adventurer at all.
     */
    public void pause() {
        synchronized (pauseLock) {
            paused = true;
        }
    }

    public void resume() {
        synchronized (pauseLock) {
            paused = false;
            pauseLock.notifyAll();
        }
    }

    public boolean isPaused() {
        return paused;
    }

    /**
     * Cooperative stop: any adventurer already blocked on roundStart or
     * roundEnd is released via BrokenBarrierException (each Adventurer
     * already catches that exception and exits its loop). Any adventurer
     * mid-turn simply finishes its current craft and then hits the (now
     * broken/reset) barrier and exits the same way.
     */
    public void requestStop() {
        stopRequested = true;
        synchronized (pauseLock) {
            paused = false;
            pauseLock.notifyAll();
        }
        roundStart.reset();
        roundEnd.reset();
    }

    public boolean isStopRequested() {
        return stopRequested;
    }

    public void run() throws InterruptedException, BrokenBarrierException {
        startDeadlockWatchdog();
        adventurers.forEach(Thread::start);
        listener.onGameStarted(config, stations, adventurers());

        RoundSnapshot lastSnapshot = null;
        try {
            for (int round = 1; round <= config.rounds(); round++) {
                awaitIfPaused();
                if (stopRequested) {
                    break;
                }

                // Scenario 2: workers wait until the coordinator starts the round.
                roundStart.await();

                // Scenario 3: coordinator waits until every worker completes the round.
                roundEnd.await();

                lastSnapshot = buildSnapshot(round);
                printRoundSnapshot(lastSnapshot);
                listener.onRoundCompleted(lastSnapshot);
            }
        } catch (BrokenBarrierException e) {
            if (!stopRequested) {
                throw e; // a real, unrequested break -> surface it
            }
            // else: this is the expected effect of requestStop(); fall through.
        }

        for (Adventurer adventurer : adventurers) {
            adventurer.join();
        }

        finished.set(true);
        printFinalSummary();
        listener.onGameFinished(lastSnapshot, stopRequested);
    }

    private void awaitIfPaused() throws InterruptedException {
        synchronized (pauseLock) {
            while (paused && !stopRequested) {
                pauseLock.wait();
            }
        }
    }

    private void startDeadlockWatchdog() {
        Thread watchdog = new Thread(() -> {
            ThreadMXBean bean = ManagementFactory.getThreadMXBean();
            while (!finished.get()) {
                long[] ids = bean.findDeadlockedThreads();
                if (ids != null && ids.length > 0) {
                    System.err.println("\n*** DEADLOCK DETECTED BY GAME WATCHDOG ***");
                    System.err.println("Run DeadlockProbe or jcmd <PID> Thread.print for a focused diagnosis.");
                    System.err.println("The starter exits here so you do not have to kill a frozen process manually.\n");
                    System.exit(2);
                }
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
        }, "deadlock-watchdog");
        watchdog.setDaemon(true);
        watchdog.start();
    }

    private RoundSnapshot buildSnapshot(int round) {
        int scoreSum = adventurers.stream().mapToInt(Adventurer::score).sum();
        int ledgerTotal = ledger.totalCrafted();
        int eventCount = ledger.eventCount();
        boolean invariantOk = (scoreSum == ledgerTotal && ledgerTotal == eventCount);

        List<RoundSnapshot.PlayerScore> playerScores = adventurers.stream()
                .map(a -> new RoundSnapshot.PlayerScore(a.playerId(), a.getName(), a.score(), a.status()))
                .toList();

        return new RoundSnapshot(round, config.rounds(), scoreSum, ledgerTotal, eventCount, invariantOk, playerScores);
    }

    private void printRoundSnapshot(RoundSnapshot snapshot) {
        System.out.printf(
                "ROUND %02d | scoreSum=%d | ledger=%d | events=%d | invariant=%s%n",
                snapshot.round(),
                snapshot.scoreSum(),
                snapshot.ledgerTotal(),
                snapshot.eventCount(),
                snapshot.invariantOk() ? "OK" : "BROKEN");
    }

    private void printFinalSummary() {
        System.out.println("\n=== RELIC RUSH - FINAL SCORE ===");
        adventurers.stream()
                .sorted(Comparator.comparingInt(Adventurer::score).reversed())
                .forEach(a -> System.out.printf("%-16s %4d relics%n", a.getName(), a.score()));

        int scoreSum = adventurers.stream().mapToInt(Adventurer::score).sum();
        System.out.printf("Total by players : %d%n", scoreSum);
        System.out.printf("Ledger total     : %d%n", ledger.totalCrafted());
        System.out.printf("Ledger events    : %d%n", ledger.eventCount());
    }

    private static List<ForgeStation> createStations(int count) {
        String[] names = {
                "Arcane Anvil", "Crystal Lens", "Rune Press", "Dragon Furnace",
                "Moon Altar", "Obsidian Table", "Echo Forge", "Solar Crucible"
        };
        List<ForgeStation> result = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            result.add(new ForgeStation(i + 1, names[i % names.length] + " " + (i + 1)));
        }
        return List.copyOf(result);
    }
}

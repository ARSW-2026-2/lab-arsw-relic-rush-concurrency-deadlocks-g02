package edu.eci.arsw.relicrush.gui;

import edu.eci.arsw.relicrush.game.Adventurer;
import edu.eci.arsw.relicrush.game.GameConfig;
import edu.eci.arsw.relicrush.game.GameEngine;
import edu.eci.arsw.relicrush.game.GameListener;
import edu.eci.arsw.relicrush.game.RoundSnapshot;
import edu.eci.arsw.relicrush.model.ForgeStation;

import javax.swing.SwingUtilities;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Owns the lifecycle of a single GameEngine run and re-dispatches every
 * GameListener callback onto the Swing Event Dispatch Thread, so panels can
 * update Swing components directly and safely.
 *
 * GameEngine.run() is blocking (it calls CyclicBarrier.await()), so it is
 * always executed on its own dedicated background thread, never on the EDT.
 */
public final class GameController {

    public interface UiCallback {
        void onStarted(GameConfig config, List<ForgeStation> stations, List<Adventurer> adventurers);

        void onRoundCompleted(RoundSnapshot snapshot);

        void onFinished(RoundSnapshot finalSnapshot, boolean stoppedEarly);

        void onError(Exception e);
    }

    private final UiCallback ui;
    private final AtomicReference<GameEngine> currentEngine = new AtomicReference<>();
    private volatile Thread engineThread;

    public GameController(UiCallback ui) {
        this.ui = ui;
    }

    public boolean isRunning() {
        Thread t = engineThread;
        return t != null && t.isAlive();
    }

    public synchronized void start(GameConfig config) {
        if (isRunning()) {
            return;
        }

        GameEngine engine = new GameEngine(config);
        engine.setListener(new GameListener() {
            @Override
            public void onGameStarted(GameConfig cfg, List<ForgeStation> stations, List<Adventurer> adventurers) {
                SwingUtilities.invokeLater(() -> ui.onStarted(cfg, stations, adventurers));
            }

            @Override
            public void onRoundCompleted(RoundSnapshot snapshot) {
                SwingUtilities.invokeLater(() -> ui.onRoundCompleted(snapshot));
            }

            @Override
            public void onGameFinished(RoundSnapshot finalSnapshot, boolean stoppedEarly) {
                SwingUtilities.invokeLater(() -> ui.onFinished(finalSnapshot, stoppedEarly));
            }
        });

        currentEngine.set(engine);
        Thread thread = new Thread(() -> {
            try {
                engine.run();
            } catch (Exception e) {
                SwingUtilities.invokeLater(() -> ui.onError(e));
            }
        }, "relic-rush-engine");
        thread.setDaemon(true);
        engineThread = thread;
        thread.start();
    }

    public void pause() {
        GameEngine engine = currentEngine.get();
        if (engine != null) {
            engine.pause();
        }
    }

    public void resume() {
        GameEngine engine = currentEngine.get();
        if (engine != null) {
            engine.resume();
        }
    }

    public void stop() {
        GameEngine engine = currentEngine.get();
        if (engine != null) {
            engine.requestStop();
        }
    }

    /** Read-only access, e.g. so a UI refresh timer can poll live station/adventurer state. */
    public GameEngine currentEngine() {
        return currentEngine.get();
    }
}

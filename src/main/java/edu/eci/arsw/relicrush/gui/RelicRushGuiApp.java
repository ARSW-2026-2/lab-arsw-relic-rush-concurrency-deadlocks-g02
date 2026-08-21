package edu.eci.arsw.relicrush.gui;

import edu.eci.arsw.relicrush.game.Adventurer;
import edu.eci.arsw.relicrush.game.GameConfig;
import edu.eci.arsw.relicrush.game.GameEngine;
import edu.eci.arsw.relicrush.game.RoundSnapshot;
import edu.eci.arsw.relicrush.model.ForgeStation;

import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * Bonus GUI for Relic Rush.
 *
 * This class ONLY observes and drives the existing GameEngine through its
 * public API (setListener/pause/resume/requestStop). It never touches
 * CyclicBarrier, LockPair, or ForgeLedger directly, and it never runs the
 * blocking GameEngine.run() on the Event Dispatch Thread.
 */
public final class RelicRushGuiApp extends JFrame implements GameController.UiCallback {

    private final ControlPanel controlPanel;
    private final AdventurerPanel adventurerPanel = new AdventurerPanel();
    private final StationPanel stationPanel = new StationPanel();
    private final ScoreboardPanel scoreboardPanel = new ScoreboardPanel();
    private final GameController controller = new GameController(this);

    // Pure UI refresh timer: reads volatile/atomic display-only fields
    // (Adventurer.status(), ForgeStation.occupant()) at a fixed cadence.
    // It is NOT a coordination mechanism for the game itself.
    private final Timer liveRefreshTimer;

    public RelicRushGuiApp() {
        super("Relic Rush - ARSW Lab 3");

        controlPanel = new ControlPanel(this::onStartRequested, this::onPauseRequested,
                this::onResumeRequested, this::onStopRequested);

        JPanel center = new JPanel(new GridLayout(1, 2, 8, 8));
        center.add(adventurerPanel);
        center.add(stationPanel);

        setLayout(new BorderLayout(8, 8));
        add(controlPanel, BorderLayout.NORTH);
        add(center, BorderLayout.CENTER);
        add(scoreboardPanel, BorderLayout.SOUTH);

        scoreboardPanel.setPreferredSize(new Dimension(900, 260));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 700);
        setLocationRelativeTo(null);

        liveRefreshTimer = new Timer(150, e -> refreshLiveState());
        liveRefreshTimer.start();
    }

    private void onStartRequested(GameConfig config) {
        adventurerPanel.reset();
        stationPanel.reset();
        scoreboardPanel.reset();
        scoreboardPanel.setStateText("STARTING");
        controlPanel.setState(ControlPanel.RunState.RUNNING);
        controller.start(config);
    }

    private void onPauseRequested() {
        controller.pause();
        controlPanel.setState(ControlPanel.RunState.PAUSED);
        scoreboardPanel.setStateText("PAUSED");
        scoreboardPanel.appendLog(">>> Pause requested. Adventurers will freeze at the next round barrier.");
    }

    private void onResumeRequested() {
        controller.resume();
        controlPanel.setState(ControlPanel.RunState.RUNNING);
        scoreboardPanel.setStateText("RUNNING");
        scoreboardPanel.appendLog(">>> Resumed.");
    }

    private void onStopRequested() {
        scoreboardPanel.appendLog(">>> Stop requested. Releasing round barriers...");
        controller.stop();
        // Buttons stay as-is until onFinished() fires, so the user can't
        // double click Start while the engine thread is still joining.
    }

    private void refreshLiveState() {
        GameEngine engine = controller.currentEngine();
        if (engine == null) {
            return;
        }
        stationPanel.refresh();
        adventurerPanel.refreshLiveStatus(engine.adventurers());
    }

    // ---- GameController.UiCallback: always invoked on the EDT ----

    @Override
    public void onStarted(GameConfig config, List<ForgeStation> stations, List<Adventurer> adventurers) {
        stationPanel.initStations(stations);
        adventurerPanel.initAdventurers(adventurers);
        scoreboardPanel.setStateText("RUNNING");
        scoreboardPanel.appendLog(String.format(
                "Started: adventurers=%d, stations=%d, rounds=%d",
                config.adventurers(), config.stations(), config.rounds()));
    }

    @Override
    public void onRoundCompleted(RoundSnapshot snapshot) {
        scoreboardPanel.updateFromSnapshot(snapshot);
        adventurerPanel.updateFromSnapshot(snapshot);
    }

    @Override
    public void onFinished(RoundSnapshot finalSnapshot, boolean stoppedEarly) {
        controlPanel.setState(ControlPanel.RunState.FINISHED);
        scoreboardPanel.setStateText(stoppedEarly ? "STOPPED" : "FINISHED");
        scoreboardPanel.appendLog(stoppedEarly
                ? ">>> Game stopped by user before completing all rounds."
                : ">>> Game finished: all rounds completed.");
    }

    @Override
    public void onError(Exception e) {
        controlPanel.setState(ControlPanel.RunState.FINISHED);
        scoreboardPanel.setStateText("ERROR");
        scoreboardPanel.appendLog(">>> ERROR: " + e);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new RelicRushGuiApp().setVisible(true));
    }
}

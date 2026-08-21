package edu.eci.arsw.relicrush.gui;

import edu.eci.arsw.relicrush.game.GameConfig;

import javax.swing.*;
import java.awt.*;
import java.util.function.Consumer;

/**
 * Top panel: lets the user pick adventurers/stations/rounds before Start,
 * and exposes Start/Pause/Resume/Stop. Button enablement is a small state
 * machine so it's impossible to e.g. click Resume while already running.
 */
public final class ControlPanel extends JPanel {

    public enum RunState { IDLE, RUNNING, PAUSED, FINISHED }

    private final JSpinner adventurersSpinner = new JSpinner(new SpinnerNumberModel(8, 2, 500, 1));
    private final JSpinner stationsSpinner = new JSpinner(new SpinnerNumberModel(6, 2, 50, 1));
    private final JSpinner roundsSpinner = new JSpinner(new SpinnerNumberModel(50, 1, 100_000, 1));

    private final JButton startButton = new JButton("Start");
    private final JButton pauseButton = new JButton("Pause");
    private final JButton resumeButton = new JButton("Resume");
    private final JButton stopButton = new JButton("Stop");

    public ControlPanel(Consumer<GameConfig> onStart, Runnable onPause, Runnable onResume, Runnable onStop) {
        super(new FlowLayout(FlowLayout.LEFT, 10, 8));

        add(labeled("Adventurers:", adventurersSpinner));
        add(labeled("Stations:", stationsSpinner));
        add(labeled("Rounds:", roundsSpinner));

        startButton.addActionListener(e -> onStart.accept(readConfig()));
        pauseButton.addActionListener(e -> onPause.run());
        resumeButton.addActionListener(e -> onResume.run());
        stopButton.addActionListener(e -> onStop.run());

        add(startButton);
        add(pauseButton);
        add(resumeButton);
        add(stopButton);

        setState(RunState.IDLE);
    }

    private GameConfig readConfig() {
        return new GameConfig(
                (Integer) adventurersSpinner.getValue(),
                (Integer) stationsSpinner.getValue(),
                (Integer) roundsSpinner.getValue());
    }

    /** Central place that enforces which buttons make sense in each state. */
    public void setState(RunState state) {
        boolean idleOrFinished = state == RunState.IDLE || state == RunState.FINISHED;

        adventurersSpinner.setEnabled(idleOrFinished);
        stationsSpinner.setEnabled(idleOrFinished);
        roundsSpinner.setEnabled(idleOrFinished);

        startButton.setEnabled(idleOrFinished);
        pauseButton.setEnabled(state == RunState.RUNNING);
        resumeButton.setEnabled(state == RunState.PAUSED);
        stopButton.setEnabled(state == RunState.RUNNING || state == RunState.PAUSED);
    }

    private static JComponent labeled(String label, JComponent field) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        p.add(new JLabel(label));
        p.add(field);
        return p;
    }
}

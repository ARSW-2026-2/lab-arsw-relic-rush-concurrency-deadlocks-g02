package edu.eci.arsw.relicrush.gui;

import edu.eci.arsw.relicrush.game.GameConfig;

import javax.swing.*;
import java.awt.*;
import java.util.function.Consumer;

/**
 * Top panel: lets the user pick adventurers/stations/rounds before Start,
 * and exposes Start/Pause/Resume/Stop with a dark medieval forge theme.
 */
public final class ControlPanel extends JPanel {

    public enum RunState { IDLE, RUNNING, PAUSED, FINISHED }

    private final JSpinner adventurersSpinner = new JSpinner(new SpinnerNumberModel(8, 2, 500, 1));
    private final JSpinner stationsSpinner = new JSpinner(new SpinnerNumberModel(6, 2, 100, 1));
    private final JSpinner roundsSpinner = new JSpinner(new SpinnerNumberModel(50, 1, 100_000, 1));

    private final JButton startButton = createForgeButton("Forge Start", new Color(46, 139, 87));
    private final JButton pauseButton = createForgeButton("Pause", new Color(184, 134, 11));
    private final JButton resumeButton = createForgeButton("Resume", new Color(70, 130, 180));
    private final JButton stopButton = createForgeButton("Stop", new Color(178, 34, 34));
    private final JButton resetButton = createForgeButton("Reset", new Color(105, 105, 105));
    public ControlPanel(Consumer<GameConfig> onStart, Runnable onPause, Runnable onResume, Runnable onStop, Runnable onReset) {
        super(new FlowLayout(FlowLayout.LEFT, 15, 10));

        setOpaque(false);
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(100, 100, 110), 2),
                BorderFactory.createEmptyBorder(8, 8, 8, 8)
        ));

        styleSpinner(adventurersSpinner, "Adventurers:");
        styleSpinner(stationsSpinner, "Stations:");
        styleSpinner(roundsSpinner, "Rounds:");

        startButton.addActionListener(e -> onStart.accept(readConfig()));
        pauseButton.addActionListener(e -> onPause.run());
        resumeButton.addActionListener(e -> onResume.run());
        stopButton.addActionListener(e -> onStop.run());
        resetButton.addActionListener(e -> onReset.run());

        add(startButton);
        add(pauseButton);
        add(resumeButton);
        add(stopButton);
        add(resetButton);
        setState(RunState.IDLE);
    }

    private JButton createForgeButton(String text, Color baseColor) {
        JButton btn = new JButton(text);
        btn.setFont(FontLoader.getCustomFont(10f));
        btn.setForeground(Color.WHITE);
        btn.setBackground(baseColor);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(60, 60, 70), 2),
                BorderFactory.createEmptyBorder(5, 12, 5, 12)
        ));

        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private void styleSpinner(JSpinner spinner, String labelText) {
        JLabel label = new JLabel(labelText);
        label.setFont(FontLoader.getCustomFont(10f));
        label.setForeground(Color.WHITE);

        JComponent editor = spinner.getEditor();
        if (editor instanceof JSpinner.DefaultEditor defEditor) {
            defEditor.getTextField().setBackground(new Color(30, 30, 40));
            defEditor.getTextField().setForeground(Color.WHITE);
            defEditor.getTextField().setCaretColor(Color.WHITE);
            defEditor.getTextField().setFont(new Font("Monospaced", Font.BOLD, 12));
        }

        add(label);
        add(spinner);
    }

    private GameConfig readConfig() {
        int adv = (int) adventurersSpinner.getValue();
        int st = (int) stationsSpinner.getValue();
        int rounds = (int) roundsSpinner.getValue();
        return new GameConfig(adv, st, rounds);
    }

    public void setState(RunState state) {
        switch (state) {
            case IDLE -> {
                setSpinnersEnabled(true);
                startButton.setEnabled(true);
                pauseButton.setEnabled(false);
                resumeButton.setEnabled(false);
                stopButton.setEnabled(false);
                resetButton.setEnabled(false);
            }
            case RUNNING -> {
                setSpinnersEnabled(false);
                startButton.setEnabled(false);
                pauseButton.setEnabled(true);
                resumeButton.setEnabled(false);
                stopButton.setEnabled(true);
            }
            case PAUSED -> {
                setSpinnersEnabled(false);
                startButton.setEnabled(false);
                pauseButton.setEnabled(false);
                resumeButton.setEnabled(true);
                stopButton.setEnabled(true);
            }
            case FINISHED -> {
                setSpinnersEnabled(true);
                startButton.setEnabled(true);
                pauseButton.setEnabled(false);
                resumeButton.setEnabled(false);
                stopButton.setEnabled(false);
                resetButton.setEnabled(true);
            }
        }
    }

    private void setSpinnersEnabled(boolean enabled) {
        adventurersSpinner.setEnabled(enabled);
        stationsSpinner.setEnabled(enabled);
        roundsSpinner.setEnabled(enabled);
    }
}

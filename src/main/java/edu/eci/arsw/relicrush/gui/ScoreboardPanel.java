package edu.eci.arsw.relicrush.gui;

import edu.eci.arsw.relicrush.game.RoundSnapshot;

import javax.swing.*;
import java.awt.*;

public final class ScoreboardPanel extends JPanel {

    private final JLabel roundLabel = new JLabel("Round: -/-");
    private final JLabel scoreSumLabel = new JLabel("scoreSum: -");
    private final JLabel ledgerLabel = new JLabel("ledger: -");
    private final JLabel eventsLabel = new JLabel("events: -");
    private final JLabel invariantLabel = new JLabel("invariant: -");
    private final JLabel stateLabel = new JLabel("state: IDLE");
    private final JTextArea log = new JTextArea();

    public ScoreboardPanel() {
        super(new BorderLayout(8, 8));
        setBorder(BorderFactory.createTitledBorder("Scoreboard / Invariants"));

        JPanel top = new JPanel(new GridLayout(2, 3, 12, 4));
        Font bold = roundLabel.getFont().deriveFont(Font.BOLD);
        for (JLabel l : new JLabel[]{roundLabel, scoreSumLabel, ledgerLabel, eventsLabel, invariantLabel, stateLabel}) {
            l.setFont(bold);
            top.add(l);
        }
        add(top, BorderLayout.NORTH);

        log.setEditable(false);
        log.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        add(new JScrollPane(log), BorderLayout.CENTER);
    }

    public void setStateText(String text) {
        stateLabel.setText("state: " + text);
    }

    public void updateFromSnapshot(RoundSnapshot s) {
        roundLabel.setText("Round: " + s.round() + "/" + s.totalRounds());
        scoreSumLabel.setText("scoreSum: " + s.scoreSum());
        ledgerLabel.setText("ledger: " + s.ledgerTotal());
        eventsLabel.setText("events: " + s.eventCount());
        invariantLabel.setText("invariant: " + (s.invariantOk() ? "OK" : "BROKEN"));
        invariantLabel.setForeground(s.invariantOk() ? new Color(30, 130, 30) : Color.RED);

        appendLog(String.format(
                "ROUND %02d | scoreSum=%d | ledger=%d | events=%d | invariant=%s",
                s.round(), s.scoreSum(), s.ledgerTotal(), s.eventCount(), s.invariantOk() ? "OK" : "BROKEN"));
    }

    public void appendLog(String line) {
        log.append(line + "\n");
        log.setCaretPosition(log.getDocument().getLength());
    }

    public void reset() {
        roundLabel.setText("Round: -/-");
        scoreSumLabel.setText("scoreSum: -");
        ledgerLabel.setText("ledger: -");
        eventsLabel.setText("events: -");
        invariantLabel.setText("invariant: -");
        invariantLabel.setForeground(Color.BLACK);
        log.setText("");
    }
}

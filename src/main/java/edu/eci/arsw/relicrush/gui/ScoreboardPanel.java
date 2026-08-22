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

        setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(100, 100, 110), 2),
                "Scoreboard / Invariants",
                javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION,
                javax.swing.border.TitledBorder.DEFAULT_POSITION,
                FontLoader.getCustomFont(12f), Color.WHITE
        ));
        setOpaque(false);

        JPanel top = new JPanel(new GridLayout(1, 4, 15, 0));
        top.setOpaque(false);

        roundLabel.setIcon(loadIcon("/Trophy.png", 32, 32));
        eventsLabel.setIcon(loadIcon("/Scroll.png", 32, 32));
        scoreSumLabel.setIcon(loadIcon("/Gem.png", 32, 32));
        ledgerLabel.setIcon(loadIcon("/Shield.png", 32, 32));

        for (JLabel l : new JLabel[]{roundLabel, eventsLabel, scoreSumLabel, ledgerLabel}) {
            l.setFont(FontLoader.getCustomFont(10f));
            l.setForeground(new Color(255, 215, 0));
            l.setVerticalTextPosition(SwingConstants.BOTTOM);
            l.setHorizontalTextPosition(SwingConstants.CENTER);
            l.setHorizontalAlignment(SwingConstants.CENTER);
            top.add(l);
        }

        JLabel invariantLabel = new JLabel("INVARIANT: SECURE", SwingConstants.CENTER);
        invariantLabel.setFont(FontLoader.getCustomFont(11f));
        invariantLabel.setForeground(new Color(57, 255, 20));
        invariantLabel.setOpaque(true);
        invariantLabel.setBackground(new Color(20, 50, 20));
        invariantLabel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(57, 255, 20), 1),
                BorderFactory.createEmptyBorder(5, 0, 5, 0)
        ));

        JPanel topContainer = new JPanel(new BorderLayout(0, 10));
        topContainer.setOpaque(false);
        topContainer.add(top, BorderLayout.CENTER);
        topContainer.add(invariantLabel, BorderLayout.SOUTH);

        add(topContainer, BorderLayout.NORTH);
        add(top, BorderLayout.NORTH);

        top.setOpaque(false);
        Font bold = roundLabel.getFont().deriveFont(Font.BOLD);

        for (JLabel l : new JLabel[]{roundLabel, scoreSumLabel, ledgerLabel, eventsLabel, invariantLabel, stateLabel}) {
            l.setFont(bold);
            l.setForeground(Color.WHITE);
            top.add(l);
        }
        add(top, BorderLayout.NORTH);

        log.setEditable(false);
        log.setBackground(new Color(15, 15, 20, 245));
        log.setForeground(new Color(0, 255, 128));
        log.setFont(FontLoader.getCustomFont(11f));
        log.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JScrollPane logScroll = new JScrollPane(log);
        logScroll.setOpaque(false);
        logScroll.getViewport().setOpaque(false);
        add(logScroll, BorderLayout.CENTER);
    }

    public void setStateText(String text) {
        stateLabel.setText("state: " + text);
    }

    public void updateFromSnapshot(RoundSnapshot s) {
        roundLabel.setText("Round: " + s.round() + "/" + s.totalRounds());
        scoreSumLabel.setText("scoreSum: " + s.scoreSum());
        ledgerLabel.setText("ledger: " + s.ledgerTotal());
        eventsLabel.setText("events: " + s.eventCount());

        boolean invariantOk = s.invariantOk();
        if (invariantOk) {
            invariantLabel.setText("invariant: [ SECURE ]");
            invariantLabel.setForeground(new Color(57, 255, 20)); // Verde neón
        } else {
            invariantLabel.setText("invariant: [ BROKEN ]");
            invariantLabel.setForeground(new Color(255, 49, 49)); // Rojo alerta
        }

        String logLine = String.format(
                "[RND %02d] >> scoreSum: %d | ledger: %d | events: %d | status: %s",
                s.round(), s.scoreSum(), s.ledgerTotal(), s.eventCount(), invariantOk ? "OK" : "FAIL");

        appendLog(logLine);
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
        invariantLabel.setForeground(Color.WHITE);
        log.setText("");
    }

    private ImageIcon loadIcon(String path, int width, int height) {
        try {
            java.net.URL imgURL = getClass().getResource(path);
            if (imgURL != null) {
                ImageIcon icon = new ImageIcon(imgURL);
                Image img = icon.getImage().getScaledInstance(width, height, Image.SCALE_SMOOTH);
                return new ImageIcon(img);
            }
        } catch (Exception e) {
            System.err.println("Icono no encontrado en resources: " + path);
        }
        return null;
    }
}

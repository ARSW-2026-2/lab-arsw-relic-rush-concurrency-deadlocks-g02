package edu.eci.arsw.relicrush.gui;

import edu.eci.arsw.relicrush.model.ForgeStation;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Shows one small card per forge station. Cards are refreshed by a periodic
 * Swing Timer that only READS ForgeStation.occupant()/isFree() -- this is a
 * plain UI refresh, not a coordination mechanism; it never blocks, waits on,
 * or otherwise participates in the game's locking protocol.
 */
public final class StationPanel extends JPanel {

    private static final Color FREE_COLOR = new Color(220, 245, 220);
    private static final Color BUSY_COLOR = new Color(250, 220, 210);

    private final List<JLabel> cards = new ArrayList<>();
    private List<ForgeStation> stations = List.of();

    public StationPanel() {
        setBorder(BorderFactory.createTitledBorder("Forge Stations"));
        setLayout(new GridLayout(0, 4, 8, 8));
    }

    public void initStations(List<ForgeStation> newStations) {
        this.stations = newStations;
        removeAll();
        cards.clear();
        for (ForgeStation station : newStations) {
            JLabel card = new JLabel(station.name(), SwingConstants.CENTER);
            card.setOpaque(true);
            card.setBackground(FREE_COLOR);
            card.setBorder(new LineBorder(Color.GRAY, 1));
            card.setPreferredSize(new Dimension(140, 50));
            cards.add(card);
            add(card);
        }
        revalidate();
        repaint();
    }

    /** Invoked by a Swing Timer every ~150ms while a game is running. */
    public void refresh() {
        for (int i = 0; i < stations.size(); i++) {
            ForgeStation station = stations.get(i);
            JLabel card = cards.get(i);
            String occupant = station.occupant();
            if (occupant == null) {
                card.setText("<html><center>" + station.name() + "<br>free</center></html>");
                card.setBackground(FREE_COLOR);
            } else {
                card.setText("<html><center>" + station.name() + "<br>" + occupant + "</center></html>");
                card.setBackground(BUSY_COLOR);
            }
        }
    }

    public void reset() {
        removeAll();
        cards.clear();
        stations = List.of();
        revalidate();
        repaint();
    }
}

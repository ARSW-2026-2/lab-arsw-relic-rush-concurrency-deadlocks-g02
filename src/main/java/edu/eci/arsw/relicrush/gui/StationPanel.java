package edu.eci.arsw.relicrush.gui;

import edu.eci.arsw.relicrush.model.ForgeStation;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class StationPanel extends JPanel {

    private List<ForgeStation> stations;
    private final List<JPanel> cards = new ArrayList<>();

    private static final Color DARK_BG = new Color(43, 43, 54);
    private static final Color BORDER_COLOR = new Color(100, 100, 110);
    private static final Color NEON_GREEN = new Color(57, 255, 20);
    private static final Color NEON_RED = new Color(255, 49, 49);

    public StationPanel() {
        super(new GridLayout(0, 3, 10, 10));

        setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 2),
                "Forge Stations",
                javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION,
                javax.swing.border.TitledBorder.DEFAULT_POSITION,
                FontLoader.getCustomFont(11f), Color.WHITE
        ));
        setOpaque(false);
    }

    public void initStations(List<ForgeStation> newStations) {
        this.stations = newStations;
        removeAll();
        cards.clear();

        for (ForgeStation station : newStations) {

            JPanel cardPanel = new JPanel(new BorderLayout(0, 5));
            cardPanel.setBackground(DARK_BG);
            cardPanel.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(BORDER_COLOR, 1),
                    BorderFactory.createEmptyBorder(5, 5, 5, 5)
            ));


            JLabel nameLabel = new JLabel(station.name(), SwingConstants.CENTER);
            nameLabel.setFont(FontLoader.getCustomFont(9f));
            nameLabel.setForeground(Color.WHITE);
            cardPanel.add(nameLabel, BorderLayout.NORTH);


            String iconPath = "/default.png";
            String sName = station.name().toLowerCase();


            if (sName.contains("anvil")) iconPath = "/anvil.png";
            else if (sName.contains("lens")) iconPath = "/gem2.png";
            else if (sName.contains("press")) iconPath = "/book.png";
            else if (sName.contains("furnace")) iconPath = "/dragon.png";
            else if (sName.contains("altar")) iconPath = "/moon.png";
            else if (sName.contains("table")) iconPath = "/gem2.png";
            else if (sName.contains("forge")) iconPath = "/dragon.png";

            JLabel iconLabel = new JLabel(loadIcon(iconPath, 45, 45), SwingConstants.CENTER);
            cardPanel.add(iconLabel, BorderLayout.CENTER);

            JLabel statusLabel = new JLabel("[ READY ]", SwingConstants.CENTER);
            statusLabel.setFont(FontLoader.getCustomFont(9f));
            statusLabel.setForeground(NEON_GREEN);
            cardPanel.add(statusLabel, BorderLayout.SOUTH);

            cards.add(cardPanel);
            add(cardPanel);
        }

        revalidate();
        repaint();
    }

    public void refresh() {
        if (stations == null || cards.isEmpty()) return;

        for (int i = 0; i < stations.size(); i++) {
            ForgeStation station = stations.get(i);
            JPanel cardPanel = cards.get(i);

            JLabel statusLabel = (JLabel) cardPanel.getComponent(2);

            String occupant = station.occupant();
            if (occupant == null) {
                statusLabel.setText("[ READY ]");
                statusLabel.setForeground(NEON_GREEN);
            } else {
                statusLabel.setText("[ " + occupant.toUpperCase() + " ]");
                statusLabel.setForeground(NEON_RED);
            }
        }
    }

    public void reset() {
        this.stations = null;

        if (cards != null) {
            cards.clear();
        }

        removeAll();

        revalidate();
        repaint();
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
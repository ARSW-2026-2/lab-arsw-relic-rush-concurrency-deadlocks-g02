package edu.eci.arsw.relicrush.gui;

import edu.eci.arsw.relicrush.game.Adventurer;
import edu.eci.arsw.relicrush.game.RoundSnapshot;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public final class AdventurerPanel extends JPanel {

    private final DefaultTableModel model = new DefaultTableModel(
            new Object[]{"Player", "Name", "Score", "Status"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable table = new JTable(model);

    public AdventurerPanel() {
        super(new BorderLayout());

        setupPanelTheme();
        setupTableConfig();
        setupScrollPane();
    }

    /** Configura el aspecto general y el borde del panel */
    private void setupPanelTheme() {
        setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(100, 100, 110), 2),
                "Adventurers",
                javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION,
                javax.swing.border.TitledBorder.DEFAULT_POSITION,
                FontLoader.getCustomFont(11f), Color.WHITE
        ));
        setOpaque(false);
    }

    /** Configura filas, colores, cabeceras y renderizadores de la tabla */
    private void setupTableConfig() {
        table.setFont(FontLoader.getCustomFont(9f));
        table.setFillsViewportHeight(true);
        table.setBackground(new Color(25, 25, 35, 220));
        table.setForeground(Color.WHITE);
        table.setGridColor(new Color(60, 60, 70));
        table.setRowHeight(30);

        table.getTableHeader().setBackground(new Color(40, 40, 50));
        table.getTableHeader().setForeground(Color.WHITE);
        table.getTableHeader().setFont(FontLoader.getCustomFont(10f));


        table.getColumnModel().getColumn(2).setCellRenderer(new DefaultTableCellRenderer() {
            private final JProgressBar bar = new JProgressBar(0, 50) {
                @Override
                public void updateUI() {
                    super.setUI(new javax.swing.plaf.basic.BasicProgressBarUI() {
                        @Override
                        protected Color getSelectionBackground() {
                            return Color.WHITE;
                        }
                        @Override
                        protected Color getSelectionForeground() {
                            return Color.WHITE;
                        }
                    });
                }
            };
            private final JLabel defaultLabel = new JLabel();

            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus, int row, int column) {
                if (value instanceof Integer score) {
                    bar.setValue(score);
                    bar.setStringPainted(true);
                    bar.setString(score + " Relics");
                    bar.setForeground(new Color(255, 95, 31));
                    bar.setBackground(new Color(43, 43, 54));
                    bar.setBorderPainted(false);
                    bar.setFont(FontLoader.getCustomFont(9f));

                    bar.setForeground(new Color(0, 153, 255));
                    bar.setBackground(new Color(20, 20, 30));
                    bar.setBorderPainted(true);
                    bar.setBorder(BorderFactory.createLineBorder(new Color(60, 60, 70), 1));

                    return bar;
                }
                defaultLabel.setText(value != null ? value.toString() : "");
                defaultLabel.setOpaque(true);
                defaultLabel.setBackground(isSelected ? table.getSelectionBackground() : table.getBackground());
                defaultLabel.setForeground(Color.WHITE);
                return defaultLabel;
            }
        });

        table.getColumnModel().getColumn(3).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

                setHorizontalAlignment(SwingConstants.CENTER);
                setFont(FontLoader.getCustomFont(9f));

                if (value != null) {
                    String status = value.toString().toUpperCase();
                    if (status.contains("RUNNING") || status.contains("FORGING")) {
                        setForeground(new Color(57, 255, 20));
                    } else if (status.contains("BLOCK") || status.contains("WAIT")) {
                        setForeground(new Color(255, 69, 0));
                    } else if (status.contains("FINISH")) {
                        setForeground(new Color(135, 206, 250));
                    } else {
                        setForeground(Color.WHITE);
                    }
                }

                if (!isSelected) {
                    setBackground(new Color(25, 25, 35, 220));
                }
                return c;
            }
        });
    }

    /** Envuelve la tabla en un JScrollPane transparente */
    private void setupScrollPane() {
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.getViewport().setBackground(new Color(25, 25, 35, 220));
        add(scrollPane, BorderLayout.CENTER);
    }

    /** Called once, right after Start, to seed the table with every player. */
    public void initAdventurers(List<Adventurer> adventurers) {
        model.setRowCount(0);
        for (Adventurer a : adventurers) {
            model.addRow(new Object[]{a.playerId(), a.getName(), 0, a.status()});
        }
    }

    /** Called on every round-completed event: refreshes scores. */
    public void updateFromSnapshot(RoundSnapshot snapshot) {
        for (RoundSnapshot.PlayerScore p : snapshot.playerScores()) {
            int row = p.playerId() - 1;
            if (row >= 0 && row < model.getRowCount()) {
                model.setValueAt(p.score(), row, 2);
                model.setValueAt(p.status(), row, 3);
            }
        }
    }

    /** Called by a lightweight UI refresh timer to show live status between rounds. */
    public void refreshLiveStatus(List<Adventurer> adventurers) {
        for (Adventurer a : adventurers) {
            int row = a.playerId() - 1;
            if (row >= 0 && row < model.getRowCount()) {
                model.setValueAt(a.status(), row, 3);
            }
        }
    }

    public void reset() {
        model.setRowCount(0);
    }
}
package edu.eci.arsw.relicrush.gui;

import edu.eci.arsw.relicrush.game.Adventurer;
import edu.eci.arsw.relicrush.game.RoundSnapshot;

import javax.swing.*;
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
        setBorder(BorderFactory.createTitledBorder("Adventurers"));
        table.setFillsViewportHeight(true);
        add(new JScrollPane(table), BorderLayout.CENTER);
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

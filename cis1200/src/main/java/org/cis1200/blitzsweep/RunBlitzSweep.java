package org.cis1200.blitzsweep;

import java.awt.*;
import javax.swing.*;

/**
 * RunBlitzSweep sets up the top-level frame and widgets for the Blitz Sweep game.
 */
public class RunBlitzSweep implements Runnable {
    public void run() {
        // Top-level frame
        final JFrame frame = new JFrame("Blitz Sweep");
        frame.setLocation(300, 300);

        // Status panel at the top
        final JPanel topPanel = new JPanel();
        topPanel.setLayout(new GridLayout(2, 2, 10, 5));
        topPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        frame.add(topPanel, BorderLayout.NORTH);

        final JLabel levelLabel = new JLabel("Level: 1");
        final JLabel scoreLabel = new JLabel("Score: 0");
        final JLabel timerLabel = new JLabel("Time: 02:00");
        final JLabel statusLabel = new JLabel("Mines: 10 | Flags: 0");

        levelLabel.setFont(new Font("Arial", Font.BOLD, 14));
        scoreLabel.setFont(new Font("Arial", Font.BOLD, 14));
        timerLabel.setFont(new Font("Arial", Font.BOLD, 14));
        statusLabel.setFont(new Font("Arial", Font.PLAIN, 12));

        topPanel.add(levelLabel);
        topPanel.add(scoreLabel);
        topPanel.add(timerLabel);
        topPanel.add(statusLabel);

        // Game board
        final BlitzSweepBoard board = new BlitzSweepBoard(
                statusLabel, timerLabel,
                scoreLabel, levelLabel
        );
        frame.add(board, BorderLayout.CENTER);

        // Control panel at the bottom
        final JPanel controlPanel = new JPanel();
        frame.add(controlPanel, BorderLayout.SOUTH);

        // New Game button
        final JButton newGame = new JButton("New Game");
        newGame.addActionListener(e -> board.reset());
        controlPanel.add(newGame);

        // Save button
        final JButton save = new JButton("Save Game");
        save.addActionListener(e -> {
            String filename = JOptionPane.showInputDialog(
                    frame,
                    "Enter filename:",
                    "game_save.txt"
            );
            if (filename != null && !filename.trim().isEmpty()) {
                board.saveGame("files/" + filename);
                JOptionPane.showMessageDialog(frame, "Game saved successfully!");
            }
        });
        controlPanel.add(save);

        // Load button
        final JButton load = new JButton("Load Game");
        load.addActionListener(e -> {
            String filename = JOptionPane.showInputDialog(
                    frame,
                    "Enter filename:",
                    "game_save.txt"
            );
            if (filename != null && !filename.trim().isEmpty()) {
                board.loadGame("files/" + filename);
            }
        });
        controlPanel.add(load);

        // Instructions button
        final JButton instructions = new JButton("Instructions");
        instructions.addActionListener(e -> {
            String message = "BLITZ SWEEP\n\n" +
                    "How to Play:\n" +
                    "- Left-click to reveal a cell\n" +
                    "- Right-click to flag a cell\n" +
                    "- Numbers show adjacent mines\n" +
                    "- Clear all safe cells to win the level\n" +
                    "- Complete 5 levels to win the game!\n\n" +
                    "Rules:\n" +
                    "- Each level has a time limit\n" +
                    "- Hit a mine = reset to Level 1\n" +
                    "- Run out of time = reset to Level 1\n" +
                    "- Boards get bigger and harder each level\n\n" +
                    "Scoring:\n" +
                    "- 10 points per revealed cell\n" +
                    "- 100 x level bonus for completing a level\n" +
                    "- 5 points per second remaining";

            JOptionPane.showMessageDialog(
                    frame, message, "Instructions",
                    JOptionPane.INFORMATION_MESSAGE
            );
        });
        controlPanel.add(instructions);

        // Put the frame on the screen
        frame.pack();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);

        // Start the game
        board.reset();
    }
}

package org.cis1200.blitzsweep;

import java.io.*;
import javax.swing.JOptionPane;

/**
 * GameStateIO - Handles saving and loading game state to/from files.
 *
 * This class demonstrates File I/O by:
 * - Saving complete game state (board, level, score, timer)
 * - Loading saved games
 * - Handling FileNotFoundException with user-friendly error messages
 */
public class GameStateIO {

    /**
     * Saves the current game state to a file.
     *
     * @param model    The game model to save
     * @param filename The file to save to (relative path)
     */
    public static void saveGame(BlitzSweepModel model, String filename) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename))) {
            // Write game metadata
            writer.write("BLITZ_SWEEP_SAVE_V1\n");
            writer.write(model.getCurrentLevel() + "\n");
            writer.write(model.getScore() + "\n");
            writer.write(model.getTimeRemaining() + "\n");
            writer.write(model.getRows() + "\n");
            writer.write(model.getCols() + "\n");
            writer.write(model.getMineCount() + "\n");

            // Write board state
            for (int r = 0; r < model.getRows(); r++) {
                for (int c = 0; c < model.getCols(); c++) {
                    Cell cell = model.getCell(r, c);
                    if (cell != null) {
                        // Format: mine,revealed,flagged,adjacentMines
                        writer.write(
                                (cell.hasMine() ? "1" : "0") + "," +
                                        (cell.isRevealed() ? "1" : "0") + "," +
                                        (cell.isFlagged() ? "1" : "0") + "," +
                                        cell.getAdjacentMines()
                        );
                        if (c < model.getCols() - 1) {
                            writer.write(" ");
                        }
                    }
                }
                writer.write("\n");
            }

        } catch (IOException e) {
            JOptionPane.showMessageDialog(
                    null,
                    "Error saving game: " + e.getMessage(),
                    "Save Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    /**
     * Loads a game state from a file.
     *
     * @param filename The file to load from (relative path)
     * @return The loaded game model, or null if load failed
     */
    public static BlitzSweepModel loadGame(String filename) {
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            String header = reader.readLine();
            if (!"BLITZ_SWEEP_SAVE_V1".equals(header)) {
                throw new IOException("Invalid save file format");
            }

            // Read metadata
            int level = Integer.parseInt(reader.readLine());
            int score = Integer.parseInt(reader.readLine());
            int timeRemaining = Integer.parseInt(reader.readLine());
            int rows = Integer.parseInt(reader.readLine());
            int cols = Integer.parseInt(reader.readLine());
            int mineCount = Integer.parseInt(reader.readLine());

            // Create a new model (we'll reconstruct it manually)
            BlitzSweepModel model = new BlitzSweepModel();

            // Use reflection-like approach: directly set fields via test methods
            // For simplicity, we'll reconstruct by reading the board
            // This is a simplified version - in production you'd use serialization

            // Read board state
            for (int r = 0; r < rows; r++) {
                String line = reader.readLine();
                if (line == null) {
                    throw new IOException("Unexpected end of file");
                }

                String[] cells = line.split(" ");
                for (int c = 0; c < cols && c < cells.length; c++) {
                    String[] parts = cells[c].split(",");
                    if (parts.length == 4) {
                        Cell cell = model.getCell(r, c);
                        if (cell != null) {
                            cell.setMine(parts[0].equals("1"));
                            cell.setRevealed(parts[1].equals("1"));
                            cell.setFlagged(parts[2].equals("1"));
                            cell.setAdjacentMines(Integer.parseInt(parts[3]));
                        }
                    }
                }
            }

            JOptionPane.showMessageDialog(
                    null,
                    "Game loaded successfully!",
                    "Load Complete",
                    JOptionPane.INFORMATION_MESSAGE
            );

            return model;

        } catch (FileNotFoundException e) {
            JOptionPane.showMessageDialog(
                    null,
                    "Save file not found: " + filename + "\n" +
                            "Please check the filename and try again.",
                    "File Not Found",
                    JOptionPane.ERROR_MESSAGE
            );
            return null;
        } catch (IOException | NumberFormatException e) {
            JOptionPane.showMessageDialog(
                    null,
                    "Error loading game: " + e.getMessage() + "\n" +
                            "The save file may be corrupted.",
                    "Load Error",
                    JOptionPane.ERROR_MESSAGE
            );
            return null;
        }
    }

    /**
     * Saves high scores to a file.
     *
     * @param score    The score to save
     * @param filename The file to append to
     */
    public static void saveHighScore(int score, String playerName, String filename) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename, true))) {
            long timestamp = System.currentTimeMillis();
            writer.write(playerName + "," + score + "," + timestamp + "\n");
        } catch (IOException e) {
            JOptionPane.showMessageDialog(
                    null,
                    "Error saving high score: " + e.getMessage(),
                    "Save Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    /**
     * Loads high scores from a file.
     *
     * @param filename The file to load from
     * @return Array of high score entries (name, score, timestamp)
     */
    public static String[][] loadHighScores(String filename) {
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            // Count lines first
            int count = 0;
            while (reader.readLine() != null) {
                count++;
            }

            // Read scores
            String[][] scores = new String[count][3];
            try (BufferedReader reader2 = new BufferedReader(new FileReader(filename))) {
                int index = 0;
                String line;
                while ((line = reader2.readLine()) != null && index < count) {
                    String[] parts = line.split(",");
                    if (parts.length == 3) {
                        scores[index] = parts;
                        index++;
                    }
                }
            }

            return scores;

        } catch (FileNotFoundException e) {
            // No high scores file yet - return empty array
            return new String[0][0];
        } catch (IOException e) {
            JOptionPane.showMessageDialog(
                    null,
                    "Error loading high scores: " + e.getMessage(),
                    "Load Error",
                    JOptionPane.ERROR_MESSAGE
            );
            return new String[0][0];
        }
    }
}

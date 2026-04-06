package org.cis1200.blitzsweep;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

/**
 * BlitzSweepBoard - The game board for Blitz Sweep
 *
 * This class acts as both the controller (with MouseListener) and the view
 * (with its paintComponent method) in the MVC framework.
 *
 * It displays the game board and handles user input (left-click to reveal,
 * right-click to flag).
 */
public class BlitzSweepBoard extends JPanel {
    private BlitzSweepModel model;
    private JLabel statusLabel;
    private JLabel timerLabel;
    private JLabel scoreLabel;
    private JLabel levelLabel;
    private Timer gameTimer;

    // Cheat mode flags
    private boolean showMineCheat = false;
    private boolean invincibilityMode = false;

    // Animation and effects
    private java.util.List<Particle> particles = new java.util.ArrayList<>();
    private Timer animationTimer;
    private int hoverRow = -1;
    private int hoverCol = -1;
    private long lastRevealTime = 0;
    private int comboStreak = 0;
    private int maxCombo = 0;

    private static final int CELL_SIZE = 30;
    private static final int MAX_BOARD_SIZE = 16; // Maximum grid size
    private static final int TIMER_INTERVAL = 1000; // 1 second

    // Enhanced Colors with gradients
    private static final Color COLOR_UNREVEALED = new Color(100, 120, 160);
    private static final Color COLOR_UNREVEALED_LIGHT = new Color(140, 160, 200);
    private static final Color COLOR_REVEALED = new Color(230, 235, 245);
    private static final Color COLOR_FLAGGED = new Color(255, 180, 60);
    private static final Color COLOR_FLAGGED_DARK = new Color(230, 140, 30);
    private static final Color COLOR_MINE = new Color(220, 50, 50);
    private static final Color COLOR_HOVER = new Color(180, 200, 255, 100);

    /**
     * Initializes the game board.
     */
    public BlitzSweepBoard(
            JLabel statusLabel, JLabel timerLabel, JLabel scoreLabel,
            JLabel levelLabel
    ) {
        setBorder(BorderFactory.createLineBorder(Color.BLACK));
        setFocusable(true);

        this.model = new BlitzSweepModel();
        this.statusLabel = statusLabel;
        this.timerLabel = timerLabel;
        this.scoreLabel = scoreLabel;
        this.levelLabel = levelLabel;

        // Timer that ticks every second
        gameTimer = new Timer(TIMER_INTERVAL, e -> tick());

        // Mouse listener for cell clicks
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (model.isGameOver()) {
                    return;
                }

                int cellSize = CELL_SIZE;
                int col = e.getX() / cellSize;
                int row = e.getY() / cellSize;

                // Left click: reveal cell
                if (SwingUtilities.isLeftMouseButton(e)) {
                    Cell cell = model.getCell(row, col);
                    boolean wasRevealed = cell != null && !cell.isRevealed();

                    model.revealCell(row, col);

                    // Track combo streak for quick reveals
                    if (wasRevealed && cell != null && !cell.hasMine()) {
                        long currentTime = System.currentTimeMillis();
                        if (currentTime - lastRevealTime < 500) { // Within 0.5 seconds
                            comboStreak++;
                            if (comboStreak > maxCombo) {
                                maxCombo = comboStreak;
                            }
                            // Bonus points for combo
                            if (comboStreak > 3) {
                                model.addScore(comboStreak * 5);
                            }
                        } else {
                            comboStreak = 1;
                        }
                        lastRevealTime = currentTime;

                        // Add sparkle particles on safe reveal
                        addSparkles(col * CELL_SIZE + CELL_SIZE / 2,
                                    row * CELL_SIZE + CELL_SIZE / 2);
                    }

                    // Check if won the level
                    if (model.isGameWon() && model.getCurrentLevel() < 5) {
                        // Win celebration!
                        createWinCelebration();
                        model.advanceLevel();
                    }
                } else if (SwingUtilities.isRightMouseButton(e)) {
                    // Right click: toggle flag
                    model.toggleFlag(row, col);
                }

                updateLabels();
                repaint();

                // Handle game over (unless invincibility is on)
                if (model.isGameOver() && !invincibilityMode) {
                    gameTimer.stop();
                    if (model.isGameWon()) {
                        if (model.getCurrentLevel() >= 5) {
                            statusLabel.setText("YOU WIN! All levels complete!");
                            createWinCelebration();
                        }
                    } else {
                        statusLabel.setText("Game Over! Resetting to Level 1...");
                        // Create explosion effect at mine location
                        createExplosion(col * CELL_SIZE + CELL_SIZE / 2,
                                       row * CELL_SIZE + CELL_SIZE / 2);
                        // Reset after a delay
                        Timer resetTimer = new Timer(2000, event -> reset());
                        resetTimer.setRepeats(false);
                        resetTimer.start();
                    }
                } else if (model.isGameOver() && invincibilityMode) {
                    // Invincibility: don't actually end the game, just show message
                    statusLabel.setText("INVINCIBLE: Mine hit but you're protected!");
                }
            }
        });

        // Keyboard listener for cheat codes (DEV MODE)
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                // Cheat: M = Show all mines (reveal their locations visually)
                if (e.getKeyCode() == KeyEvent.VK_M) {
                    showMineCheat = !showMineCheat;
                    statusLabel.setText(
                            showMineCheat
                                    ? "CHEAT: Mines revealed!"
                                    : "Mines: " + model.getMineCount()
                    );
                    repaint();
                }

                // Cheat: T = Add 60 seconds to timer
                if (e.getKeyCode() == KeyEvent.VK_T) {
                    addTime(60);
                    statusLabel.setText("CHEAT: +60 seconds!");
                    updateLabels();
                }

                // Cheat: N = Skip to next level
                if (e.getKeyCode() == KeyEvent.VK_N) {
                    if (model.getCurrentLevel() < 5) {
                        model.advanceLevel();
                        statusLabel.setText("CHEAT: Skipped to level " + model.getCurrentLevel());
                        updateLabels();
                        repaint();
                    }
                }

                // Cheat: W = Auto-win current level
                if (e.getKeyCode() == KeyEvent.VK_W) {
                    autoWinLevel();
                    statusLabel.setText("CHEAT: Auto-completed level!");
                }

                // Cheat: I = Toggle invincibility (can't lose)
                if (e.getKeyCode() == KeyEvent.VK_I) {
                    invincibilityMode = !invincibilityMode;
                    statusLabel.setText(
                            invincibilityMode ? "CHEAT: Invincibility ON!" : "Invincibility OFF"
                    );
                }

                // Cheat: S = Add 1000 score
                if (e.getKeyCode() == KeyEvent.VK_S) {
                    addScore(1000);
                    statusLabel.setText("CHEAT: +1000 score!");
                    updateLabels();
                }
            }
        });

        // Mouse motion listener for hover effects
        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                int newHoverRow = e.getY() / CELL_SIZE;
                int newHoverCol = e.getX() / CELL_SIZE;

                if (newHoverRow != hoverRow || newHoverCol != hoverCol) {
                    hoverRow = newHoverRow;
                    hoverCol = newHoverCol;
                    repaint();
                }
            }
        });

        // Animation timer for particles and effects (60 FPS)
        animationTimer = new Timer(16, e -> {
            updateParticles();
            repaint();
        });
        animationTimer.start();
    }

    /**
     * Starts or resets the game.
     */
    public void reset() {
        model.reset();
        comboStreak = 0;
        particles.clear();
        updateLabels();
        repaint();
        gameTimer.start();
        requestFocusInWindow();
    }

    /**
     * Called every second by the timer.
     */
    private void tick() {
        if (!model.isGameOver()) {
            model.decrementTimer();
            updateLabels();

            if (model.isGameOver()) {
                gameTimer.stop();
                statusLabel.setText("Time's up! Resetting to Level 1...");
                // Reset after a delay
                Timer resetTimer = new Timer(2000, e -> reset());
                resetTimer.setRepeats(false);
                resetTimer.start();
            }

            repaint();
        }
    }

    /**
     * Updates all the status labels.
     */
    private void updateLabels() {
        levelLabel.setText("Level: " + model.getCurrentLevel());
        scoreLabel.setText("Score: " + model.getScore());
        timerLabel.setText("Time: " + formatTime(model.getTimeRemaining()));

        if (!model.isGameOver()) {
            int flagged = model.getFlaggedCount();
            int mines = model.getMineCount();
            String comboText = comboStreak > 3 ? " | COMBO x" + comboStreak + "!" : "";
            statusLabel.setText("Mines: " + mines + " | Flags: " + flagged + comboText);
        }
    }

    /**
     * Formats time in MM:SS format.
     */
    private String formatTime(int seconds) {
        int minutes = seconds / 60;
        int secs = seconds % 60;
        return String.format("%02d:%02d", minutes, secs);
    }

    /**
     * CHEAT: Adds time to the timer.
     */
    private void addTime(int seconds) {
        model.addTime(seconds);
    }

    /**
     * CHEAT: Adds score to the player.
     */
    private void addScore(int points) {
        model.addScore(points);
    }

    /**
     * CHEAT: Auto-completes the current level by revealing all non-mine cells.
     */
    private void autoWinLevel() {
        for (int r = 0; r < model.getRows(); r++) {
            for (int c = 0; c < model.getCols(); c++) {
                Cell cell = model.getCell(r, c);
                if (!cell.hasMine() && !cell.isRevealed()) {
                    model.revealCell(r, c);
                }
            }
        }
        updateLabels();
        repaint();
    }

    /**
     * Draws the game board.
     */
    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        int rows = model.getRows();
        int cols = model.getCols();

        // Draw cells
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                drawCell(g, r, c);
            }
        }

        // Draw grid lines
        g.setColor(Color.DARK_GRAY);
        for (int r = 0; r <= rows; r++) {
            g.drawLine(0, r * CELL_SIZE, cols * CELL_SIZE, r * CELL_SIZE);
        }
        for (int c = 0; c <= cols; c++) {
            g.drawLine(c * CELL_SIZE, 0, c * CELL_SIZE, rows * CELL_SIZE);
        }

        // Draw visual timer progress bar at bottom
        if (!model.isGameOver()) {
            int barHeight = 5;
            int barY = rows * CELL_SIZE + 2;
            int barWidth = cols * CELL_SIZE;

            // Background
            g.setColor(new Color(100, 100, 100));
            g.fillRect(0, barY, barWidth, barHeight);

            // Progress (time remaining)
            float timePercent = model.getTimeRemaining() / 120.0f; // Assuming max 120 sec
            int progressWidth = (int) (barWidth * timePercent);

            // Color based on time remaining (green -> yellow -> red)
            Color barColor;
            if (timePercent > 0.5f) {
                barColor = new Color(100, 220, 100);
            } else if (timePercent > 0.25f) {
                barColor = new Color(255, 200, 50);
            } else {
                barColor = new Color(255, 80, 80);
            }

            g.setColor(barColor);
            g.fillRect(0, barY, progressWidth, barHeight);
        }

        // Draw particles (explosions, sparkles, celebration)
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        for (Particle p : particles) {
            p.draw(g2d);
        }
    }

    /**
     * Draws a single cell.
     */
    private void drawCell(Graphics g, int row, int col) {
        Cell cell = model.getCell(row, col);
        if (cell == null) {
            return;
        }

        int x = col * CELL_SIZE;
        int y = row * CELL_SIZE;

        // Determine cell color
        if (cell.isRevealed()) {
            if (cell.hasMine()) {
                // Show mine (red background)
                g.setColor(COLOR_MINE);
                g.fillRect(x + 1, y + 1, CELL_SIZE - 2, CELL_SIZE - 2);

                // Draw mine symbol
                g.setColor(Color.BLACK);
                g.fillOval(x + 8, y + 8, 14, 14);
            } else {
                // Revealed safe cell
                g.setColor(COLOR_REVEALED);
                g.fillRect(x + 1, y + 1, CELL_SIZE - 2, CELL_SIZE - 2);

                // Draw number if adjacent mines > 0
                int adjacentMines = cell.getAdjacentMines();
                if (adjacentMines > 0) {
                    g.setColor(getNumberColor(adjacentMines));
                    g.setFont(new Font("Arial", Font.BOLD, 16));
                    FontMetrics fm = g.getFontMetrics();
                    String num = String.valueOf(adjacentMines);
                    int textX = x + (CELL_SIZE - fm.stringWidth(num)) / 2;
                    int textY = y + ((CELL_SIZE - fm.getHeight()) / 2) + fm.getAscent();
                    g.drawString(num, textX, textY);
                }
            }
        } else if (cell.isFlagged()) {
            // Flagged cell with gradient
            Graphics2D g2d = (Graphics2D) g;
            GradientPaint gradient = new GradientPaint(
                    x, y, COLOR_FLAGGED,
                    x + CELL_SIZE, y + CELL_SIZE, COLOR_FLAGGED_DARK
            );
            g2d.setPaint(gradient);
            g2d.fillRect(x + 1, y + 1, CELL_SIZE - 2, CELL_SIZE - 2);

            // Draw flag emoji or symbol
            g.setColor(Color.RED);
            g.setFont(new Font("Arial", Font.BOLD, 18));
            g.drawString("🚩", x + 7, y + 22);

            // Fallback if emoji doesn't work
            if (!g.getFont().canDisplay('🚩')) {
                int[] flagX = { x + 10, x + 10, x + 22 };
                int[] flagY = { y + 8, y + 18, y + 13 };
                g.fillPolygon(flagX, flagY, 3);
                g.setColor(Color.BLACK);
                g.drawLine(x + 10, y + 8, x + 10, y + 24);
            }
        } else {
            // Unrevealed cell with gradient
            Graphics2D g2d = (Graphics2D) g;
            GradientPaint gradient = new GradientPaint(
                    x, y, COLOR_UNREVEALED_LIGHT,
                    x + CELL_SIZE, y + CELL_SIZE, COLOR_UNREVEALED
            );
            g2d.setPaint(gradient);
            g2d.fillRect(x + 1, y + 1, CELL_SIZE - 2, CELL_SIZE - 2);

            // Hover effect
            if (row == hoverRow && col == hoverCol && !model.isGameOver()) {
                g.setColor(COLOR_HOVER);
                g.fillRect(x + 1, y + 1, CELL_SIZE - 2, CELL_SIZE - 2);
            }

            // CHEAT: Show mine indicator if cheat is enabled
            if (showMineCheat && cell.hasMine()) {
                g.setColor(new Color(255, 150, 150, 150)); // Light red tint
                g.fillRect(x + 1, y + 1, CELL_SIZE - 2, CELL_SIZE - 2);
                g.setColor(Color.RED);
                g.setFont(new Font("Arial", Font.BOLD, 10));
                g.drawString("💣", x + 8, y + 20);
            }

            // Add 3D effect
            g.setColor(new Color(255, 255, 255, 100));
            g.drawLine(x + 1, y + 1, x + CELL_SIZE - 2, y + 1);
            g.drawLine(x + 1, y + 1, x + 1, y + CELL_SIZE - 2);
            g.setColor(new Color(0, 0, 0, 60));
            g.drawLine(x + CELL_SIZE - 2, y + 1, x + CELL_SIZE - 2, y + CELL_SIZE - 2);
            g.drawLine(x + 1, y + CELL_SIZE - 2, x + CELL_SIZE - 2, y + CELL_SIZE - 2);
        }
    }

    /**
     * Returns the color for a number based on adjacent mine count.
     */
    private Color getNumberColor(int num) {
        switch (num) {
            case 1:
                return Color.BLUE;
            case 2:
                return new Color(0, 128, 0); // Green
            case 3:
                return Color.RED;
            case 4:
                return new Color(0, 0, 128); // Dark blue
            case 5:
                return new Color(128, 0, 0); // Dark red
            case 6:
                return new Color(0, 128, 128); // Teal
            case 7:
                return Color.BLACK;
            case 8:
                return Color.GRAY;
            default:
                return Color.BLACK;
        }
    }

    /**
     * Returns the preferred size of the game board.
     */
    @Override
    public Dimension getPreferredSize() {
        int size = MAX_BOARD_SIZE * CELL_SIZE;
        return new Dimension(size, size);
    }

    /**
     * Saves the current game state to a file.
     */
    public void saveGame(String filename) {
        GameStateIO.saveGame(model, filename);
    }

    /**
     * Loads a game state from a file.
     */
    public void loadGame(String filename) {
        BlitzSweepModel loadedModel = GameStateIO.loadGame(filename);
        if (loadedModel != null) {
            this.model = loadedModel;
            updateLabels();
            repaint();
            if (!model.isGameOver()) {
                gameTimer.start();
            }
        }
    }

    // ==================== PARTICLE SYSTEM ====================

    /**
     * Updates all particles (movement, fade, removal).
     */
    private void updateParticles() {
        particles.removeIf(p -> !p.update());
    }

    /**
     * Creates explosion particles when hitting a mine.
     */
    private void createExplosion(int x, int y) {
        for (int i = 0; i < 30; i++) {
            double angle = Math.random() * Math.PI * 2;
            double speed = 2 + Math.random() * 4;
            double vx = Math.cos(angle) * speed;
            double vy = Math.sin(angle) * speed;
            Color color = new Color(255, (int) (100 + Math.random() * 100), 0);
            particles.add(new Particle(x, y, vx, vy, color, 40));
        }
    }

    /**
     * Creates sparkle particles when revealing safe cells.
     */
    private void addSparkles(int x, int y) {
        for (int i = 0; i < 5; i++) {
            double angle = Math.random() * Math.PI * 2;
            double speed = 0.5 + Math.random() * 1.5;
            double vx = Math.cos(angle) * speed;
            double vy = Math.sin(angle) * speed;
            Color color = new Color(
                    (int) (200 + Math.random() * 55),
                    (int) (200 + Math.random() * 55),
                    (int) (100 + Math.random() * 155)
            );
            particles.add(new Particle(x, y, vx, vy, color, 20));
        }
    }

    /**
     * Creates celebration particles when winning.
     */
    private void createWinCelebration() {
        int centerX = (model.getCols() * CELL_SIZE) / 2;
        int centerY = (model.getRows() * CELL_SIZE) / 2;

        for (int i = 0; i < 100; i++) {
            double angle = Math.random() * Math.PI * 2;
            double speed = 2 + Math.random() * 6;
            double vx = Math.cos(angle) * speed;
            double vy = Math.sin(angle) * speed;
            Color[] colors = {
                new Color(255, 100, 100),
                new Color(100, 255, 100),
                new Color(100, 100, 255),
                new Color(255, 255, 100),
                new Color(255, 100, 255)
            };
            Color color = colors[(int) (Math.random() * colors.length)];
            particles.add(new Particle(centerX, centerY, vx, vy, color, 60));
        }
    }

    // ==================== PARTICLE CLASS ====================

    /**
     * Represents a single particle for visual effects.
     */
    private static class Particle {
        private double x, y;
        private double vx, vy;
        private Color color;
        private int life;
        private int maxLife;

        public Particle(double x, double y, double vx, double vy, Color color, int life) {
            this.x = x;
            this.y = y;
            this.vx = vx;
            this.vy = vy;
            this.color = color;
            this.life = life;
            this.maxLife = life;
        }

        /**
         * Updates particle position and life. Returns false if particle is dead.
         */
        public boolean update() {
            x += vx;
            y += vy;
            vy += 0.2; // Gravity
            life--;
            return life > 0;
        }

        /**
         * Draws the particle with fading alpha.
         */
        public void draw(Graphics2D g) {
            float alpha = (float) life / maxLife;
            Color fadeColor = new Color(
                    color.getRed(),
                    color.getGreen(),
                    color.getBlue(),
                    (int) (alpha * 255)
            );
            g.setColor(fadeColor);
            int size = 3 + (int) (alpha * 3);
            g.fillOval((int) x - size / 2, (int) y - size / 2, size, size);
        }
    }
}

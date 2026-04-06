package org.cis1200.blitzsweep;

import java.util.Random;

/**
 * BlitzSweepModel - The game logic for Blitz Sweep
 *
 * This class is the model in the MVC framework. It handles all game logic
 * independently of the GUI, including:
 * - Board management with 2D arrays
 * - Recursive flood-fill algorithm
 * - Level progression
 * - Timer and score tracking
 * - Win/lose conditions
 *
 * This model is completely independent of the view and controller,
 * allowing for comprehensive JUnit testing without GUI dependencies.
 */
public class BlitzSweepModel {
    private Cell[][] board;
    private int rows;
    private int cols;
    private int currentLevel;
    private int score;
    private int timeRemaining; // in seconds
    private int mineCount;
    private int revealedCount;
    private boolean gameOver;
    private boolean gameWon;
    private boolean firstClick;
    private Random random;

    // Level configurations: [level][0]=rows, [1]=cols, [2]=mines, [3]=time
    private static final int[][] LEVEL_CONFIG = {
            { 8, 8, 10, 120 },   // Level 1: 8x8, 10 mines, 120 seconds
            { 10, 10, 15, 100 }, // Level 2: 10x10, 15 mines, 100 seconds
            { 12, 12, 25, 90 },  // Level 3: 12x12, 25 mines, 90 seconds
            { 14, 14, 35, 80 },  // Level 4: 14x14, 35 mines, 80 seconds
            { 16, 16, 50, 70 }   // Level 5: 16x16, 50 mines, 70 seconds
    };

    /**
     * Constructor initializes the game at level 1.
     */
    public BlitzSweepModel() {
        this.random = new Random();
        reset();
    }

    /**
     * Resets the game to level 1 with initial state.
     */
    public void reset() {
        this.currentLevel = 1;
        this.score = 0;
        initializeLevel();
    }

    /**
     * Initializes the current level with appropriate board size and mines.
     */
    private void initializeLevel() {
        int levelIndex = currentLevel - 1;
        this.rows = LEVEL_CONFIG[levelIndex][0];
        this.cols = LEVEL_CONFIG[levelIndex][1];
        this.mineCount = LEVEL_CONFIG[levelIndex][2];
        this.timeRemaining = LEVEL_CONFIG[levelIndex][3];
        this.revealedCount = 0;
        this.gameOver = false;
        this.gameWon = false;
        this.firstClick = true;

        // Initialize board
        board = new Cell[rows][cols];
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                board[r][c] = new Cell();
            }
        }

        // Don't place mines yet - wait for first click to ensure it's safe
    }

    /**
     * Places mines randomly on the board.
     */
    private void placeMines() {
        int minesPlaced = 0;
        while (minesPlaced < mineCount) {
            int r = random.nextInt(rows);
            int c = random.nextInt(cols);

            if (!board[r][c].hasMine()) {
                board[r][c].setMine(true);
                minesPlaced++;
            }
        }
    }

    /**
     * Places mines randomly while avoiding a 3x3 area around the first click.
     * This ensures the first click is always safe and reveals a good area.
     */
    private void placeMinesAvoidingArea(int clickRow, int clickCol) {
        int minesPlaced = 0;
        while (minesPlaced < mineCount) {
            int r = random.nextInt(rows);
            int c = random.nextInt(cols);

            // Check if this position is in the 3x3 area around the first click
            boolean inSafeZone = Math.abs(r - clickRow) <= 1 && Math.abs(c - clickCol) <= 1;

            if (!board[r][c].hasMine() && !inSafeZone) {
                board[r][c].setMine(true);
                minesPlaced++;
            }
        }
    }

    /**
     * Calculates the adjacent mine count for all cells.
     */
    private void calculateAdjacentMines() {
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                if (!board[r][c].hasMine()) {
                    int count = countAdjacentMines(r, c);
                    board[r][c].setAdjacentMines(count);
                }
            }
        }
    }

    /**
     * Counts the number of mines adjacent to a given cell.
     */
    private int countAdjacentMines(int row, int col) {
        int count = 0;
        for (int dr = -1; dr <= 1; dr++) {
            for (int dc = -1; dc <= 1; dc++) {
                if (dr == 0 && dc == 0) {
                    continue;
                }
                int newRow = row + dr;
                int newCol = col + dc;

                if (isValidCell(newRow, newCol) && board[newRow][newCol].hasMine()) {
                    count++;
                }
            }
        }
        return count;
    }

    /**
     * Checks if a cell position is valid (within bounds).
     */
    private boolean isValidCell(int row, int col) {
        return row >= 0 && row < rows && col >= 0 && col < cols;
    }

    /**
     * Reveals a cell at the given position.
     * Returns true if successful, false if game over or invalid move.
     *
     * If the cell has no adjacent mines, recursively reveals neighbors (flood-fill).
     * If the cell has a mine, the game is lost and resets to level 1.
     */
    public boolean revealCell(int row, int col) {
        // Check bounds
        if (!isValidCell(row, col)) {
            return false;
        }

        Cell cell = board[row][col];

        // Cannot reveal flagged or already revealed cells
        if (cell.isFlagged() || cell.isRevealed() || gameOver) {
            return false;
        }

        // On first click, place mines ensuring this area is safe
        if (firstClick) {
            placeMinesAvoidingArea(row, col);
            calculateAdjacentMines();
            firstClick = false;
        }

        // Reveal the cell
        cell.setRevealed(true);
        revealedCount++;

        // Check if hit a mine (should never happen on first click)
        if (cell.hasMine()) {
            gameOver = true;
            gameWon = false;
            // Reset to level 1 on mine hit
            currentLevel = 1;
            return true;
        }

        // Add score for revealing a safe cell
        score += 10;

        // Recursive flood-fill if no adjacent mines
        if (cell.getAdjacentMines() == 0) {
            floodFill(row, col);
        }

        // Check win condition
        checkWinCondition();

        return true;
    }

    /**
     * Recursive flood-fill algorithm to reveal connected empty cells.
     * Base cases:
     * - Out of bounds
     * - Already revealed
     * - Flagged
     * - Has a mine
     * - Has adjacent mines (reveals but doesn't recurse further)
     */
    private void floodFill(int row, int col) {
        // Check all 8 adjacent cells
        for (int dr = -1; dr <= 1; dr++) {
            for (int dc = -1; dc <= 1; dc++) {
                if (dr == 0 && dc == 0) {
                    continue;
                }
                int newRow = row + dr;
                int newCol = col + dc;

                // Base cases
                if (!isValidCell(newRow, newCol)) {
                    continue;
                }

                Cell neighbor = board[newRow][newCol];

                if (neighbor.isRevealed() || neighbor.isFlagged() || neighbor.hasMine()) {
                    continue;
                }

                // Reveal the neighbor
                neighbor.setRevealed(true);
                revealedCount++;
                score += 10;

                // Recursively flood-fill if neighbor has no adjacent mines
                if (neighbor.getAdjacentMines() == 0) {
                    floodFill(newRow, newCol);
                }
            }
        }
    }

    /**
     * Toggles the flag status of a cell.
     * Returns true if successful, false otherwise.
     */
    public boolean toggleFlag(int row, int col) {
        if (!isValidCell(row, col) || gameOver) {
            return false;
        }

        Cell cell = board[row][col];

        // Cannot flag revealed cells
        if (cell.isRevealed()) {
            return false;
        }

        cell.setFlagged(!cell.isFlagged());
        return true;
    }

    /**
     * Checks if the win condition is met.
     * Win condition: all non-mine cells are revealed.
     */
    private void checkWinCondition() {
        int totalCells = rows * cols;
        int nonMineCells = totalCells - mineCount;

        if (revealedCount >= nonMineCells) {
            gameWon = true;
            gameOver = true;

            // Bonus points for completing level
            score += 100 * currentLevel;

            // Add time bonus
            score += timeRemaining * 5;
        }
    }

    /**
     * Advances to the next level.
     * Returns true if advanced, false if already at max level.
     */
    public boolean advanceLevel() {
        if (currentLevel >= LEVEL_CONFIG.length) {
            return false;
        }

        currentLevel++;
        initializeLevel();
        return true;
    }

    /**
     * Decrements the timer by 1 second.
     * If time runs out, game is lost and resets to level 1.
     */
    public void decrementTimer() {
        if (gameOver) {
            return;
        }

        timeRemaining--;

        if (timeRemaining <= 0) {
            gameOver = true;
            gameWon = false;
            currentLevel = 1;
        }
    }

    /**
     * Returns the cell at the given position, or null if invalid.
     */
    public Cell getCell(int row, int col) {
        if (!isValidCell(row, col)) {
            return null;
        }
        return board[row][col];
    }

    /**
     * Returns the number of rows in the current board.
     */
    public int getRows() {
        return rows;
    }

    /**
     * Returns the number of columns in the current board.
     */
    public int getCols() {
        return cols;
    }

    /**
     * Returns the current level (1-5).
     */
    public int getCurrentLevel() {
        return currentLevel;
    }

    /**
     * Returns the current score.
     */
    public int getScore() {
        return score;
    }

    /**
     * Returns the time remaining in seconds.
     */
    public int getTimeRemaining() {
        return timeRemaining;
    }

    /**
     * Returns the total number of mines on the board.
     */
    public int getMineCount() {
        return mineCount;
    }

    /**
     * Returns the number of flagged cells.
     */
    public int getFlaggedCount() {
        int count = 0;
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                if (board[r][c].isFlagged()) {
                    count++;
                }
            }
        }
        return count;
    }

    /**
     * Returns whether the game is over.
     */
    public boolean isGameOver() {
        return gameOver;
    }

    /**
     * Returns whether the game was won.
     */
    public boolean isGameWon() {
        return gameWon;
    }

    /**
     * CHEAT: Adds time to the timer (for dev/testing).
     */
    public void addTime(int seconds) {
        this.timeRemaining += seconds;
    }

    /**
     * CHEAT: Adds score (for dev/testing).
     */
    public void addScore(int points) {
        this.score += points;
    }

    /**
     * For testing: sets a specific seed for the random number generator.
     */
    public void setRandomSeed(long seed) {
        this.random = new Random(seed);
    }

    /**
     * For testing: manually place a mine at a specific position.
     */
    public void setMineAt(int row, int col) {
        if (isValidCell(row, col)) {
            board[row][col].setMine(true);
        }
    }

    /**
     * For testing: recalculate adjacent mine counts after manual mine placement.
     */
    public void recalculateAdjacentMines() {
        calculateAdjacentMines();
    }
}

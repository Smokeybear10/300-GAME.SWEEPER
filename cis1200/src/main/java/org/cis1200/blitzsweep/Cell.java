package org.cis1200.blitzsweep;

/**
 * Represents a single cell in the Blitz Sweep game board.
 * Each cell can contain a mine, be revealed, flagged, and knows its adjacent mine count.
 */
public class Cell {
    private boolean hasMine;
    private boolean isRevealed;
    private boolean isFlagged;
    private int adjacentMines;

    /**
     * Constructor creates a new cell with default values.
     */
    public Cell() {
        this.hasMine = false;
        this.isRevealed = false;
        this.isFlagged = false;
        this.adjacentMines = 0;
    }

    /**
     * Returns whether this cell contains a mine.
     */
    public boolean hasMine() {
        return hasMine;
    }

    /**
     * Sets whether this cell contains a mine.
     */
    public void setMine(boolean hasMine) {
        this.hasMine = hasMine;
    }

    /**
     * Returns whether this cell has been revealed.
     */
    public boolean isRevealed() {
        return isRevealed;
    }

    /**
     * Sets whether this cell has been revealed.
     */
    public void setRevealed(boolean revealed) {
        this.isRevealed = revealed;
    }

    /**
     * Returns whether this cell is flagged.
     */
    public boolean isFlagged() {
        return isFlagged;
    }

    /**
     * Sets whether this cell is flagged.
     */
    public void setFlagged(boolean flagged) {
        this.isFlagged = flagged;
    }

    /**
     * Returns the count of adjacent mines (0-8).
     */
    public int getAdjacentMines() {
        return adjacentMines;
    }

    /**
     * Sets the count of adjacent mines.
     */
    public void setAdjacentMines(int count) {
        this.adjacentMines = count;
    }

    /**
     * Resets the cell to its default state.
     */
    public void reset() {
        this.hasMine = false;
        this.isRevealed = false;
        this.isFlagged = false;
        this.adjacentMines = 0;
    }
}

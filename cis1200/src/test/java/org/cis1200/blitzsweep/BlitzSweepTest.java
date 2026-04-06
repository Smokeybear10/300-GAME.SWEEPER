package org.cis1200.blitzsweep;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * JUnit tests for Blitz Sweep game.
 *
 * These tests verify the game logic independently of the GUI, demonstrating
 * the testable component concept.
 */
public class BlitzSweepTest {
    private BlitzSweepModel model;

    @BeforeEach
    public void setUp() {
        model = new BlitzSweepModel();
        // Use a fixed seed for predictable testing
        model.setRandomSeed(12345L);
        model.reset();
    }

    /**
     * Test 1: Initial game state
     */
    @Test
    public void testInitialState() {
        assertEquals(1, model.getCurrentLevel());
        assertEquals(0, model.getScore());
        assertEquals(120, model.getTimeRemaining()); // Level 1 has 120 seconds
        assertEquals(8, model.getRows()); // Level 1 is 8x8
        assertEquals(8, model.getCols());
        assertEquals(10, model.getMineCount()); // Level 1 has 10 mines
        assertFalse(model.isGameOver());
        assertFalse(model.isGameWon());
    }

    /**
     * Test 2: Cell creation and initial state
     */
    @Test
    public void testCellInitialState() {
        Cell cell = new Cell();
        assertFalse(cell.hasMine());
        assertFalse(cell.isRevealed());
        assertFalse(cell.isFlagged());
        assertEquals(0, cell.getAdjacentMines());
    }

    /**
     * Test 3: Revealing a safe cell increases score
     */
    @Test
    public void testRevealSafeCellIncreasesScore() {
        int initialScore = model.getScore();

        // Find a cell without a mine
        boolean revealed = false;
        for (int r = 0; r < model.getRows() && !revealed; r++) {
            for (int c = 0; c < model.getCols() && !revealed; c++) {
                Cell cell = model.getCell(r, c);
                if (!cell.hasMine()) {
                    model.revealCell(r, c);
                    revealed = true;
                }
            }
        }

        assertTrue(model.getScore() > initialScore);
    }

    /**
     * Test 4: Flagging a cell
     */
    @Test
    public void testFlagging() {
        model.toggleFlag(0, 0);
        Cell cell = model.getCell(0, 0);
        assertTrue(cell.isFlagged());

        // Toggle again to unflag
        model.toggleFlag(0, 0);
        assertFalse(cell.isFlagged());
    }

    /**
     * Test 5: Cannot flag a revealed cell
     */
    @Test
    public void testCannotFlagRevealedCell() {
        // Reveal a cell
        Cell cell = model.getCell(0, 0);
        if (!cell.hasMine()) {
            model.revealCell(0, 0);
            assertTrue(cell.isRevealed());

            // Try to flag it
            model.toggleFlag(0, 0);
            assertFalse(cell.isFlagged()); // Should remain unflagged
        }
    }

    /**
     * Test 6: Timer decrements correctly
     */
    @Test
    public void testTimerDecrement() {
        int initialTime = model.getTimeRemaining();
        model.decrementTimer();
        assertEquals(initialTime - 1, model.getTimeRemaining());
    }

    /**
     * Test 7: Game over when time runs out
     */
    @Test
    public void testTimeRunsOut() {
        // Decrement timer to 0
        for (int i = 0; i <= 120; i++) {
            model.decrementTimer();
        }

        assertTrue(model.isGameOver());
        assertFalse(model.isGameWon());
        assertEquals(1, model.getCurrentLevel()); // Should reset to level 1
    }

    /**
     * Test 8: Level advancement
     */
    @Test
    public void testLevelAdvancement() {
        boolean advanced = model.advanceLevel();
        assertTrue(advanced);
        assertEquals(2, model.getCurrentLevel());
        assertEquals(10, model.getRows()); // Level 2 is 10x10
        assertEquals(10, model.getCols());
    }

    /**
     * Test 9: Cannot advance past level 5
     */
    @Test
    public void testCannotAdvancePastLevel5() {
        // Advance to level 5
        for (int i = 1; i < 5; i++) {
            model.advanceLevel();
        }
        assertEquals(5, model.getCurrentLevel());

        // Try to advance further
        boolean advanced = model.advanceLevel();
        assertFalse(advanced);
        assertEquals(5, model.getCurrentLevel());
    }

    /**
     * Test 10: Flood-fill reveals multiple cells
     *
     * This test verifies the recursive flood-fill algorithm by creating
     * a custom board and checking that revealing an empty cell reveals
     * all connected empty cells.
     */
    @Test
    public void testFloodFill() {
        // Create a new model and manually set up mines
        BlitzSweepModel testModel = new BlitzSweepModel();
        testModel.reset();

        // Clear all mines first (for testing)
        for (int r = 0; r < testModel.getRows(); r++) {
            for (int c = 0; c < testModel.getCols(); c++) {
                Cell cell = testModel.getCell(r, c);
                cell.setMine(false);
                cell.setAdjacentMines(0);
            }
        }

        // Place a few mines in a corner
        testModel.setMineAt(6, 6);
        testModel.setMineAt(6, 7);
        testModel.setMineAt(7, 6);
        testModel.setMineAt(7, 7);
        testModel.recalculateAdjacentMines();

        // Reveal a cell far from mines (should trigger flood-fill)
        testModel.revealCell(0, 0);

        // Check that multiple cells were revealed
        int revealedCount = 0;
        for (int r = 0; r < testModel.getRows(); r++) {
            for (int c = 0; c < testModel.getCols(); c++) {
                if (testModel.getCell(r, c).isRevealed()) {
                    revealedCount++;
                }
            }
        }

        // With 4 mines in corner, clicking (0,0) should reveal a large area
        assertTrue(
                revealedCount > 10,
                "Flood-fill should reveal multiple cells, but only revealed " + revealedCount
        );
    }

    /**
     * Test 11: Win condition - all non-mine cells revealed
     */
    @Test
    public void testWinCondition() {
        // Create a simple 3x3 board with 1 mine for easier testing
        BlitzSweepModel testModel = new BlitzSweepModel();
        testModel.reset();

        // Clear the board and set up a simple scenario
        for (int r = 0; r < testModel.getRows(); r++) {
            for (int c = 0; c < testModel.getCols(); c++) {
                testModel.getCell(r, c).setMine(false);
            }
        }

        // Place one mine
        testModel.setMineAt(7, 7);
        testModel.recalculateAdjacentMines();

        // Reveal all cells except the mine
        for (int r = 0; r < testModel.getRows(); r++) {
            for (int c = 0; c < testModel.getCols(); c++) {
                if (r != 7 || c != 7) {
                    testModel.revealCell(r, c);
                }
            }
        }

        // Should have won
        assertTrue(testModel.isGameWon());
        assertTrue(testModel.isGameOver());
    }

    /**
     * Test 12: Hitting a mine ends the game
     */
    @Test
    public void testHittingMine() {
        // Find a mine
        boolean foundMine = false;
        for (int r = 0; r < model.getRows() && !foundMine; r++) {
            for (int c = 0; c < model.getCols() && !foundMine; c++) {
                if (model.getCell(r, c).hasMine()) {
                    model.revealCell(r, c);
                    foundMine = true;

                    // Game should be over
                    assertTrue(model.isGameOver());
                    assertFalse(model.isGameWon());
                    // Should reset to level 1
                    assertEquals(1, model.getCurrentLevel());
                }
            }
        }

        assertTrue(foundMine, "Test setup failed: no mine found on board");
    }

    /**
     * Test 13: Cannot reveal flagged cell
     */
    @Test
    public void testCannotRevealFlaggedCell() {
        Cell cell = model.getCell(0, 0);

        // Flag the cell
        model.toggleFlag(0, 0);
        assertTrue(cell.isFlagged());

        // Try to reveal it
        model.revealCell(0, 0);

        // Should still be unrevealed
        assertFalse(cell.isRevealed());
    }

    /**
     * Test 14: Adjacent mine count is calculated correctly
     */
    @Test
    public void testAdjacentMineCount() {
        BlitzSweepModel testModel = new BlitzSweepModel();
        testModel.reset();

        // Clear all mines
        for (int r = 0; r < testModel.getRows(); r++) {
            for (int c = 0; c < testModel.getCols(); c++) {
                testModel.getCell(r, c).setMine(false);
            }
        }

        // Place mines around cell (4,4)
        testModel.setMineAt(3, 3);
        testModel.setMineAt(3, 4);
        testModel.setMineAt(3, 5);
        testModel.recalculateAdjacentMines();

        // Cell (4,4) should have 3 adjacent mines
        assertEquals(3, testModel.getCell(4, 4).getAdjacentMines());
    }

    /**
     * Test 15: Reset clears the board
     */
    @Test
    public void testReset() {
        // Make some moves
        model.revealCell(0, 0);
        model.toggleFlag(1, 1);
        model.decrementTimer();

        // Reset
        model.reset();

        // Check everything is reset
        assertEquals(1, model.getCurrentLevel());
        assertEquals(0, model.getScore());
        assertEquals(120, model.getTimeRemaining());
        assertFalse(model.isGameOver());

        // Check board is cleared
        for (int r = 0; r < model.getRows(); r++) {
            for (int c = 0; c < model.getCols(); c++) {
                Cell cell = model.getCell(r, c);
                assertFalse(cell.isRevealed());
                assertFalse(cell.isFlagged());
            }
        }
    }

    /**
     * Test 16: Score increases when completing a level
     */
    @Test
    public void testLevelCompletionBonus() {
        BlitzSweepModel testModel = new BlitzSweepModel();
        testModel.reset();

        // Set up a simple winning scenario
        for (int r = 0; r < testModel.getRows(); r++) {
            for (int c = 0; c < testModel.getCols(); c++) {
                testModel.getCell(r, c).setMine(false);
            }
        }
        testModel.setMineAt(7, 7);
        testModel.recalculateAdjacentMines();

        int scoreBeforeWin = testModel.getScore();

        // Reveal all non-mine cells to win
        for (int r = 0; r < testModel.getRows(); r++) {
            for (int c = 0; c < testModel.getCols(); c++) {
                if (r != 7 || c != 7) {
                    testModel.revealCell(r, c);
                }
            }
        }

        // Score should have increased significantly
        assertTrue(testModel.getScore() > scoreBeforeWin + 100);
        assertTrue(testModel.isGameWon());
    }
}

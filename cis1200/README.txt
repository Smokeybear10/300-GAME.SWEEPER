=:=:=:=:=:=:=:=:=:=:=:=:=:=:=:=:=:=:=
CIS 1200 Game Project README
PennKey: 37527460
=:=:=:=:=:=:=:=:=:=:=:=:=:=:=:=:=:=:=

===================
=: Core Concepts :=
===================

- List the four core concepts, the features they implement, and why each feature
  is an appropriate use of the concept. Incorporate the feedback you got after
  submitting your proposal.

  1. 2D Arrays
     Feature: The game board is represented as a 2D array of Cell objects (Cell[][] board) 
     in the BlitzSweepModel class. This structure stores the entire game state including 
     mine locations, revealed status, flags, and adjacent mine counts for each cell.
     
     Why appropriate: 2D arrays are the natural data structure for grid-based games like 
     minesweeper. They enable efficient neighbor checking (using row/col indices with 
     offsets), support the recursive flood-fill algorithm by providing direct access to 
     adjacent cells, and allow the board dimensions to scale dynamically with level 
     progression (8x8 to 16x16). The array structure makes it straightforward to iterate 
     through all cells for operations like mine placement, adjacent mine counting, and 
     win condition checking.

  2. Recursion
     Feature: The flood-fill algorithm implemented in the floodFill() method recursively 
     reveals all connected empty cells (cells with zero adjacent mines) when a player 
     clicks on an empty cell. The recursion continues through all 8 neighboring cells 
     until it reaches cells with adjacent mines or hits base cases.
     
     Why appropriate: Recursion is the ideal approach for flood-fill because it naturally 
     handles the connected component problem - when one empty cell is revealed, we need to 
     automatically reveal all connected empty cells. The recursive approach elegantly 
     handles the base cases (out of bounds, already revealed, flagged, has mine, or has 
     adjacent mines) and recursively processes valid neighbors. This is more elegant and 
     maintainable than an iterative approach using a stack or queue.

  3. JUnit Testable Component
     Feature: The BlitzSweepModel class is completely separated from the GUI, containing 
     all game logic in methods that can be tested independently. Comprehensive JUnit tests 
     in BlitzSweepTest.java verify flood-fill correctness, win/lose detection, level 
     progression, score calculation, timer logic, flagging, and edge cases - all without 
     any GUI dependencies.
     
     Why appropriate: Separating the model from the view/controller follows the MVC 
     pattern and enables thorough testing of game logic. The model has no dependencies on 
     Swing components, making it easy to write unit tests that verify correctness. This 
     separation ensures that game logic bugs can be caught and fixed independently of 
     visual presentation, and allows for regression testing as the game evolves.

  4. File I/O
     Feature: The GameStateIO class handles saving and loading complete game state 
     (board configuration, revealed cells, flags, current level, score, timer) to/from 
     text files. It also supports saving and loading high scores with player names and 
     timestamps. The implementation properly handles FileNotFoundException and other I/O 
     errors with user-friendly dialog messages.
     
     Why appropriate: File I/O is essential for allowing players to save their progress 
     and resume games later. The implementation saves all necessary game state in a 
     structured text format, making it human-readable and debuggable. Error handling 
     ensures the game doesn't crash when files are missing or corrupted, providing a 
     better user experience. The high score functionality demonstrates persistent data 
     storage across game sessions.

===============================
=: File Structure Screenshot :=
===============================
- Include a screenshot of your project's file structure. This should include
  all of the files in your project, and the folders they are in. You can
  upload this screenshot in your homework submission to gradescope, named 
  "file_structure.png".

=========================
=: Your Implementation :=
=========================

- Provide an overview of each of the classes in your code, and what their
  function is in the overall game.

  BlitzSweepModel: This is the core model class that contains all game logic, completely 
  independent of the GUI. It manages the 2D array board, handles mine placement, 
  implements the recursive flood-fill algorithm, tracks game state (level, score, timer), 
  handles win/lose conditions, and manages level progression. It provides methods like 
  revealCell(), toggleFlag(), decrementTimer(), and advanceLevel() that the GUI calls but 
  doesn't implement any visual rendering itself.

  BlitzSweepBoard: This class acts as both the view (rendering the game board with 
  paintComponent()) and controller (handling mouse and keyboard input) in the MVC 
  framework. It displays cells with appropriate colors and graphics, handles left-click to 
  reveal and right-click to flag, manages the game timer, displays status information, and 
  includes visual effects like particles for explosions and celebrations. It also 
  contains cheat code functionality for development/testing.

  Cell: A simple data class that represents a single cell on the game board. It stores 
  whether the cell has a mine, is revealed, is flagged, and the count of adjacent mines. 
  This encapsulation makes the code cleaner and easier to work with than storing these 
  values separately in the 2D array.

  GameStateIO: Handles all file I/O operations for the game. It provides static methods 
  to save and load game state to/from text files, and to save/load high scores. It 
  properly handles exceptions and displays user-friendly error messages using JOptionPane 
  dialogs.

  RunBlitzSweep: The main entry point that sets up the top-level JFrame, creates the UI 
  components (labels, buttons), and initializes the game board. It handles the overall 
  window layout and provides buttons for new game, save, load, and instructions.


- Were there any significant stumbling blocks while you were implementing your
  game (related to your design, or otherwise)?

  One significant challenge was ensuring the first click is always safe. Initially, mines 
  were placed randomly before the first click, which could result in the player hitting a 
  mine immediately. I solved this by deferring mine placement until the first click, then 
  placing mines while avoiding a 3x3 area around the first click position. This ensures 
  the first click always reveals a safe area and provides a good starting point.

  Another challenge was properly implementing the recursive flood-fill algorithm. 
  Initially, I had issues with infinite recursion when the base cases weren't properly 
  checked. I fixed this by carefully checking all base cases (out of bounds, already 
  revealed, flagged, has mine, or has adjacent mines) before recursing, and ensuring 
  that cells are marked as revealed before the recursive call to prevent revisiting.

  A design challenge was properly separating the model from the view. Initially, some 
  game logic was mixed into the GUI code, making it difficult to test. I refactored to 
  move all game logic into BlitzSweepModel, which made the code more testable and 
  maintainable. However, this required careful design of the model's public interface to 
  expose all necessary state without breaking encapsulation.

  Loading game state was also tricky because the model's internal state (like currentLevel, 
  score, timeRemaining) needed to be restored. Since these fields are private, I had to 
  work around this by reconstructing the model state through the public interface, which 
  was somewhat awkward but maintained proper encapsulation.


- Evaluate your design. Is there a good separation of functionality? How well is
  private state encapsulated? What would you refactor, if given the chance?

  Separation of Functionality: The design follows the MVC pattern reasonably well. 
  BlitzSweepModel contains all game logic and is completely independent of the GUI, which 
  is excellent for testing. BlitzSweepBoard handles both view and controller 
  responsibilities, which is acceptable for a game of this size, though ideally these 
  could be separated further. GameStateIO is properly separated as a utility class for 
  file operations.

  Encapsulation: Private state is well-encapsulated. The model's internal fields (board, 
  game state variables) are private, and access is provided through well-defined public 
  methods. The Cell class properly encapsulates its state with getters and setters. 
  However, one weakness is that GameStateIO needs to access model state to save it, which 
  works but requires the model to expose all necessary state through getters.

  What I Would Refactor: 
  1. I would separate BlitzSweepBoard into separate view and controller classes for 
     better separation of concerns.
  2. I would add a proper save/load interface to BlitzSweepModel (like Serializable or 
     custom save/load methods) to make GameStateIO's job cleaner and avoid the awkward 
     state reconstruction in loadGame().
  3. I would extract the particle system and visual effects into a separate class rather 
     than having it as an inner class in BlitzSweepBoard.
  4. I would consider using an enum for game states (PLAYING, WON, LOST) instead of 
     separate boolean flags (gameOver, gameWon).
  5. The cheat code functionality could be extracted into a separate class or at least 
     better organized.



========================
=: External Resources :=
========================

- Cite any external resources (images, tutorials, etc.) that you may have used 
  while implementing your game.

  No external images or assets were used. All graphics are drawn programmatically using 
  Java's Graphics2D API. The game uses standard Java Swing components and built-in fonts.

  The minesweeper game mechanics and flood-fill algorithm are based on the classic 
  minesweeper game, but the implementation (including the recursive flood-fill, level 
  progression system, and scoring) was developed from scratch for this project.

  Java documentation references used:
  - Java Swing API documentation for JPanel, JFrame, MouseListener, etc.
  - Java Graphics2D documentation for rendering and drawing operations
  - Java File I/O documentation for BufferedReader, BufferedWriter, FileReader, FileWriter

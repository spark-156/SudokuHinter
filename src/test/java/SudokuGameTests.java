import nl.lucabergman.SudokuGame;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SudokuGameTests {
    @Test
    public void testIsValidValid() {
        // completely valid
        SudokuGame sudokuGame = new SudokuGame(new Integer[][]{ // sudoku game designed against brute solvers
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, 3, null, 8, 5},
                {null, null, 1, null, 2, null, null, null, null},
                {null, null, null, 5, null, 7, null, null, null},
                {null, null, 4, null, null, null, 1, null, null},
                {null, 9, null, null, null, null, null, null, null},
                {5, null, null, null, null, null, null, 7, 3},
                {null, null, 2, null, 1, null, null, null, null},
                {null, null, null, null, 4, null, null, null, 9},
        });

        assertTrue(sudokuGame.isValid());
    }

    @Test
    public void testIsSubsectionValid() {
        // left bottom block is wrong, two 2's
        SudokuGame sudokuGame = new SudokuGame(new Integer[][]{
                {9, null, null, 5, null, 8, null, null, 7},
                {null, 8, null, 3, null, 2, 9, null, 5},
                {null, 5, 4, null, null, null, null, 8, null},
                {null, 7, null, 6, 8, null, null, 3, 2},
                {1, null, null, null, null, 4, null, null, 8},
                {5, null, null, 2, 1, 9, null, 6, null},
                {2, null, null, 9, null, 6, null, null, 1},
                {7, 2, 6, null, null, 1, null, 4, null},
                {null, null, 1, 4, 7, null, null, 5, 6},
        });

        assertFalse(sudokuGame.isValid(), "Expected the invalid subsection to make the grid invalid");
    }

    @Test
    public void testIsRowValid() {
        // top row is wrong, two 9's
        SudokuGame sudokuGame = new SudokuGame(new Integer[][]{
                {9, null, null, 5, 9, 8, null, null, 7},
                {null, 8, null, 3, null, 2, 9, null, 5},
                {null, 5, 4, null, null, null, null, 8, null},
                {null, 7, null, 6, 8, null, null, 3, 2},
                {1, null, null, null, null, 4, null, null, 8},
                {5, null, null, 2, 1, 9, null, 6, null},
                {null, null, null, 9, null, 6, null, null, 1},
                {7, 2, 6, null, null, 1, null, 4, null},
                {null, null, 1, 4, 7, null, null, 5, 6},
        });

        assertFalse(sudokuGame.isValid(), "Expected the invalid row to make the grid invalid");
    }

    @Test
    public void testIsColValid() {
        // left col is wrong, two 9's
        SudokuGame sudokuGame = new SudokuGame(new Integer[][]{
                {9, null, null, 5, null, 8, null, null, 7},
                {null, 8, null, 3, null, 2, 9, null, 5},
                {null, 5, 4, null, null, null, null, 8, null},
                {null, 7, null, 6, 8, null, null, 3, 2},
                {1, null, null, null, null, 4, null, null, 8},
                {5, null, null, 2, 1, 9, null, 6, null},
                {null, null, null, 9, null, 6, null, null, 1},
                {7, 2, 6, null, null, 1, null, 4, null},
                {9, null, 1, 4, 7, null, null, 5, 6},
        });

        assertFalse(sudokuGame.isValid(), "Expected the invalid column to make the grid invalid");
    }

    @Test
    public void testValidGrid() {
        // no duplicates in any row, column or block
        SudokuGame sudokuGame = new SudokuGame(new Integer[][]{
                {9, null, null, 5, null, 8, null, null, 7},
                {null, 8, null, 3, null, 2, 9, null, 5},
                {null, 5, 4, null, null, null, null, 8, null},
                {null, 7, null, 6, 8, null, null, 3, 2},
                {1, null, null, null, null, 4, null, null, 8},
                {5, null, null, 2, 1, 9, null, 6, null},
                {null, null, null, 9, null, 6, null, null, 1},
                {7, 2, 6, null, null, 1, null, 4, null},
                {null, null, 1, 4, 7, null, null, 5, 6},
        });

        assertTrue(sudokuGame.isValid(), "Expected the grid to be valid");
    }
}

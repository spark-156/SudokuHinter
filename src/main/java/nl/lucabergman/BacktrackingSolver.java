package nl.lucabergman;

public class BacktrackingSolver implements ISudokuSolver {
    public BacktrackingSolver() {
    }

    private boolean recursiveSolve(SudokuGame sudokuGame) {
        for (int rowIndex = 0; rowIndex < 9; rowIndex++) {
            for (int colIndex = 0; colIndex < 9; colIndex++) {
                if (sudokuGame.board[rowIndex][colIndex] == null) {
                    for (int i = 1; i <= 9; i++) {
                        sudokuGame.board[rowIndex][colIndex] = i; // try value
                        if (sudokuGame.isValidAt(new Coordinates(rowIndex, colIndex)) && recursiveSolve(sudokuGame)) return true;
                    }
                    sudokuGame.board[rowIndex][colIndex] = null; // reset when wrong
                    return false;
                }
            }
        }
        return true;
    }

    public void solve(SudokuGame sudokuGame) {
        recursiveSolve(sudokuGame);
    }
}

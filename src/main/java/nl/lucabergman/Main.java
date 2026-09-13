package nl.lucabergman;

public class Main {
    private void solve_and_time_sudoku(SudokuGame sudokuGame, ISudokuSolver solver) {
        IO.println(sudokuGame.toString());

        long startTime = System.nanoTime();
        solver.solve(sudokuGame);
        long endTime = System.nanoTime();
        long duration_in_ms = (endTime - startTime) / 1_000_000;

        IO.println(sudokuGame.toString());
        IO.println(String.format("Took: %d ms", duration_in_ms));
    }

    void main() throws Exception {
//        SudokuGame sudokuGame = new SudokuGame(new Integer[][]{
//                {1,    null, null, 7,    null, null, null, null, null},
//                {2,    null, null, null, null, null, null, null, null},
//                {3,    null, null, null, null, null, null, null, null},
//                {4,    null, null, null, 1,    null, null, null, null},
//                {null, null, null, null, null, null, null, null, null},
//                {5,    null, null, null, 2,    null, null, null, null},
//                {6,    null, null, null, null, null, null, null, null},
//                {8,    null, null, null, null, 7,    null, null, null},
//                {null, null, null, null, null, null, null, null, null},
//        });

        SudokuGame sudokuGame = new SudokuGame(new Integer[][]{
                {1,    null, null, 7,    null, null, null, null, null},
                {2,    null, null, null, null, null, null, null, null},
                {3,    null, null, null, null, null, null, null, null},
                {null, null, null, null, 1,    null, null, null, 7   },
                {null, null, null, null, null, null, null, null, null},
                {5,    null, null, null, 2,    null, null, null, null},
                {6,    null, null, null, null, null, null, null, null},
                {8,    null, null, null, null, 7,    null, null, null},
                {null, null, null, null, null, null, null, null, null},
        });
//        SudokuGamePossibleValues sgpv = new SudokuGamePossibleValues(sudokuGame);
//        IO.println(sgpv.toString());

        SudokuHinter sh = new SudokuHinter(sudokuGame);

        IO.println(sh.describe());

        while (sh.findHint() != null) {
//            IO.println(sh.possibleValues.toString());
//            IO.println(sh.sudokuGame.describe());
            IO.println(sh.hint.describe());
            IO.println(sh.describe());
            sh.applyHint();
        }

    }

//    void main() {
//        ISudokuSolver backtrackingSolver = new BacktrackingSolver();
//
//        SudokuGame sudokuGame = new SudokuGame(new Integer[][]{ // sudoku game 78
//                {null, 1, null, null, null, 3, null, null, 2},
//                {null, null, null, null, null, null, 4, null, null},
//                {null, 7, 4, null, 1, null, 9, null, null},
//                {null, 4, 7, null, 3, null, null, null, null},
//                {null, null, 2, null, null, 6, null, null, 7},
//                {null, null, null, null, 2, null, null, null, 3},
//                {null, null, null, null, null, null, 1, 9, 6},
//                {5, null, null, 6, 9, null, null, null, null,},
//                {null, null, null, null, null, 7, null, null, null,}
//        });
//        this.solve_and_time_sudoku(sudokuGame, backtrackingSolver);
//
//        sudokuGame = new SudokuGame(new Integer[][]{ // sudoku game designed against brute solvers
//                {null, null, null, null, null, null, null, null, null},
//                {null, null, null, null, null, 3, null, 8, 5},
//                {null, null, 1, null, 2, null, null, null, null},
//                {null, null, null, 5, null, 7, null, null, null},
//                {null, null, 4, null, null, null, 1, null, null},
//                {null, 9, null, null, null, null, null, null, null},
//                {5, null, null, null, null, null, null, 7, 3},
//                {null, null, 2, null, 1, null, null, null, null},
//                {null, null, null, null, 4, null, null, null, 9},
//        });
//        this.solve_and_time_sudoku(sudokuGame, backtrackingSolver);
//
//
//        sudokuGame = new SudokuGame(new Integer[][]{ // Really hard one for humans
//                {null, 2, 3, null, null, null, 7, null, null},
//                {null, null, 6, null, null, null, null, null, null},
//                {7, null, null, null, 2, null, 4, null, 6},
//                {null, null, null, null, null, null, 8, null, null},
//                {null, null, 7, 8, null, null, 2, 3, 4},
//                {null, null, null, null, 3, 4, null, 6, null},
//                {null, null, null, null, 7, null, null, null, 2},
//                {null, null, null, null, null, null, null, null, null},
//                {null, 1, null, null, null, null, 6, null, null},
//        });
//        this.solve_and_time_sudoku(sudokuGame, backtrackingSolver);
//    }
}

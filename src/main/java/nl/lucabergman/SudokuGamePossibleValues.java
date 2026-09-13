package nl.lucabergman;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class SudokuGamePossibleValues {
    // implementation of full candidate notation for the SudokuGame
    // might be reimplemented in to the sudokugame instead of being its own object.
    @SuppressWarnings("unchecked")
    private final Set<Integer>[][] possibleValues = new Set[9][9];
    private SudokuGame sudokuGame;

    public SudokuGamePossibleValues(SudokuGame sudokuGame) {
        this.sudokuGame = sudokuGame;
        this.resetPossibleValues(); // init nested array with empty sets.
        this.fillPossibleValues(sudokuGame);
    }


    public void resetPossibleValues() {
        for (Set<Integer>[] row : this.possibleValues) {
            for (int col = 0; col < row.length; col++) {
                row[col] = new HashSet<>();
            }
        }
    }

    public void fillPossibleValues(SudokuGame sudokuGame) {
        this.resetPossibleValues();
        // naively fill all possible values for all fields by checking if a move is valid or not.
        // todo further implement box
        for (int rowI = 0; rowI < 9; rowI++) {
            for (int colI = 0; colI < 9; colI++) {
                if (sudokuGame.board[rowI][colI] != null) continue;

                for (int possibleValue = 1; possibleValue < 10; possibleValue++) {
                    sudokuGame.board[rowI][colI] = possibleValue;
                    if (sudokuGame.isValidAt(rowI, colI)) {
                        this.possibleValues[rowI][colI].add(possibleValue);
                    }
                    sudokuGame.board[rowI][colI] = null;
                }
            }
        }
        this.sudokuGame = sudokuGame;
    }

    public Map<Integer, Set<Integer>> possibleValuesToOccurences(Set<Integer>[] pvs) {
        // list of sets to dictionary containing all indexes where that number occured in list
        // possible value : set of indexes of occurrences
        Map<Integer, Set<Integer>> dict = new HashMap<>();
        // loop over list of sets.
        for (int i = 0; i < 9; i++) {
            // loop over values of set and add index of occurrence
            for (Integer pv : pvs[i]) {
                Set<Integer> is = dict.get(pv);
                if (is == null) is = new HashSet<>();
                is.add(i);
                dict.put(pv, is);
            }
        }
        return dict;
    }

    public Set<Integer> getCell(int ri, int ci) {
        return this.possibleValues[ri][ci];
    }

    public Set<Integer>[] getRow(int ri) {
        return this.possibleValues[ri];
    }

    public Set<Integer>[] getColumn(int ci) {
        Set<Integer>[] col = new Set[9];
        for (int i = 0; i < 9; i++) {
            col[i] = this.possibleValues[i][ci];
        }
        return col;
    }

    public Set<Integer>[] getBlock(int bi) {
        int bri = (bi / 3) * 3;
        int bci = (bi % 3) * 3;

        Set<Integer>[] block = new Set[9];
        int blockIndex = 0;

        for (int i = bri; i < bri + 3; i++) {
            for (int j = bci; j < bci + 3; j++) {
                block[blockIndex] = this.possibleValues[i][j];
                blockIndex++;
            }
        }

        return block;
    }


    @Override
    public String toString() {
        StringBuilder sudoku = new StringBuilder();

        sudoku.append("------------------------------------------------------------------------------------\n");

        for (int ri = 0; ri < 9; ri++) {
            // inside row
            for (int loopi = 0; loopi < 3; loopi++) {
                // go over each col 3 times to print all 9 possible values
                sudoku.append(" | ");
                for (int ci = 0; ci < 9; ci++) {
                    // inside row AND col.
                    // check if already filled.
                    if (this.sudokuGame.board[ri][ci] != null) {
                        if (loopi == 0 || loopi == 2) {
                            sudoku.append("  .   ");
                        } else {
                            sudoku.append(String.format(". %d . ", this.sudokuGame.board[ri][ci]));
                        }
                    } else {
                        // print the possible values.
                        for (int pv = loopi * 3; pv < loopi * 3 + 3; pv++) {
                            // find the possible values.
                            if (this.getCell(ri, ci).contains(pv + 1)) {
                                sudoku.append(pv + 1);
                                sudoku.append(" ");
                            } else {
                                sudoku.append("  ");
                            }
                        }
                    }

                    sudoku.append("| ");
                }

                sudoku.append("\n");
            }

            sudoku.append("------------------------------------------------------------------------------------\n");
        }

        return sudoku.toString();
    }
}

package nl.lucabergman;

import java.util.*;
import java.util.function.Supplier;

public class SudokuHinter {
    public final SudokuGame sudokuGame;
    public final SudokuGamePossibleValues possibleValues;

    public Hint hint;

    public SudokuHinter(SudokuGame sudokuGame) {
        this.sudokuGame = sudokuGame;
        this.possibleValues = new SudokuGamePossibleValues(sudokuGame);
    }

    public String describe() {
        StringBuilder out = new StringBuilder();
        String separator = "---------+---------+---------";

        for (int ri = 0; ri < 9; ri++) {
            if (ri > 0 && ri % 3 == 0) {
                out.append(separator).append('\n');
            }
            StringBuilder line = new StringBuilder();
            for (int ci = 0; ci < 9; ci++) {
                if (ci > 0 && ci % 3 == 0) {
                    line.append('|');
                }
                line.append(describeCell(ri, ci, this.hint));
            }
            out.append(line.toString().stripTrailing()).append('\n');
        }
        return out.toString();
    }

    private String describeCell(int row, int col, Hint hint) {
        if (hint != null && hint.rowIndex() == row && hint.colIndex() == col) {
            return "[" + hint.value() + "]";
        }
        Integer cell = this.sudokuGame.board[row][col];
        return " " + (cell == null ? "." : cell) + " ";
    }


    public Hint findHint() {
        // in order of how hard they are to do.
        this.possibleValues.fillPossibleValues(this.sudokuGame);
        List<Supplier<Hint>> strategies = List.of(this::findNakedSingle, this::findHiddenSingle);

        this.hint = strategies.stream().map(Supplier::get).filter(Objects::nonNull).findFirst().orElse(null);
        return this.hint;
    }

    public void applyHint() throws Exception {
        if (this.hint == null) return;

        this.sudokuGame.makeMove(
                this.hint.rowIndex(),
                this.hint.colIndex(),
                this.hint.value()
        );
    }

    // PRIVATE UNDER HERE
    private int[] getRowColIndexFromBoxListIndex(int bi, int li) {
        int ri = bi % 3 * 3 + li % 3;
        int ci = bi % 3 * 3 + li % 3;

        return new int[]{ri, ci};
    }

    private Integer[] getMissingValues(Integer[] list) {
        // return a list of ints that are missing in the Integer list assuming list is 1 till 9
        HashSet<Integer> missing = new HashSet<>(Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9));

        for (Integer integer : list) {
            missing.remove(integer);
        }

        return missing.toArray(new Integer[0]);
    }


    // FINDERS
    private Hint findNakedSingle() {
        // loop over all col and rows in the possible values and find a set that has just one number
        for (int ri = 0; ri < 9; ri++) {
            for (int ci = 0; ci < 9; ci++) {
                if (this.possibleValues.getCell(ri, ci).size() == 1) {
                    return new Hint(ri, ci, this.possibleValues.getCell(ri, ci).iterator().next(), HintLocation.CELL, HintType.NAKED_SINGLE);
                }
            }
        }
        return null;
    }


    private Hint findHiddenSingle() {
        // look at all possible values in list
        // if a pv only appears at one index in list then it is a hidden single.

        for (int bi = 0; bi < 9; bi++) {
            Set<Integer>[] block = this.possibleValues.getBlock(bi);

            Map<Integer, Set<Integer>> occurrences = this.possibleValues.possibleValuesToOccurences(block);
            for (Map.Entry<Integer, Set<Integer>> occ : occurrences.entrySet()) {
                if (occ.getValue().size() == 1) {
                    // only occured in one spot! lets gooo
                    var ii = this.getRowColIndexFromBoxListIndex(bi, occ.getValue().iterator().next());
                    return new Hint(ii[0], ii[1], occ.getKey(), HintLocation.BLOCK, HintType.HIDDEN_SINGLE);
                }
            }
        }

        for (int ri = 0; ri < 9; ri++) {
            Set<Integer>[] row = this.possibleValues.getRow(ri);

            Map<Integer, Set<Integer>> occurrences = this.possibleValues.possibleValuesToOccurences(row);
            for (Map.Entry<Integer, Set<Integer>> occ : occurrences.entrySet()) {
                if (occ.getValue().size() == 1) {
                    // only occured in one spot! lets gooo
                    return new Hint(ri, occ.getValue().iterator().next(), occ.getKey(), HintLocation.ROW, HintType.HIDDEN_SINGLE);
                }
            }
        }

        for (int ci = 0; ci < 9; ci++) {
            Set<Integer>[] row = this.possibleValues.getColumn(ci);

            Map<Integer, Set<Integer>> occurrences = this.possibleValues.possibleValuesToOccurences(row);
            for (Map.Entry<Integer, Set<Integer>> occ : occurrences.entrySet()) {
                if (occ.getValue().size() == 1) {
                    // only occured in one spot! lets gooo
                    return new Hint(occ.getValue().iterator().next(), ci, occ.getKey(), HintLocation.COLUMN, HintType.HIDDEN_SINGLE);
                }
            }
        }

        return null;
    }
}

package nl.lucabergman;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
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

        this.sudokuGame.makeMove(this.hint.rowIndex(), this.hint.colIndex(), this.hint.value());
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

            Map<Integer, Set<Integer>> occurrences = this.possibleValues.candidateOccurrences(block);
            for (Map.Entry<Integer, Set<Integer>> occ : occurrences.entrySet()) {
                if (occ.getValue().size() == 1) {
                    Coordinates coordinates = new BlockListCoordinates(bi, occ.getValue().iterator().next()).getCoordinates();
                    return new Hint(coordinates.rowIndex(), coordinates.columnIndex(), occ.getKey(), HintLocation.BLOCK, HintType.HIDDEN_SINGLE);
                }
            }
        }

        for (int ri = 0; ri < 9; ri++) {
            Set<Integer>[] row = this.possibleValues.getRow(ri);

            Map<Integer, Set<Integer>> occurrences = this.possibleValues.candidateOccurrences(row);
            for (Map.Entry<Integer, Set<Integer>> occ : occurrences.entrySet()) {
                if (occ.getValue().size() == 1) {
                    return new Hint(ri, occ.getValue().iterator().next(), occ.getKey(), HintLocation.ROW, HintType.HIDDEN_SINGLE);
                }
            }
        }

        for (int ci = 0; ci < 9; ci++) {
            Set<Integer>[] row = this.possibleValues.getColumn(ci);

            Map<Integer, Set<Integer>> occurrences = this.possibleValues.candidateOccurrences(row);
            for (Map.Entry<Integer, Set<Integer>> occ : occurrences.entrySet()) {
                if (occ.getValue().size() == 1) {
                    return new Hint(occ.getValue().iterator().next(), ci, occ.getKey(), HintLocation.COLUMN, HintType.HIDDEN_SINGLE);
                }
            }
        }

        return null;
    }
}

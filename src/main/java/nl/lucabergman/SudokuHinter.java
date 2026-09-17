package nl.lucabergman;

import java.util.*;
import java.util.function.Supplier;

import static java.util.stream.Collectors.*;

public class SudokuHinter {
    public final SudokuGame sudokuGame;

    @SuppressWarnings("unchecked")
    private final Set<Integer>[][] candidates = new Set[9][9];


    public Hint hint;

    public SudokuHinter(SudokuGame sudokuGame) {
        this.sudokuGame = sudokuGame;
        this.resetPossibleValues(); // init nested array with empty sets.
        this.fillPossibleValues();
    }

    // CANDIDATE NOTATION
    private void resetPossibleValues() {
        for (Set<Integer>[] row : this.candidates) {
            for (int col = 0; col < row.length; col++) {
                row[col] = new HashSet<>();
            }
        }
    }

    private void fillPossibleValues() {
        // naively fill all possible values for all fields by checking if a move is valid or not.
        for (int ri = 0; ri < 9; ri++) {
            for (int ci = 0; ci < 9; ci++) {
                Coordinates coordinates = new Coordinates(ri, ci);
                if (this.sudokuGame.getCell(coordinates) != null) continue;

                for (int candidateValue = 1; candidateValue < 10; candidateValue++) {
                    this.sudokuGame.board[ri][ci] = candidateValue; // todo refactor to public method of SudokuGame class
                    if (this.sudokuGame.isValidAt(coordinates)) {
                        this.candidates[ri][ci].add(candidateValue); // todo refactor to private method?
                    }
                    this.sudokuGame.board[ri][ci] = null; // todo refactor to public method of SudokuGame class (undo func?)
                }
            }
        }
    }


    public Map<Integer, Set<Integer>> candidateOccurrences(Set<Integer>[] candidates) {
        // list of sets to dictionary containing all indexes where that number occurred in list
        // possible value : set of indexes of occurrences
        Map<Integer, Set<Integer>> dict = new HashMap<>();
        // loop over list of sets.
        for (int i = 0; i < 9; i++) {
            // loop over values of set and add index of occurrence
            for (Integer candidate : candidates[i]) {
                Set<Integer> occurrences = dict.get(candidate);
                if (occurrences == null) occurrences = new HashSet<>();
                occurrences.add(i);
                dict.put(candidate, occurrences);
            }
        }
        return dict;
    }


    public Set<Integer>[] getRowCandidates(int ri) {
        return this.candidates[ri];
    }


    public Set<Integer>[] getColumnCandidates(int ci) {
        Set<Integer>[] col = new Set[9];
        for (int i = 0; i < 9; i++) {
            col[i] = this.candidates[i][ci];
        }
        return col;
    }


    public Set<Integer>[] getBlockCandidates(int bi) {
        int bri = (bi / 3) * 3;
        int bci = (bi % 3) * 3;

        Set<Integer>[] block = new Set[9];
        int blockIndex = 0;

        for (int i = bri; i < bri + 3; i++) {
            for (int j = bci; j < bci + 3; j++) {
                block[blockIndex] = this.candidates[i][j];
                blockIndex++;
            }
        }
        return block;
    }

    private boolean assertSubsetOfRegion(Set<Integer> foundSet, Set<Integer>[] candidates) {
        // todo improve, does it really do what it says?
        // Size doesnt matter, removing other candidates does.
        for (Set<Integer> candidate : candidates) {
            if (candidate.isEmpty() || candidate.equals(foundSet)) continue;
            Set<Integer> diffSet = new HashSet<>(candidate);
            diffSet.removeAll(foundSet);
            if (candidate.size() != diffSet.size()) return true;
        }
        return false;
    }

    private void restrictCandidates(Set<Integer> values, Set<Coordinates> coordinates) {
        // todo find out where sets get copied and accidentally mutate each other.
        // todo improve. 3 code blocks do almost exactly the same.
        // give a set of candidates that are only allowed in a set of locations.
        // this function will filter out all other candidates within the same region that may no longer house these values.
        boolean is_row = coordinates.stream().allMatch(l -> l.rowIndex() == coordinates.iterator().next().rowIndex());
        boolean is_col = coordinates.stream().allMatch(l -> l.columnIndex() == coordinates.iterator().next().columnIndex());
        boolean is_block = coordinates.stream().allMatch(l -> l.getBlockStartCoordinates().equals(coordinates.iterator().next().getBlockStartCoordinates()));

        Coordinates firstCoordinates = coordinates.iterator().next();

        if (is_row) {
            int ri = firstCoordinates.rowIndex();
            for (int ci = 0; ci < 9; ci++) {
                if (this.sudokuGame.board[ri][ci] != null) continue;
                    // only keep the possible values.
                else if (coordinates.contains(new Coordinates(ri, ci))) this.candidates[ri][ci] = values;
                    // remove the candidates as they cannot be in this location
                else this.candidates[ri][ci].removeAll(values);
            }
        }

        if (is_col) {
            int ci = firstCoordinates.columnIndex();
            for (int ri = 0; ri < 9; ri++) {
                if (this.sudokuGame.board[ri][ci] != null) continue;
                    // only keep the possible values.
                else if (coordinates.contains(new Coordinates(ri, ci))) this.candidates[ri][ci] = values;
                    // remove the candidates as they cannot be in this location
                else this.candidates[ri][ci].removeAll(values);
            }
        }

        if (is_block) {
            Coordinates blockStartCoords = firstCoordinates.getBlockStartCoordinates();
            for (int ri = blockStartCoords.rowIndex(); ri < blockStartCoords.rowIndex() + 3; ri++) {
                for (int ci = blockStartCoords.columnIndex(); ci < blockStartCoords.columnIndex() + 3; ci++) {
                    if (this.sudokuGame.board[ri][ci] != null) continue;
                    else if (coordinates.contains(new Coordinates(ri, ci))) this.candidates[ri][ci] = values;
                    else this.candidates[ri][ci].removeAll(values);
                }
            }
        }
    }


    // DESCRIBING
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
                line.append(describeCell(new Coordinates(ri, ci)));
            }
            out.append(line.toString().stripTrailing()).append('\n');
        }
        return out.toString();
    }

    private String describeCell(Coordinates cell) {
        Integer value = this.sudokuGame.getCell(cell);
        String content = value == null ? "." : String.valueOf(value);
        return switch (this.hint) {
            case Placement p when p.cell().equals(cell) -> "[" + p.value() + "]";
            case Resctriction r when r.cells().contains(cell) -> "{" + content + "}";
            case null, default -> " " + content + " ";
        };
    }


    // HINTERS
    public Hint findHint() {
        // in order of how hard they are to do.
        List<Supplier<Hint>> strategies = List.of(this::findNakedSingle, this::findHiddenSingle, this::findHiddenSets);

        this.hint = strategies.stream().map(Supplier::get).filter(Objects::nonNull).findFirst().orElse(null);
        return this.hint;
    }

    public void applyHint() throws Exception {
        if (this.hint == null) return;

        switch (this.hint) {
            case Placement p -> {
                this.sudokuGame.makeMove(p.cell(), p.value());
                this.restrictCandidates(Set.of(p.value()), Set.of(p.cell()));
                this.candidates[p.cell().rowIndex()][p.cell().columnIndex()] = new HashSet<>();
            }
            case Resctriction r -> this.restrictCandidates(r.values(), r.cells());
            default -> throw new IllegalStateException("Unexpected value: " + this.hint);
        }

        this.hint = null;
//        this.sudokuGame.makeMove(this.hint.(), this.hint.colIndex(), this.hint.value());
    }


    // FINDERS
    private Placement findNakedSingle() {
        // loop over all col and rows in the possible values and find a set that has just one number
        for (int ri = 0; ri < 9; ri++) {
            for (int ci = 0; ci < 9; ci++) {
                if (this.candidates[ri][ci].size() == 1) {
                    return new Placement(new Coordinates(ri, ci), this.candidates[ri][ci].iterator().next(), HintLocation.CELL, HintType.NAKED_SINGLE);
                }
            }
        }
        // Didnt find a naked single
        return null;
    }


    private Placement findHiddenSingle() {
        // look at all possible values in list
        // if a pv only appears at one index in list then it is a hidden single.

        for (int bi = 0; bi < 9; bi++) {
            Set<Integer>[] block = this.getBlockCandidates(bi);

            Map<Integer, Set<Integer>> occurrences = this.candidateOccurrences(block);
            for (Map.Entry<Integer, Set<Integer>> occ : occurrences.entrySet()) {
                if (occ.getValue().size() == 1) {
                    Coordinates coordinates = new BlockListCoordinates(bi, occ.getValue().iterator().next()).getCoordinates();
                    return new Placement(coordinates, occ.getKey(), HintLocation.BLOCK, HintType.HIDDEN_SINGLE);
                }
            }
        }

        for (int ri = 0; ri < 9; ri++) {
            Set<Integer>[] row = this.getRowCandidates(ri);

            Map<Integer, Set<Integer>> occurrences = this.candidateOccurrences(row);
            for (Map.Entry<Integer, Set<Integer>> occ : occurrences.entrySet()) {
                if (occ.getValue().size() == 1) {
                    return new Placement(new Coordinates(ri, occ.getValue().iterator().next()), occ.getKey(), HintLocation.ROW, HintType.HIDDEN_SINGLE);
                }
            }
        }

        for (int ci = 0; ci < 9; ci++) {
            Set<Integer>[] row = this.getColumnCandidates(ci);

            Map<Integer, Set<Integer>> occurrences = this.candidateOccurrences(row);
            for (Map.Entry<Integer, Set<Integer>> occ : occurrences.entrySet()) {
                if (occ.getValue().size() == 1) {
                    return new Placement(new Coordinates(occ.getValue().iterator().next(), ci), occ.getKey(), HintLocation.COLUMN, HintType.HIDDEN_SINGLE);
                }
            }
        }

        // Didnt find a hidden single.
        return null;
    }

    // todo make return type a hint
    private Resctriction findHiddenSets() {
        // returns true if it found a hidden set (re-run again)
        // find hidden pairs in a Sudoku
        // assume possible values have already been set.

        // loop over cols, rows and boxes and try to find naked pairs.
        // caller of this function should keep calling until all options have been exhausted.

        // rows
        for (int ri = 0; ri < 9; ri++) {
            Set<Integer>[] row = this.getRowCandidates(ri);
            Map<Integer, Set<Integer>> occurrences = this.candidateOccurrences(row); // map of value to what indexes it occurs at
            Map<Set<Integer>, Set<Integer>> duplicates = occurrences.entrySet()// map of indexes in list to values
                    .stream().collect(groupingBy(Map.Entry::getValue, mapping(Map.Entry::getKey, toSet())));
            for (Map.Entry<Set<Integer>, Set<Integer>> dupe : duplicates.entrySet()) {
                // translate indexes of duplicates to coordinates and filter the regions.
                Set<Coordinates> coords = new HashSet<>();
                for (Integer ci : dupe.getKey()) {
                    Coordinates coordinates = new Coordinates(ri, ci);
                    coords.add(coordinates);
                }
                if (coords.size() > 1 && dupe.getValue().size() == coords.size() && assertSubsetOfRegion(dupe.getValue(), row)) {
                    // also assert that the row or col or block is not completely filled yet.
                    return new Resctriction(coords, dupe.getValue(), HintLocation.ROW, HintType.HIDDEN_SET);
                }
            }
        }
        // column
        for (int ci = 0; ci < 9; ci++) {
            Set<Integer>[] col = this.getColumnCandidates(ci);
            Map<Integer, Set<Integer>> occurrences = this.candidateOccurrences(col); // map of value to what indexes it occurs at
            Map<Set<Integer>, Set<Integer>> duplicates = occurrences.entrySet()// map of indexes in list to values
                    .stream().collect(groupingBy(Map.Entry::getValue, mapping(Map.Entry::getKey, toSet())));
            for (Map.Entry<Set<Integer>, Set<Integer>> dupe : duplicates.entrySet()) {
                // translate indexes of duplicates to coordinates and filter the regions.
                Set<Coordinates> coords = new HashSet<>();
                for (Integer ri : dupe.getKey()) {
                    Coordinates coordinates = new Coordinates(ri, ci);
                    coords.add(coordinates);
                }
                if (coords.size() > 1 && dupe.getValue().size() == coords.size() && assertSubsetOfRegion(dupe.getValue(), col)) {
                    return new Resctriction(coords, dupe.getValue(), HintLocation.COLUMN, HintType.HIDDEN_SET);
                }
            }
        }
        // block
        for (int bi = 0; bi < 9; bi++) {
            Set<Integer>[] block = this.getBlockCandidates(bi);
            Map<Integer, Set<Integer>> occurrences = this.candidateOccurrences(block); // map of value to what indexes it occurs at
            Map<Set<Integer>, Set<Integer>> duplicates = occurrences.entrySet()// map of indexes in list to values
                    .stream().collect(groupingBy(Map.Entry::getValue, mapping(Map.Entry::getKey, toSet())));
            for (Map.Entry<Set<Integer>, Set<Integer>> dupe : duplicates.entrySet()) {
                // translate indexes of duplicates to coordinates and filter the regions.
                Set<Coordinates> coords = new HashSet<>();
                for (Integer li : dupe.getKey()) {
                    coords.add(new BlockListCoordinates(bi, li).getCoordinates());
                }
                if (coords.size() > 1 && dupe.getValue().size() == coords.size() && assertSubsetOfRegion(dupe.getValue(), block)) {
                    return new Resctriction(coords, dupe.getValue(), HintLocation.BLOCK, HintType.HIDDEN_SET);
                }
            }
        }

        // Didnt find a Restriction Hint
        return null;
    }
}

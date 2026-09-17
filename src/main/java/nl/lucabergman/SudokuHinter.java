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
        this.resetCandidates(); // init nested array with empty sets.
        this.naiveFillCandidates();
    }

    // CANDIDATE NOTATION
    private void resetCandidates() {
        for (Set<Integer>[] row : this.candidates) {
            for (int col = 0; col < row.length; col++) {
                row[col] = new HashSet<>();
            }
        }
    }

    private void naiveFillCandidates() {
        // naively fill all candidates for all fields by checking if a move is valid or not.
        for (int ri = 0; ri < 9; ri++) {
            for (int ci = 0; ci < 9; ci++) {
                Coordinates coordinates = new Coordinates(ri, ci);
                if (this.sudokuGame.getCell(coordinates) != null) continue;

                for (int candidateValue = 1; candidateValue < 10; candidateValue++) {
                    this.sudokuGame.setCell(coordinates, candidateValue);
                    if (this.sudokuGame.isValidAt(coordinates)) {
                        this.candidates[ri][ci].add(candidateValue);
                    }
                    this.sudokuGame.setCell(coordinates, null);
                }
            }
        }
    }


    public Map<Integer, Set<Integer>> candidateOccurrences(Set<Integer>[] candidates) {
        // list of sets to dictionary containing all indexes where that number occurred in list
        // AKA: candidate 5 occurred at indexes: (3, 6) in the list. Candidate 6 occurred at indexes (1, 6, 7)
        // { "candidate" : set of indexes of occurrences }
        Map<Integer, Set<Integer>> dict = new HashMap<>();
        // loop over the candidates in the list of lists of candidates.
        // per candidate -> add map entry and keep track of every index it occurred at.
        for (int i = 0; i < 9; i++) {
            for (Integer candidate : candidates[i]) {
                Set<Integer> occurrences = dict.get(candidate);
                if (occurrences == null) occurrences = new HashSet<>();
                occurrences.add(i);
                dict.put(candidate, occurrences);
            }
        }
        return dict;
    }


    // HELPER FUNCTIONS FOR GETTING CANDIDATES
    private Set<Integer>[] getCandidatesRow(int ri) {
        return this.candidates[ri];
    }

    private Set<Integer>[] getCandidatesColumn(int ci) {
        Set<Integer>[] col = new Set[9];
        for (int i = 0; i < 9; i++) {
            col[i] = this.candidates[i][ci];
        }
        return col;
    }

    private Set<Integer>[] getCandidatesBlock(int bi) {
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

    private void restrictCandidates(Set<Coordinates> coordinates, Set<Integer> values) {
        // todo find out where sets get copied and accidentally mutate each other.
        // todo improve. 3 code blocks do almost exactly the same.
        // give a set of candidates that are only allowed in a set of coordinates.
        // this function will filter out all other candidates within the same region that may no longer house these values.
        // region is determined by the positioning of the coordinates (straight line, all within same block etc)
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


    // HINTERS
    public Hint findHint() {
        // in order of how hard they are to do and what a human would do.
        List<Supplier<Hint>> strategies = List.of(this::findNakedSingle, this::findHiddenSingle, this::findHiddenSets);

        this.hint = strategies.stream().map(Supplier::get).filter(Objects::nonNull).findFirst().orElse(null);
        return this.hint;
    }

    public void applyHint() throws Exception {
        if (this.hint == null) return;
        switch (this.hint) {
            case Placement p -> {
                this.sudokuGame.makeMove(p.cell(), p.value()); // Raises Exception if illegal move.
                this.restrictCandidates(Set.of(p.cell()), Set.of(p.value())); // Update candidate map
                this.candidates[p.cell().rowIndex()][p.cell().columnIndex()] = new HashSet<>(); // Update candidate map
            }
            case Resctriction r -> this.restrictCandidates(r.cells(), r.values());
            default -> throw new IllegalStateException("Unexpected value: " + this.hint);
        }
        this.hint = null;
    }


    // FINDERS
    private Placement findNakedSingle() {
        // loop over all cells and try to find a cell that has just one candidate.
        for (int ri = 0; ri < 9; ri++) {
            for (int ci = 0; ci < 9; ci++) {
                if (this.candidates[ri][ci].size() == 1) {
                    return new Placement(new Coordinates(ri, ci), this.candidates[ri][ci].iterator().next(), HintLocation.CELL, HintType.NAKED_SINGLE);
                }
            }
        }
        return null; // Didnt find a naked single
    }


    private Placement findHiddenSingle() {
        // loop over all cells in a list of candidate cells and try to find a candidate
        // that appears in just one cell.

        for (int bi = 0; bi < 9; bi++) {
            Set<Integer>[] block = this.getCandidatesBlock(bi);

            Map<Integer, Set<Integer>> occurrences = this.candidateOccurrences(block);
            for (Map.Entry<Integer, Set<Integer>> occ : occurrences.entrySet()) {
                if (occ.getValue().size() == 1) {
                    Coordinates coordinates = new BlockListCoordinates(bi, occ.getValue().iterator().next()).getCoordinates();
                    return new Placement(coordinates, occ.getKey(), HintLocation.BLOCK, HintType.HIDDEN_SINGLE);
                }
            }
        }

        for (int ri = 0; ri < 9; ri++) {
            Set<Integer>[] row = this.getCandidatesRow(ri);

            Map<Integer, Set<Integer>> occurrences = this.candidateOccurrences(row);
            for (Map.Entry<Integer, Set<Integer>> occ : occurrences.entrySet()) {
                if (occ.getValue().size() == 1) {
                    return new Placement(new Coordinates(ri, occ.getValue().iterator().next()), occ.getKey(), HintLocation.ROW, HintType.HIDDEN_SINGLE);
                }
            }
        }

        for (int ci = 0; ci < 9; ci++) {
            Set<Integer>[] row = this.getCandidatesColumn(ci);

            Map<Integer, Set<Integer>> occurrences = this.candidateOccurrences(row);
            for (Map.Entry<Integer, Set<Integer>> occ : occurrences.entrySet()) {
                if (occ.getValue().size() == 1) {
                    return new Placement(new Coordinates(occ.getValue().iterator().next(), ci), occ.getKey(), HintLocation.COLUMN, HintType.HIDDEN_SINGLE);
                }
            }
        }

        return null; // Didnt find a hidden single.
    }

    private Resctriction findHiddenSets() {
        // Find an arbitrary group of candidates that form a hidden set.
        // Returns a Restriction hint if found
        // Searches rows, columns and blocks, in that order.

        // rows
        for (int ri = 0; ri < 9; ri++) {
            Set<Integer>[] row = this.getCandidatesRow(ri);
            Map<Integer, Set<Integer>> occurrences = this.candidateOccurrences(row); // map of value to what indexes it occurs at
            // map of index sets in list to value sets.
            // candidate value (c.v.), indexes of where c.v. occurred (i.o.)
            // 5 (c.v.): (3, 6)(i.o.), 6 (c.v.): (1,6,7)(i.o.), 7 (c.v.): (3,6)
            // ->
            // (3,6)(i.o): (5, 7)(c.v.), (1,6,7)(i.o): (6)
            // 5, 7 only occur at indexes 3 and 6 in the list. 6 can be stripped as candidate from index 6 in this list.
            // and 5,7 can be removed as candidates from other cells in this row.
            Map<Set<Integer>, Set<Integer>> duplicates = occurrences.entrySet()
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

        // columns
        for (int ci = 0; ci < 9; ci++) {
            Set<Integer>[] col = this.getCandidatesColumn(ci);
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

        // blocks
        for (int bi = 0; bi < 9; bi++) {
            Set<Integer>[] block = this.getCandidatesBlock(bi);
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

        return null; // Didn't find a hidden set.
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
}

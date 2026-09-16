package nl.lucabergman;

import java.util.*;

import static java.util.stream.Collectors.*;

public class SudokuGamePossibleValues {
    // implementation of full candidate notation for the SudokuGame
    // might be reimplemented in to the Sudoku instead of being its own object.
    @SuppressWarnings("unchecked")
    private final Set<Integer>[][] candidates = new Set[9][9];
    private SudokuGame sudokuGame;

    public SudokuGamePossibleValues(SudokuGame sudokuGame) {
        this.sudokuGame = sudokuGame;
        this.resetPossibleValues(); // init nested array with empty sets.
        this.fillPossibleValues(sudokuGame);
    }


    public void resetPossibleValues() {
        for (Set<Integer>[] row : this.candidates) {
            for (int col = 0; col < row.length; col++) {
                row[col] = new HashSet<>();
            }
        }
    }

    public void fillPossibleValues(SudokuGame sudokuGame) {
        this.resetPossibleValues();
        // naively fill all possible values for all fields by checking if a move is valid or not.
        for (int ri = 0; ri < 9; ri++) {
            for (int ci = 0; ci < 9; ci++) {
                if (sudokuGame.board[ri][ci] != null) continue;

                for (int candidateValue = 1; candidateValue < 10; candidateValue++) {
                    sudokuGame.board[ri][ci] = candidateValue;
                    if (sudokuGame.isValidAt(ri, ci)) {
                        this.candidates[ri][ci].add(candidateValue);
                    }
                    sudokuGame.board[ri][ci] = null;
                }
            }
        }
        // todo each hidden set really already is a hint ^^ create a hint stack?
        this.findHiddenSets();

        this.sudokuGame = sudokuGame;
    }

    private boolean assertSubsetOfRegion(Set<Integer> foundSet, Set<Integer>[] candidates) {
        // Size doesnt matter, removing other candidates does.

        for (Set<Integer> candidate : candidates) {
            if (candidate.isEmpty() || candidate.equals(foundSet)) continue;
            Set<Integer> diffSet = new HashSet<>(candidate);
            diffSet.removeAll(foundSet);
            if (candidate.size() != diffSet.size()) return true;
        }
        return false;
//        return Arrays.stream(candidates).anyMatch(candidate -> candidate.size() < size);
//        return coordinates.stream().anyMatch(coordinate -> this.candidates[coordinate.rowIndex()][coordinate.columnIndex()].size() > values.size());
    }

    private void filterRegions(Set<Integer> values, Set<Coordinates> coordinates) {
        // give a set of candidates that are only allowed in a set of locations.
        // this function will filter out all other candidates within the same region that may no longer house these values.
        boolean is_row = coordinates.stream().allMatch(l -> l.rowIndex() == coordinates.iterator().next().rowIndex());
        boolean is_col = coordinates.stream().allMatch(l -> l.columnIndex() == coordinates.iterator().next().columnIndex());
        boolean is_block = coordinates.stream().allMatch(l -> l.getBlockStartCoordinates().equals(coordinates.iterator().next().getBlockStartCoordinates()));

        if (is_row) {
            int ri = coordinates.iterator().next().rowIndex();
            for (int ci = 0; ci < 9; ci++) {
                if (this.sudokuGame.board[ri][ci] != null) continue;
                    // only keep the possible values.
                else if (coordinates.contains(new Coordinates(ri, ci))) this.candidates[ri][ci] = values;
                    // remove the candidates as they cannot be in this location
                else this.candidates[ri][ci].removeAll(values);
            }
        }

        if (is_col) {
            int ci = coordinates.iterator().next().columnIndex();
            for (int ri = 0; ri < 9; ri++) {
                if (this.sudokuGame.board[ri][ci] != null) continue;
                    // only keep the possible values.
                else if (coordinates.contains(new Coordinates(ri, ci))) this.candidates[ri][ci] = values;
                    // remove the candidates as they cannot be in this location
                else this.candidates[ri][ci].removeAll(values);
            }
        }

        if (is_block) {
            Coordinates blockStartCoords = coordinates.iterator().next().getBlockStartCoordinates();
            for (int ri = blockStartCoords.rowIndex(); ri < blockStartCoords.rowIndex() + 3; ri++) {
                for (int ci = blockStartCoords.columnIndex(); ci < blockStartCoords.columnIndex() + 3; ci++) {
                    if (this.sudokuGame.board[ri][ci] != null) continue;
                    else if (coordinates.contains(new Coordinates(ri, ci))) this.candidates[ri][ci] = values;
                    else this.candidates[ri][ci].removeAll(values);
                }
            }
        }
    }

    public void findHiddenSets() {
        // returns true if it found a hidden set (re-run again)
        // find hidden pairs in a Sudoku
        // assume possible values have already been set.

        // loop over cols, rows and boxes and try to find naked pairs.
        // caller of this function should keep calling until all options have been exhausted.
        boolean foundHiddenSet = false;

        // rows
        for (int ri = 0; ri < 9; ri++) {
            Set<Integer>[] row = this.getRow(ri);
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
                    filterRegions(dupe.getValue(), coords);
                    foundHiddenSet = true;
                }
            }
        }
        // column
        for (int ci = 0; ci < 9; ci++) {
            Set<Integer>[] col = this.getColumn(ci);
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
                    filterRegions(dupe.getValue(), coords);
                    foundHiddenSet = true;
                }
            }
        }
        // block
        for (int bi = 0; bi < 9; bi++) {
            Set<Integer>[] block = this.getBlock(bi);
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
                    filterRegions(dupe.getValue(), coords);
                    foundHiddenSet = true;
                }
            }
        }

        if (foundHiddenSet) this.findHiddenSets();
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

    public Set<Integer> getCell(int ri, int ci) {
        return this.candidates[ri][ci];
    }

    public Set<Integer>[] getRow(int ri) {
        return this.candidates[ri];
    }


    public Set<Integer>[] getColumn(int ci) {
        Set<Integer>[] col = new Set[9];
        for (int i = 0; i < 9; i++) {
            col[i] = this.candidates[i][ci];
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
                block[blockIndex] = this.candidates[i][j];
                blockIndex++;
            }
        }

        return block;
    }


    public String describe() {
        String thinLine = "-".repeat(79) + "\n";
        String thickLine = "=".repeat(79) + "\n";
        StringBuilder stringBuilder = new StringBuilder(thickLine);

        for (int ri = 0; ri < 9; ri++) {
            for (int loopi = 0; loopi < 3; loopi++) {
                stringBuilder.append(" || ");
                for (int ci = 0; ci < 9; ci++) {
                    if (this.sudokuGame.board[ri][ci] != null) {
                        if (loopi == 1) {
                            stringBuilder.append(String.format(". %d . ", this.sudokuGame.board[ri][ci]));
                        } else {
                            stringBuilder.append("  .   ");
                        }
                    } else {
                        for (int pv = loopi * 3; pv < loopi * 3 + 3; pv++) {
                            if (this.getCell(ri, ci).contains(pv + 1)) {
                                stringBuilder.append(pv + 1).append(" ");
                            } else {
                                stringBuilder.append("  ");
                            }
                        }
                    }
                    stringBuilder.append(ci % 3 == 2 ? "|| " : "| ");
                }
                stringBuilder.append("\n");
            }
            stringBuilder.append(ri % 3 == 2 ? thickLine : thinLine);
        }
        return stringBuilder.toString();
    }
}

package nl.lucabergman;

public class SudokuGame {
    public Integer[][] board;

    public SudokuGame(Integer[][] board) {
        this.board = board;
    }

    public String describe() {
        StringBuilder out = new StringBuilder();
        String separator = "------+-------+------\n";

        for (int ri = 0; ri < 9; ri++) {
            if (ri > 0 && ri % 3 == 0) {
                out.append(separator);
            }
            for (int ci = 0; ci < 9; ci++) {
                if (ci > 0 && ci % 3 == 0) {
                    out.append("| ");
                }
                Integer cell = board[ri][ci];
                out.append(cell == null ? "." : cell).append(" ");
            }
            out.setLength(out.length() - 1);
            out.append('\n');
        }
        return out.toString();
    }

    public void makeMove(Coordinates cell, int value) throws Exception {
        this.board[cell.rowIndex()][cell.columnIndex()] = value;
        if (!isValidAt(cell)) {
            throw new Exception("Invalid move at: (" + cell.rowIndex() + ", " + cell.columnIndex() + ")");
        }
    }

    public boolean isValidAt(Coordinates cell) {
        // Check if a certain cell is valid. Use by making the move and then checking if it is valid.
        return isRowValid(cell.rowIndex()) && isColumnValid(cell.columnIndex()) && isSubsectionValid(cell.rowIndex(), cell.columnIndex());
    }

    public boolean isValid() {
        for (int i = 0; i < 9; i++) {
            // loop over rows and cols.
            if (!isRowValid(i)) return false;
            if (!isColumnValid(i)) return false;
        }
        for (int i = 0; i < 9; i += 3) {
            for (int j = 0; j < 9; j += 3) {
                if (!isSubsectionValid(i, j)) return false;
            }
        }

        return true;
    }

    public void setCell(Coordinates coordinates, Integer value) {
        this.board[coordinates.rowIndex()][coordinates.columnIndex()] = value;
    }

    public Integer getCell(Coordinates coordinates) {
        return this.board[coordinates.rowIndex()][coordinates.columnIndex()];
    }

    public Integer[] getRow(int rowIndex) {
        return this.board[rowIndex];
    }

    public Integer[] getCol(int colIndex) {
        Integer[] col = new Integer[9];
        for (int i = 0; i < 9; i++) {
            col[i] = this.board[i][colIndex];
        }
        return col;
    }

    public Integer[] getBlock(int bi) {
        int bri = (bi / 3) * 3;
        int bci = (bi % 3) * 3;

        Integer[] block = new Integer[9];
        int blockIndex = 0;

        for (int i = bri; i < bri + 3; i++) {
            for (int j = bci; j < bci + 3; j++) {
                block[blockIndex] = this.board[i][j];
                blockIndex++;
            }
        }

        return block;
    }

    private boolean hasDuplicates(Integer[] arr) {
        boolean[] seen = new boolean[10];

        for (Integer value : arr) {
            if (value == null) {
                continue;
            }
            if (seen[value]) {
                return true;
            }
            seen[value] = true;
        }

        return false;
    }

    private boolean isRowValid(int rowIndex) {
        return !hasDuplicates(this.board[rowIndex]);
    }

    private boolean isColumnValid(int colIndex) {
        Integer[] col = new Integer[9];
        for (int i = 0; i < 9; i++) {
            col[i] = this.board[i][colIndex];
        }
        return !hasDuplicates(col);
    }

    private boolean isSubsectionValid(int rowIndex, int colIndex) {
        // replace row and col index with block coordinates record.
        int blockRowIndex = Math.floorDiv(rowIndex, 3) * 3;
        int blockColIndex = Math.floorDiv(colIndex, 3) * 3;

        Integer[] block = new Integer[9];
        int blockIndex = 0;

        for (int i = blockRowIndex; i < blockRowIndex + 3; i++) {
            for (int j = blockColIndex; j < blockColIndex + 3; j++) {
                block[blockIndex] = this.board[i][j];
                blockIndex++;
            }
        }

        return !hasDuplicates(block);
    }
}

package nl.lucabergman;

public class SudokuGame {
    public Integer[][] board;

    public SudokuGame(Integer[][] board) {
        this.board = board;
    }

    @Override
    public String toString() {
        StringBuilder sudoku = new StringBuilder();

        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board[i].length; j++) {
                if (board[i][j] == null) {
                    sudoku.append("  ");
                } else {
                    sudoku.append(board[i][j]);
                    sudoku.append(" ");
                }
            }
            sudoku.append("\n");
        }

        return sudoku.toString();
    }

    public boolean isValidAt(int rowIndex, int colIndex) {
        return isRowValid(rowIndex) && isColumnValid(colIndex) && isSubsectionValid(rowIndex, colIndex);
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

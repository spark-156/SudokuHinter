package nl.lucabergman;

public record Coordinates(int rowIndex, int columnIndex) {
    public Coordinates getBlockStartCoordinates() {
        // top left corner of the block this coordinate is at.
        return new Coordinates(this.rowIndex() / 3 * 3, this.columnIndex() / 3 * 3);
    }
}

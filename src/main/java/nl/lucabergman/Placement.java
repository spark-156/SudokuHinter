package nl.lucabergman;

public record Placement(Coordinates cell, int value, HintLocation location, HintType type) implements Hint {
    public String describe() {
        return "Found %s: place %d at (%d, %d), forced by its %s"
                .formatted(type, value, cell.rowIndex(), cell.columnIndex(), location);
    }
}

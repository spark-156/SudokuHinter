package nl.lucabergman;

public record Hint(int rowIndex, int colIndex, int value, HintLocation hintLocation, HintType hintType) {
    public String describe() {
        return "Found %s: place %d at (%d, %d), forced by its %s"
                .formatted(hintType, value, rowIndex, colIndex, hintLocation);
    }
}

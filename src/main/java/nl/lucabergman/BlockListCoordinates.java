package nl.lucabergman;

public record BlockListCoordinates(int blockIndex, int listIndex) {
    public Coordinates getCoordinates() {
        return new Coordinates((blockIndex() / 3) * 3 + listIndex() / 3, (blockIndex() % 3) * 3 + listIndex() % 3);
    }
}

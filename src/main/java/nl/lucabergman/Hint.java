package nl.lucabergman;

public interface Hint {
    HintLocation location();
    HintType type();
    String describe();
}

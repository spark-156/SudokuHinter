package nl.lucabergman;

import java.util.Set;

public record Resctriction(Set<Coordinates> cells, Set<Integer> values, HintLocation location, HintType type) implements Hint {
    public String describe() {
        return "Found %s: %s can only go in %s within their %s, other candidates there are removed"
                .formatted(type, values, cells, location);
    }
}

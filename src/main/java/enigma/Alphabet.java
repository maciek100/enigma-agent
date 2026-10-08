package enigma;

/** Helpers for mapping between letters A-Z and indices 0-25. */
final class Alphabet {
    static final int SIZE = 26;

    private Alphabet() {
    }

    static int toIndex(char c) {
        char upper = Character.toUpperCase(c);
        if (upper < 'A' || upper > 'Z') {
            throw new IllegalArgumentException("Not a letter A-Z: '" + c + "'");
        }
        return upper - 'A';
    }

    static char toChar(int index) {
        return (char) ('A' + index);
    }

    static int mod(int value) {
        return Math.floorMod(value, SIZE);
    }

    /**
     * Parses a 26-letter wiring string into a permutation array.
     * Rejects strings that are not a permutation of A-Z.
     */
    static int[] parsePermutation(String wiring) {
        if (wiring.length() != SIZE) {
            throw new IllegalArgumentException("Wiring must have 26 letters: " + wiring);
        }
        int[] map = new int[SIZE];
        boolean[] seen = new boolean[SIZE];
        for (int i = 0; i < SIZE; i++) {
            int target = toIndex(wiring.charAt(i));
            if (seen[target]) {
                throw new IllegalArgumentException("Wiring is not a permutation: " + wiring);
            }
            seen[target] = true;
            map[i] = target;
        }
        return map;
    }

    static int[] invert(int[] map) {
        int[] inverse = new int[map.length];
        for (int i = 0; i < map.length; i++) {
            inverse[map[i]] = i;
        }
        return inverse;
    }
}

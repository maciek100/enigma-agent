package enigma;

import java.util.ArrayList;
import java.util.List;

/** The plugboard (Steckerbrett): up to 13 pairs of swapped letters. */
public final class Plugboard {
    private final int[] map = new int[Alphabet.SIZE];
    private final List<String> pairs = new ArrayList<>();

    public Plugboard() {
        for (int i = 0; i < map.length; i++) {
            map[i] = i;
        }
    }

    /**
     * Builds a plugboard from a spec such as {@code "AB CD EF"} (pairs may be separated by
     * spaces, commas or semicolons; dashes inside a pair are ignored: {@code "A-B,C-D"}).
     */
    public static Plugboard parse(String spec) {
        Plugboard board = new Plugboard();
        if (spec == null) {
            return board;
        }
        for (String token : spec.trim().split("[\\s,;]+")) {
            String pair = token.replace("-", "").replace("/", "");
            if (pair.isEmpty()) {
                continue;
            }
            if (pair.length() != 2) {
                throw new IllegalArgumentException("Plugboard pair must be two letters: " + token);
            }
            board.connect(pair.charAt(0), pair.charAt(1));
        }
        return board;
    }

    public void connect(char a, char b) {
        int x = Alphabet.toIndex(a);
        int y = Alphabet.toIndex(b);
        if (x == y) {
            throw new IllegalArgumentException("Cannot plug a letter to itself: " + a);
        }
        if (map[x] != x || map[y] != y) {
            throw new IllegalArgumentException(
                    "Letter already plugged: " + (map[x] != x ? Character.toUpperCase(a) : Character.toUpperCase(b)));
        }
        map[x] = y;
        map[y] = x;
        pairs.add("" + Alphabet.toChar(x) + Alphabet.toChar(y));
    }

    public int swap(int contact) {
        return map[contact];
    }

    public List<String> pairs() {
        return List.copyOf(pairs);
    }
}

package enigma;

import java.util.List;

/**
 * A three-rotor Enigma I.
 *
 * <p>Signal path for each key press: rotors step, then
 * plugboard → right → middle → left rotor → reflector → left → middle → right → plugboard.
 *
 * <p>Stepping follows the real ratchet mechanism, including the middle rotor's
 * "double step": the right rotor always steps; the middle rotor steps when the right
 * rotor is at its notch <em>or</em> when the middle rotor itself is at its notch
 * (in which case it also carries the left rotor).
 */
public final class EnigmaMachine {
    private final Reflector reflector;
    private final Rotor left;
    private final Rotor middle;
    private final Rotor right;
    private final Plugboard plugboard;

    public EnigmaMachine(Reflector reflector, Rotor left, Rotor middle, Rotor right, Plugboard plugboard) {
        if (left.type() == middle.type() || left.type() == right.type() || middle.type() == right.type()) {
            throw new IllegalArgumentException("Each rotor may be used only once");
        }
        this.reflector = reflector;
        this.left = left;
        this.middle = middle;
        this.right = right;
        this.plugboard = plugboard;
    }

    public static Builder builder() {
        return new Builder();
    }

    /** Advances the rotors as happens when a key is pressed, before the lamp lights. */
    void step() {
        boolean middleAtNotch = middle.atNotch();
        boolean rightAtNotch = right.atNotch();
        if (middleAtNotch) {
            left.step();
        }
        if (middleAtNotch || rightAtNotch) {
            middle.step();
        }
        right.step();
    }

    /** Presses one key and returns the lit lamp. */
    public char press(char key) {
        int c = Alphabet.toIndex(key);
        step();
        c = plugboard.swap(c);
        c = right.forward(c);
        c = middle.forward(c);
        c = left.forward(c);
        c = reflector.reflect(c);
        c = left.backward(c);
        c = middle.backward(c);
        c = right.backward(c);
        c = plugboard.swap(c);
        return Alphabet.toChar(c);
    }

    /**
     * Encrypts (or, equivalently, decrypts) text. Letters are upper-cased and
     * enciphered; every other character is copied through without stepping the rotors.
     */
    public String process(String text) {
        StringBuilder out = new StringBuilder(text.length());
        for (int i = 0; i < text.length(); i++) {
            char ch = text.charAt(i);
            char upper = Character.toUpperCase(ch);
            if (upper >= 'A' && upper <= 'Z') {
                out.append(press(upper));
            } else {
                out.append(ch);
            }
        }
        return out.toString();
    }

    /** The three letters currently visible in the windows, left to right. */
    public String windows() {
        return "" + left.windowLetter() + middle.windowLetter() + right.windowLetter();
    }

    public void setWindows(String letters) {
        if (letters.length() != 3) {
            throw new IllegalArgumentException("Expected three window letters: " + letters);
        }
        setPosition(left, letters.charAt(0));
        setPosition(middle, letters.charAt(1));
        setPosition(right, letters.charAt(2));
    }

    private static void setPosition(Rotor rotor, char letter) {
        int target = Alphabet.toIndex(letter);
        while (rotor.position() != target) {
            rotor.step();
        }
    }

    @Override
    public String toString() {
        List<String> pairs = plugboard.pairs();
        return String.format("Enigma I [UKW-%s] %s-%s-%s rings %s windows %s plugs %s",
                reflector.type(), left.type(), middle.type(), right.type(),
                "" + Alphabet.toChar(left.ring()) + Alphabet.toChar(middle.ring()) + Alphabet.toChar(right.ring()),
                windows(), pairs.isEmpty() ? "-" : String.join(" ", pairs));
    }

    /** Fluent configuration for an {@link EnigmaMachine}. */
    public static final class Builder {
        private ReflectorType reflector = ReflectorType.B;
        private RotorType[] rotors = {RotorType.I, RotorType.II, RotorType.III};
        private int[] rings = {0, 0, 0};
        private int[] positions = {0, 0, 0};
        private String plugboard = "";

        public Builder reflector(ReflectorType reflector) {
            this.reflector = reflector;
            return this;
        }

        /** Rotors in left-to-right order (slow, middle, fast). */
        public Builder rotors(RotorType left, RotorType middle, RotorType right) {
            this.rotors = new RotorType[] {left, middle, right};
            return this;
        }

        /** Ring settings as letters, e.g. {@code "AAA"} or {@code "BUL"}. */
        public Builder rings(String letters) {
            this.rings = parseThreeLetters(letters, "ring settings");
            return this;
        }

        /** Ring settings as 1-based numbers, e.g. {@code 1, 1, 1}. */
        public Builder rings(int left, int middle, int right) {
            this.rings = new int[] {checkRing(left) - 1, checkRing(middle) - 1, checkRing(right) - 1};
            return this;
        }

        /** Initial window letters (Grundstellung), e.g. {@code "AAA"}. */
        public Builder positions(String letters) {
            this.positions = parseThreeLetters(letters, "rotor positions");
            return this;
        }

        public Builder plugboard(String pairs) {
            this.plugboard = pairs == null ? "" : pairs;
            return this;
        }

        public EnigmaMachine build() {
            return new EnigmaMachine(
                    new Reflector(reflector),
                    new Rotor(rotors[0], rings[0], positions[0]),
                    new Rotor(rotors[1], rings[1], positions[1]),
                    new Rotor(rotors[2], rings[2], positions[2]),
                    Plugboard.parse(plugboard));
        }

        private static int checkRing(int ring) {
            if (ring < 1 || ring > 26) {
                throw new IllegalArgumentException("Ring setting must be 1-26: " + ring);
            }
            return ring;
        }

        private static int[] parseThreeLetters(String letters, String what) {
            String s = letters.trim();
            if (s.length() != 3) {
                throw new IllegalArgumentException("Expected three letters for " + what + ": " + letters);
            }
            return new int[] {Alphabet.toIndex(s.charAt(0)), Alphabet.toIndex(s.charAt(1)), Alphabet.toIndex(s.charAt(2))};
        }
    }
}

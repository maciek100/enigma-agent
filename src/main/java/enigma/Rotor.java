package enigma;

/**
 * A single rotor with its current position and ring setting (Ringstellung).
 *
 * <p>The signal enters at contact {@code c} (relative to the machine), is shifted by
 * {@code position - ring} into the rotor's own frame, passed through the fixed wiring,
 * and shifted back.
 */
public final class Rotor {
    private final RotorType type;
    private final int[] forward;
    private final int[] backward;
    private final boolean[] notch = new boolean[Alphabet.SIZE];
    private final int ring;
    private int position;

    /**
     * @param type     which rotor
     * @param ring     ring setting, 0-based (0 = A / 01)
     * @param position initial window letter, 0-based (0 = A)
     */
    public Rotor(RotorType type, int ring, int position) {
        this.type = type;
        this.forward = Alphabet.parsePermutation(type.wiring());
        this.backward = Alphabet.invert(forward);
        for (char c : type.notches().toCharArray()) {
            notch[Alphabet.toIndex(c)] = true;
        }
        this.ring = Alphabet.mod(ring);
        this.position = Alphabet.mod(position);
    }

    public RotorType type() {
        return type;
    }

    public int position() {
        return position;
    }

    public char windowLetter() {
        return Alphabet.toChar(position);
    }

    public int ring() {
        return ring;
    }

    /** True when the window shows a notch letter, i.e. the next step will carry left. */
    public boolean atNotch() {
        return notch[position];
    }

    public void step() {
        position = Alphabet.mod(position + 1);
    }

    public int forward(int contact) {
        return through(forward, contact);
    }

    public int backward(int contact) {
        return through(backward, contact);
    }

    private int through(int[] map, int contact) {
        int shift = position - ring;
        return Alphabet.mod(map[Alphabet.mod(contact + shift)] - shift);
    }
}

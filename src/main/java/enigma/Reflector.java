package enigma;

/** A fixed reflector. Its wiring must be a fixed-point-free involution. */
public final class Reflector {
    private final ReflectorType type;
    private final int[] map;

    public Reflector(ReflectorType type) {
        this.type = type;
        this.map = Alphabet.parsePermutation(type.wiring());
        for (int i = 0; i < map.length; i++) {
            if (map[i] == i || map[map[i]] != i) {
                throw new IllegalStateException("Reflector " + type + " is not a proper reflector");
            }
        }
    }

    public ReflectorType type() {
        return type;
    }

    public int reflect(int contact) {
        return map[contact];
    }
}

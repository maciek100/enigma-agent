package enigma;

/** Reflectors (Umkehrwalzen) used with the Enigma I. */
public enum ReflectorType {
    A("EJMZALYXVBWFCRQUONTSPIKHGD"),
    B("YRUHQSLDPXNGOKMIEBFZCWVJAT"),
    C("FVPJIAOYEDRZXWGCTKUQSBNMHL");

    private final String wiring;

    ReflectorType(String wiring) {
        this.wiring = wiring;
    }

    public String wiring() {
        return wiring;
    }

    public static ReflectorType parse(String name) {
        String n = name.trim().toUpperCase();
        if (n.startsWith("UKW-")) {
            n = n.substring(4);
        }
        try {
            return valueOf(n);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Unknown reflector: " + name + " (expected A, B or C)");
        }
    }
}

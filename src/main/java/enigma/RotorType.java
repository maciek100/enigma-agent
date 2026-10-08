package enigma;

/** The five rotors (Walzen) issued with the Wehrmacht/Luftwaffe Enigma I. */
public enum RotorType {
    I("EKMFLGDQVZNTOWYHXUSPAIBRCJ", "Q"),
    II("AJDKSIRUXBLHWTMCQGZNPYFVOE", "E"),
    III("BDFHJLCPRTXVZNYEIWGAKMUSQO", "V"),
    IV("ESOVPZJAYQUIRHXLNFTGKDCMWB", "J"),
    V("VZBRGITYUPSDNHLXAWMJQOFECK", "Z");

    private final String wiring;
    private final String notches;

    RotorType(String wiring, String notches) {
        this.wiring = wiring;
        this.notches = notches;
    }

    public String wiring() {
        return wiring;
    }

    /** Window letters at which this rotor causes its left neighbour to step. */
    public String notches() {
        return notches;
    }

    /** Parses a rotor name such as "I", "iv" or "5". */
    public static RotorType parse(String name) {
        String n = name.trim().toUpperCase();
        switch (n) {
            case "1": return I;
            case "2": return II;
            case "3": return III;
            case "4": return IV;
            case "5": return V;
            default:
                try {
                    return valueOf(n);
                } catch (IllegalArgumentException e) {
                    throw new IllegalArgumentException("Unknown rotor: " + name + " (expected I-V)");
                }
        }
    }
}

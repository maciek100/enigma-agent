package enigma;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

/**
 * Command-line front end.
 *
 * <pre>
 * java -jar enigma.jar [options] [text...]
 *   -r, --rotors     I,II,III   rotors left to right (I-V)
 *   -u, --reflector  B          reflector A, B or C
 *   -g, --rings      AAA        ring settings as letters, or numbers like 1,1,1
 *   -p, --positions  AAA        initial window letters
 *   -s, --plugboard  "AB CD"    plugboard pairs
 *   -v, --verbose               print the machine setup to stderr
 * </pre>
 *
 * With no text arguments, text is read line by line from standard input and the
 * machine state carries over from line to line.
 */
public final class Main {
    private Main() {
    }

    public static void main(String[] args) throws IOException {
        try {
            run(args);
        } catch (IllegalArgumentException e) {
            System.err.println("error: " + e.getMessage());
            System.err.println("try --help");
            System.exit(2);
        }
    }

    private static void run(String[] args) throws IOException {
        EnigmaMachine.Builder builder = EnigmaMachine.builder();
        StringBuilder text = new StringBuilder();
        boolean verbose = false;

        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            switch (arg) {
                case "-h":
                case "--help":
                    printUsage();
                    return;
                case "-r":
                case "--rotors": {
                    String[] names = value(args, ++i, arg).split("[\\s,\\-]+");
                    if (names.length != 3) {
                        throw new IllegalArgumentException("Expected three rotors, e.g. I,II,III");
                    }
                    builder.rotors(RotorType.parse(names[0]), RotorType.parse(names[1]), RotorType.parse(names[2]));
                    break;
                }
                case "-u":
                case "--reflector":
                    builder.reflector(ReflectorType.parse(value(args, ++i, arg)));
                    break;
                case "-g":
                case "--rings":
                    applyRings(builder, value(args, ++i, arg));
                    break;
                case "-p":
                case "--positions":
                    builder.positions(value(args, ++i, arg));
                    break;
                case "-s":
                case "--plugboard":
                    builder.plugboard(value(args, ++i, arg));
                    break;
                case "-v":
                case "--verbose":
                    verbose = true;
                    break;
                default:
                    if (arg.startsWith("-") && arg.length() > 1) {
                        throw new IllegalArgumentException("Unknown option: " + arg);
                    }
                    if (text.length() > 0) {
                        text.append(' ');
                    }
                    text.append(arg);
            }
        }

        EnigmaMachine machine = builder.build();
        if (verbose) {
            System.err.println(machine);
        }

        if (text.length() > 0) {
            System.out.println(machine.process(text.toString()));
        } else {
            BufferedReader in = new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8));
            String line;
            while ((line = in.readLine()) != null) {
                System.out.println(machine.process(line));
            }
        }

        if (verbose) {
            System.err.println("final windows: " + machine.windows());
        }
    }

    private static void applyRings(EnigmaMachine.Builder builder, String spec) {
        String s = spec.trim();
        if (s.matches("[A-Za-z]{3}")) {
            builder.rings(s);
            return;
        }
        String[] parts = s.split("[\\s,\\-]+");
        if (parts.length != 3) {
            throw new IllegalArgumentException("Ring settings must be like AAA or 1,1,1: " + spec);
        }
        try {
            builder.rings(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]), Integer.parseInt(parts[2]));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Ring settings must be like AAA or 1,1,1: " + spec);
        }
    }

    private static String value(String[] args, int index, String option) {
        if (index >= args.length) {
            throw new IllegalArgumentException("Missing value for " + option);
        }
        return args[index];
    }

    private static void printUsage() {
        System.out.println(String.join("\n",
                "Enigma I simulator",
                "",
                "usage: java -jar enigma.jar [options] [text...]",
                "",
                "  -r, --rotors     I,II,III   rotors left to right, chosen from I-V (default I,II,III)",
                "  -u, --reflector  B          reflector A, B or C (default B)",
                "  -g, --rings      AAA        ring settings as letters or numbers, e.g. 1,1,1 (default AAA)",
                "  -p, --positions  AAA        initial window letters (default AAA)",
                "  -s, --plugboard  \"AB CD\"    plugboard pairs (default none)",
                "  -v, --verbose               print the machine setup and final windows to stderr",
                "  -h, --help                  show this help",
                "",
                "Letters are enciphered; other characters pass through unchanged and do not step",
                "the rotors. Without text arguments, input is read from stdin.",
                "Enigma is reciprocal: run the ciphertext through the same settings to decrypt."));
    }
}

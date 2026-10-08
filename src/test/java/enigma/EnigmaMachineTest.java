package enigma;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Random;
import org.junit.jupiter.api.Test;

class EnigmaMachineTest {

    private static EnigmaMachine defaultMachine() {
        return EnigmaMachine.builder().build();
    }

    @Test
    void knownVectorAllA() {
        // Rotors I-II-III, UKW-B, rings AAA, start AAA, no plugs: AAAAA -> BDZGO.
        assertEquals("BDZGO", defaultMachine().process("AAAAA"));
    }

    @Test
    void decryptionIsSymmetric() {
        String plain = "WETTERVORHERSAGEBISKAYA";
        EnigmaMachine enc = EnigmaMachine.builder()
                .rotors(RotorType.IV, RotorType.II, RotorType.V)
                .reflector(ReflectorType.C)
                .rings("GMY")
                .positions("DHO")
                .plugboard("AV BS CG DL FU HZ IN KM OW RX")
                .build();
        EnigmaMachine dec = EnigmaMachine.builder()
                .rotors(RotorType.IV, RotorType.II, RotorType.V)
                .reflector(ReflectorType.C)
                .rings("GMY")
                .positions("DHO")
                .plugboard("AV BS CG DL FU HZ IN KM OW RX")
                .build();
        String cipher = enc.process(plain);
        assertNotEquals(plain, cipher);
        assertEquals(plain, dec.process(cipher));
    }

    @Test
    void letterNeverEncryptsToItself() {
        EnigmaMachine machine = EnigmaMachine.builder().plugboard("AZ BY CX").build();
        Random random = new Random(42);
        for (int i = 0; i < 20_000; i++) {
            char c = (char) ('A' + random.nextInt(26));
            assertNotEquals(c, machine.press(c));
        }
    }

    @Test
    void rightRotorStepsBeforeEncryption() {
        EnigmaMachine machine = defaultMachine();
        machine.press('A');
        assertEquals("AAB", machine.windows());
    }

    @Test
    void middleRotorTurnsOverAtNotch() {
        // Rotor III has its notch at V: moving V -> W carries the middle rotor.
        EnigmaMachine machine = EnigmaMachine.builder().positions("AAV").build();
        machine.press('A');
        assertEquals("ABW", machine.windows());
    }

    @Test
    void middleRotorDoubleSteps() {
        // Rotor II (middle) notch is E. From ADU: ADV, AEW, BFX.
        EnigmaMachine machine = EnigmaMachine.builder().positions("ADU").build();
        machine.press('A');
        assertEquals("ADV", machine.windows());
        machine.press('A');
        assertEquals("AEW", machine.windows());
        machine.press('A');
        assertEquals("BFX", machine.windows());
        machine.press('A');
        assertEquals("BFY", machine.windows());
    }

    @Test
    void stepPeriodIs16900() {
        // The double step means the full cycle is 26 * 25 * 26 key presses, not 26^3.
        EnigmaMachine machine = defaultMachine();
        int presses = 0;
        do {
            machine.step();
            presses++;
        } while (!machine.windows().equals("AAA"));
        assertEquals(26 * 25 * 26, presses);
    }

    @Test
    void ringSettingShiftsOutputButNotNotchLetter() {
        EnigmaMachine plain = defaultMachine();
        EnigmaMachine ringed = EnigmaMachine.builder().rings("BBB").build();
        assertNotEquals(plain.process("AAAAA"), ringed.process("AAAAA"));

        // The notch is on the ring, so turnover still happens when the window shows V.
        EnigmaMachine machine = EnigmaMachine.builder().rings(5, 10, 20).positions("AAV").build();
        machine.press('A');
        assertEquals("ABW", machine.windows());
    }

    @Test
    void ringSettingEquivalentToShiftedWiringAndPosition() {
        // Advancing both ring and start position of every rotor by one leaves the
        // internal wiring offset unchanged; only the turnover timing differs, which
        // does not occur within the first few letters from these start positions.
        EnigmaMachine a = EnigmaMachine.builder().rings("AAA").positions("AAA").build();
        EnigmaMachine b = EnigmaMachine.builder().rings("BBB").positions("BBB").build();
        assertEquals(a.process("HELLOWORLD"), b.process("HELLOWORLD"));
    }

    @Test
    void plugboardSwapsInputAndOutput() {
        String withoutPlugs = defaultMachine().process("AAAAA"); // BDZGO
        // Plugging B<->X swaps the first output letter (B) to X; no other letter of
        // input or output is affected.
        EnigmaMachine swapped = EnigmaMachine.builder().plugboard("BX").build();
        String result = swapped.process("AAAAA");
        assertEquals('X', result.charAt(0));
        assertEquals(withoutPlugs.substring(1), result.substring(1));
    }

    @Test
    void nonLettersPassThroughWithoutStepping() {
        EnigmaMachine machine = defaultMachine();
        assertEquals("BD ZG-O!", machine.process("aa aa-a!"));
        assertEquals("AAF", machine.windows());
    }

    @Test
    void setWindowsResetsPosition() {
        EnigmaMachine machine = defaultMachine();
        String first = machine.process("ENIGMA");
        machine.setWindows("AAA");
        assertEquals(first, machine.process("ENIGMA"));
    }

    @Test
    void rejectsInvalidConfiguration() {
        assertThrows(IllegalArgumentException.class,
                () -> EnigmaMachine.builder().rotors(RotorType.I, RotorType.I, RotorType.III).build());
        assertThrows(IllegalArgumentException.class, () -> EnigmaMachine.builder().plugboard("AB AC").build());
        assertThrows(IllegalArgumentException.class, () -> EnigmaMachine.builder().plugboard("AA").build());
        assertThrows(IllegalArgumentException.class, () -> EnigmaMachine.builder().plugboard("ABC").build());
        assertThrows(IllegalArgumentException.class, () -> EnigmaMachine.builder().positions("AB").build());
        assertThrows(IllegalArgumentException.class, () -> EnigmaMachine.builder().rings(0, 1, 1).build());
        assertThrows(IllegalArgumentException.class, () -> RotorType.parse("VI"));
        assertThrows(IllegalArgumentException.class, () -> ReflectorType.parse("D"));
    }

    @Test
    void allReflectorsAreValidInvolutions() {
        for (ReflectorType type : ReflectorType.values()) {
            new Reflector(type); // throws if not a fixed-point-free involution
        }
    }

    @Test
    void parsesRotorAndReflectorNames() {
        assertEquals(RotorType.IV, RotorType.parse("iv"));
        assertEquals(RotorType.V, RotorType.parse("5"));
        assertEquals(ReflectorType.B, ReflectorType.parse("UKW-B"));
    }
}

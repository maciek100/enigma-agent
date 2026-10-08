# Enigma I simulator

A Java simulator of the three-rotor Enigma I used by the German Army and Air Force.

## Features

- Rotors I–V with their original wirings and turnover notches
- Reflectors (UKW) A, B and C
- Ring settings (Ringstellung) and starting positions (Grundstellung)
- Plugboard (Steckerbrett) with up to 13 pairs
- Accurate stepping, including the middle rotor's double step
- Command-line front end and a small Java API

## Build and test

Requires Java 17+ and Maven.

```sh
mvn package
```

## Command line

```sh
java -jar target/enigma-simulator-1.0.0.jar [options] [text...]
```

| Option | Default | Meaning |
| --- | --- | --- |
| `-r`, `--rotors I,II,III` | `I,II,III` | rotors left to right |
| `-u`, `--reflector B` | `B` | reflector A, B or C |
| `-g`, `--rings AAA` | `AAA` | ring settings as letters or numbers (`1,1,1`) |
| `-p`, `--positions AAA` | `AAA` | starting window letters |
| `-s`, `--plugboard "AB CD"` | none | plugboard pairs |
| `-v`, `--verbose` | off | print setup and final windows to stderr |

Without text arguments the program reads stdin line by line, carrying the rotor
state across lines. Letters are enciphered; any other character is passed through
and does not step the rotors.

```sh
$ java -jar target/enigma-simulator-1.0.0.jar AAAAA
BDZGO

$ java -jar target/enigma-simulator-1.0.0.jar -r IV,II,V -u C -g GMY -p DHO \
      -s "AV BS CG DL FU HZ IN KM OW RX" "Hello, Enigma world"
SDHIJ, RMWIZI KCWWI
```

Enigma is reciprocal, so decrypting is running the ciphertext through a machine
with the same settings.

## Java API

```java
EnigmaMachine machine = EnigmaMachine.builder()
        .rotors(RotorType.I, RotorType.II, RotorType.III)
        .reflector(ReflectorType.B)
        .rings("AAA")
        .positions("AAA")
        .plugboard("AB CD")
        .build();

String cipher = machine.process("HELLO");
```

## Design

- `Rotor` holds a rotor's wiring, ring setting and position. A signal at contact
  `c` passes through `wiring[c + position - ring] - (position - ring)` (mod 26)
  going in, and through the inverse wiring coming back.
- `EnigmaMachine.step()` runs before every key press: the right rotor always
  steps; the middle rotor steps if the right rotor is at its notch or if the
  middle rotor is itself at its notch (the double step), and in the latter case
  the left rotor steps too.
- The signal path is plugboard → right → middle → left → reflector → left →
  middle → right → plugboard.

package org.github.abarhub.taptempojava;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;

class TapTempoTest {

    private ByteArrayOutputStream baos;
    private PrintStream ps;
    private PrintStream old;
    private Locale defaultLocale;

    @BeforeEach
    void setUp() {
        defaultLocale = Locale.getDefault();
        Locale.setDefault(Locale.ENGLISH);
        baos = new ByteArrayOutputStream();
        ps = new PrintStream(baos);
        old = System.out;
        System.setOut(ps);
        TapTempo.setClock(Clock.systemUTC());
    }

    @AfterEach
    void tearDown() {
        System.setOut(old);
        TapTempo.setClock(Clock.systemUTC());
        Locale.setDefault(defaultLocale);
    }

    @Test
    void runHelp() throws Exception {
        // Arrange

        // Act
        TapTempo.ExitException exception = assertThrows(TapTempo.ExitException.class, () -> TapTempo.run(new String[]{"-h"}));

        // Assert
        assertEquals(0, exception.getExitCode());
        String result = baos.toString();
        assertIterableEquals(split("""
                usage: TempoTap
                 -h,--help                Display this help message.
                 -p,--precision <arg>     Set the decimal precision of the tempo display.
                                          Default is 0 digits, max is 5 digits.
                 -r,--reset-time <arg>    Set the time in second to reset the computation.
                                          Default is 5 seconds.
                 -s,--sample-size <arg>   Set the number of samples needed to compute the
                                          tempo. Default is 5 samples.
                 -v,--version             Display the version.
                """), split(result), () -> "result=" + result);
    }

    @Test
    void runVersion() throws Exception {
        // Arrange

        // Act
        TapTempo.ExitException exception = assertThrows(TapTempo.ExitException.class, () -> TapTempo.run(new String[]{"-v"}));

        // Assert
        assertEquals(0, exception.getExitCode());
        String result = baos.toString();
        assertIterableEquals(split("""
                Version: 1.0.0"""), split(result));
    }

    @Test
    void runInvalideParameter() throws Exception {
        // Arrange

        // Act
        TapTempo.ExitException exception = assertThrows(TapTempo.ExitException.class, () -> TapTempo.run(new String[]{"-p", "aaa"}));

        // Assert
        assertEquals(1, exception.getExitCode());
        String result = baos.toString();
        assertIterableEquals(split("""
                class java.lang.NumberFormatException: For input string: "aaa"
                usage: TempoTap
                 -h,--help                Display this help message.
                 -p,--precision <arg>     Set the decimal precision of the tempo display.
                                          Default is 0 digits, max is 5 digits.
                 -r,--reset-time <arg>    Set the time in second to reset the computation.
                                          Default is 5 seconds.
                 -s,--sample-size <arg>   Set the number of samples needed to compute the
                                          tempo. Default is 5 samples.
                 -v,--version             Display the version.
                """), split(result));
    }


    @Test
    void run() throws Exception {
        // Arrange
        ByteArrayInputStream bais = new ByteArrayInputStream("\n\n\n\n\n\n\nq\n".getBytes(StandardCharsets.UTF_8));
        System.setIn(bais);
        TapTempo.setClock(new StubClock(Instant.ofEpochMilli(1636224354000L), Duration.ofSeconds(2), ZoneId.systemDefault()));

        // Act
        TapTempo.run(null);

        // Assert
        String result = baos.toString();
        var listResult = split(result);
        System.err.println("listResult=" + listResult);
        Assertions.assertAll(
                "listResult=" + listResult,
                () -> assertEquals(9, listResult.size()),
                () -> assertEquals("Hit enter key for each beat (q to quit).", listResult.get(0), "get(0)"),
                () -> assertEquals("[Hit enter key one more time to start bpm computation...]", listResult.get(1), "get(1)"),
                () -> assertEquals("Tempo: 30 bpm", listResult.get(2), "get(2)"),
                () -> assertEquals("Tempo: 30 bpm", listResult.get(3), "get(3)"),
                () -> assertEquals("Tempo: 30 bpm", listResult.get(4), "get(4)"),
                () -> assertEquals("Tempo: 30 bpm", listResult.get(5), "get(5)"),
                () -> assertEquals("Tempo: 30 bpm", listResult.get(6), "get(6)"),
                () -> assertEquals("Tempo: 30 bpm", listResult.get(7), "get(7)"),
                () -> assertEquals("Bye Bye!", listResult.get(8), "get(8)")
        );
    }

    @Test
    void compareDiffFalse() {
        // Arrange
        Instant instant = Instant.ofEpochMilli(1636224354000L);

        // Act
        boolean result = TapTempo.compareDiff(instant, instant.plusSeconds(2), 5);

        // Assert
        assertFalse(result);
    }

    @Test
    void compareDiffTrue() {
        // Arrange
        Instant instant = Instant.ofEpochMilli(1636224354000L);

        // Act
        boolean result = TapTempo.compareDiff(instant, instant.plusSeconds(7), 5);

        // Assert
        assertTrue(result);
    }

    @Test
    void computeBPM() {
        // Arrange
        Instant instant = Instant.ofEpochMilli(1636224354000L);

        // Acte
        double result = TapTempo.computeBPM(instant.plusSeconds(2), instant, 5);

        // Assert
        assertEquals(150.0, result, 0.001);
    }

    private List<String> split(String s) {
        if (s == null) {
            return List.of();
        } else {
            return List.of(s.split("""
                    \\r?\\n|\\r"""));
        }
    }
}
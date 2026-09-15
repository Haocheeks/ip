package hermes.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests what the console shows and how it reads a line.
 *
 * <p>Ui talks to System.in and System.out directly, so each case swaps them for
 * in-memory streams and puts the real ones back afterwards.
 */
public class UiTest {

    private static final String DIVIDER = "____________________________________________________________";
    private static final String LINE_BREAK = System.lineSeparator();

    private final InputStream originalIn = System.in;
    private final PrintStream originalOut = System.out;
    private final ByteArrayOutputStream printed = new ByteArrayOutputStream();

    @BeforeEach
    public void captureOutput() {
        System.setOut(new PrintStream(printed, true, StandardCharsets.UTF_8));
    }

    @AfterEach
    public void restoreStreams() {
        System.setOut(originalOut);
        System.setIn(originalIn);
    }

    private String printedText() {
        return printed.toString(StandardCharsets.UTF_8);
    }

    /** Returns a Ui reading the given text as if the user had typed it. */
    private Ui uiReading(String typed) {
        System.setIn(new ByteArrayInputStream(typed.getBytes(StandardCharsets.UTF_8)));
        return new Ui();
    }

    @Test
    public void show_message_strippedAndFramedByDividers() {
        uiReading("").show("\n  It is recorded.  \n");

        assertEquals(DIVIDER + LINE_BREAK + "It is recorded." + LINE_BREAK
                + DIVIDER + LINE_BREAK + LINE_BREAK, printedText());
    }

    @Test
    public void showError_message_framedTheSameWayAsAnyReply() {
        uiReading("").showError("Alas, no task numbered 9 is to be found.");

        assertEquals(DIVIDER + LINE_BREAK + "Alas, no task numbered 9 is to be found." + LINE_BREAK
                + DIVIDER + LINE_BREAK + LINE_BREAK, printedText());
    }

    @Test
    public void showWelcome_printsGreetingBetweenDividers() {
        uiReading("").showWelcome();

        String shown = printedText();
        assertTrue(shown.startsWith(DIVIDER + LINE_BREAK));
        assertTrue(shown.contains(Ui.GREETING + LINE_BREAK));
        assertTrue(shown.endsWith(DIVIDER + LINE_BREAK));
    }

    @Test
    public void readCommand_typedLines_readInOrderWithoutSurroundingSpaces() {
        Ui ui = uiReading("  todo read  \nlist\n");

        assertTrue(ui.hasNextCommand());
        assertEquals("todo read", ui.readCommand());
        assertTrue(ui.hasNextCommand());
        assertEquals("list", ui.readCommand());
        assertFalse(ui.hasNextCommand());
    }

    @Test
    public void formatLoadingError_oneLine_speaksOfIt() {
        assertEquals("""
                Alas, I could not read 1 line in my records, and have set it aside.
                Whatever I cannot read shall be lost when next I save, so look to
                data/Hermes.txt first, shouldst thou need it.
                """, Ui.formatLoadingError(1, "data/Hermes.txt"));
    }

    @Test
    public void formatLoadingError_severalLines_speaksOfThem() {
        assertEquals("""
                Alas, I could not read 3 lines in my records, and have set them aside.
                Whatever I cannot read shall be lost when next I save, so look to
                elsewhere.txt first, shouldst thou need it.
                """, Ui.formatLoadingError(3, "elsewhere.txt"));
    }
}

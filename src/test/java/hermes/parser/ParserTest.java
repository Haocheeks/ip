package hermes.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import hermes.HermesException;

public class ParserTest {

    private final Parser parser = new Parser();

    @Test
    public void parse_todoWithoutDescription_exceptionThrown() {
        HermesException exception = assertThrows(HermesException.class,
                () -> parser.parse("todo"));

        assertEquals("A todo needs a description, for example: " + Keyword.TODO.getExample(),
                exception.getMessage());
    }

    @Test
    public void parse_deadlineWithoutDescription_exceptionThrown() {
        HermesException exception = assertThrows(HermesException.class,
                () -> parser.parse("deadline"));

        assertEquals("A deadline needs a description, for example: " + Keyword.DEADLINE.getExample(),
                exception.getMessage());
    }

    @Test
    public void parse_deadlineWithoutByDate_exceptionThrown() {
        HermesException exception = assertThrows(HermesException.class,
                () -> parser.parse("deadline clean toilet"));

        assertEquals("A deadline needs a /by date, for example: " + Keyword.DEADLINE.getExample(),
                exception.getMessage());
    }

    @Test
    public void parse_eventWithoutDescription_exceptionThrown() {
        HermesException exception = assertThrows(HermesException.class,
                () -> parser.parse("event"));

        assertEquals("An event needs a description, for example: " + Keyword.EVENT.getExample(),
                exception.getMessage());
    }

    @Test
    public void parse_eventWithoutFromDate_exceptionThrown() {
        HermesException exception = assertThrows(HermesException.class,
                () -> parser.parse("event toilet cleaning"));

        assertEquals("An event needs a /from time, for example: " + Keyword.EVENT.getExample(),
                exception.getMessage());
    }
}
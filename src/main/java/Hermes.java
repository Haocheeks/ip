/**
 * A command-line task assistant.
 *
 * <p>This class assembles the parts and runs the conversation. It reads a line
 * through {@link Ui}, has {@link Parser} make sense of it, asks {@link LogBook}
 * to carry it out, and shows the answer. It holds no user-facing text and no
 * knowledge of how input is worded or how tasks are stored.
 */
public class Hermes {

    /** Where tasks are kept between runs. */
    private static final String DATA_PATH = "data/Hermes.txt";

    private static Ui ui = new Ui();

    private static Parser parser = new Parser();

    private static LogBook logBook = new LogBook(new Storage(DATA_PATH));

    public static void main(String[] args) {
        ui.showWelcome();
        warnAboutSkippedLines();

        boolean isRunning = true;

        // The "bye" case cannot break out of the loop from inside a switch,
        // so this flag ends the conversation instead.
        while (isRunning && ui.hasNextCommand()) {
            String input = ui.readCommand();

            if (input.isEmpty()) {
                continue;
            }

            Keyword instruction = parser.parseKeyword(input);
            String arguments = parser.parseArguments(input);

            try {
                switch (instruction) {
                    case BYE -> {
                        ui.showGoodbye();
                        isRunning = false;
                    }
                    case LIST -> ui.show(logBook.toString());
                    case MARK -> ui.show(logBook.mark(
                            parser.parseTaskNumber(arguments, instruction)));
                    case UNMARK -> ui.show(logBook.unmark(
                            parser.parseTaskNumber(arguments, instruction)));
                    case DELETE -> ui.show(logBook.delete(
                            parser.parseTaskNumber(arguments, instruction)));
                    case TODO -> ui.show(logBook.log(parser.parseToDo(arguments)));
                    case DEADLINE -> ui.show(logBook.log(parser.parseDeadline(arguments)));
                    case EVENT -> ui.show(logBook.log(parser.parseEvent(arguments)));
                    case DUE -> ui.show(logBook.listTaskDueBy(parser.parseDueCutoff(arguments)));
                    case SORT -> ui.show(logBook.sort());
                    case UNKNOWN -> ui.showUnknownCommand(parser.parseCommandWord(input));
                }
            } catch (HermesException e) {
                ui.showError(e.getMessage());
            }
        }
    }

    /**
     * Tells the user if any lines of the data file could not be understood
     * when Hermes started, and says nothing when they all loaded.
     */
    private static void warnAboutSkippedLines() {
        int skipped = logBook.getSkippedLines();

        if (skipped > 0) {
            ui.showLoadingError(skipped, DATA_PATH);
        }
    }
}

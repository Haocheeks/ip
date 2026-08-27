/**
 * A command-line task assistant.
 *
 * <p>This class assembles the parts and runs the conversation: read a line
 * through {@link Ui}, have {@link Parser} turn it into a {@link Command}, run
 * it, repeat. It does not know which commands exist, how they are worded, what
 * they do, or how tasks are stored.
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

        while (isRunning && ui.hasNextCommand()) {
            String input = ui.readCommand();

            if (input.isEmpty()) {
                continue;
            }

            try {
                Command command = parser.parse(input);
                command.execute(logBook, ui);
                isRunning = !command.isExit();
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

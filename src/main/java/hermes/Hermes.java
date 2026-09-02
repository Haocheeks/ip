package hermes;

import hermes.command.Command;
import hermes.parser.Parser;
import hermes.task.LogBook;
import hermes.task.Storage;
import hermes.ui.Ui;

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

    private Ui ui;
    private Parser parser;
    private LogBook logBook;

    public Hermes(String dataPath) {
        this.ui = new Ui();
        this.parser = new Parser();
        this.logBook = new LogBook(new Storage(dataPath));
    }

    /**
     * Starts Hermes and runs the conversation until the user says goodbye.
     *
     * @param args Command line arguments, which Hermes does not use.
     */
    public static void main(String[] args) {
        Hermes hermes = new Hermes("data/Hermes.txt");

        hermes.ui.showWelcome();
        hermes.warnAboutSkippedLines();

        boolean isRunning = true;

        while (isRunning && hermes.ui.hasNextCommand()) {
            String input = hermes.ui.readCommand();

            if (input.isEmpty()) {
                continue;
            }

            try {
                Command command = hermes.parser.parse(input);
                hermes.ui.show(command.execute(hermes.logBook));
                isRunning = !command.isExit();
            } catch (HermesException e) {
                hermes.ui.showError(e.getMessage());
            }
        }
    }

    /**
     * Tells the user if any lines of the data file could not be understood
     * when Hermes started, and says nothing when they all loaded.
     */
    protected String warnAboutSkippedLines() {
        int skipped = this.logBook.getSkippedLines();

        if (skipped > 0) {
            return ui.showLoadingError(skipped, DATA_PATH);
        }
        return "";
    }

    protected String getResponse(String input) {
        try {
            Command command = parser.parse(input);
            return command.execute(logBook);
        } catch (HermesException e) {
            return e.getMessage();
        }
    }
}

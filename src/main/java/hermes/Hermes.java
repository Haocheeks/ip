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

    /** Assembles the parts Hermes needs, keeping tasks in the usual place. */
    public Hermes() {
        this(DATA_PATH);
    }

    /**
     * Assembles the parts Hermes needs, reading tasks from a given file.
     *
     * <p>Naming the file is what makes Hermes testable: a test can point it at
     * a scratch file rather than the list the user is really keeping.
     *
     * @param dataPath where tasks are read from and written back to
     */
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
        Hermes hermes = new Hermes();

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
     *
     * @return the warning to show, empty if every line loaded
     */
    protected String warnAboutSkippedLines() {
        int skipped = this.logBook.getSkippedLines();

        if (skipped > 0) {
            return ui.showLoadingError(skipped, DATA_PATH);
        }
        return "";
    }

    /**
     * Runs one line of input and returns what should be shown for it.
     *
     * <p>A problem comes back as its message rather than as an exception, so a
     * caller with nowhere to report one, such as the window, can show a reply
     * and a complaint the same way.
     *
     * @param input one full line of input
     * @return the reply to show the user
     */
    protected String getResponse(String input) {
        try {
            Command command = parser.parse(input);
            return command.execute(logBook);
        } catch (HermesException e) {
            return e.getMessage();
        }
    }
}

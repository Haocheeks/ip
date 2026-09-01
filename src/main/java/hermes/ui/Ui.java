package hermes.ui;

import java.util.Scanner;

/**
 * Handles everything Hermes shows to the user and reads back from them.
 *
 * <p>Keeping the console in one class means the rest of Hermes works in plain
 * strings and never calls {@code System.out} directly, so how a reply is framed
 * can change here without touching the code that decides what to say.
 */
public class Ui {

    private static final String DIVIDER =
            "____________________________________________________________";

    private static final String WELCOME = """
            ____________________________________________________________
             _   _
            | | | | ___ _ __ _ __ ___   ___  ___
            | |_| |/ _ \\ '__| '_ ` _ \\ / _ \\/ __|
            |  _  |  __/ |  | | | | | |  __/\\__ \\
            |_| |_|\\___|_|  |_| |_| |_|\\___||___/
            Greetings! I am Hermes.
            How may I assist you today?
            ____________________________________________________________""";

    private final Scanner scanner = new Scanner(System.in);

    /** Prints the banner and greeting shown once when Hermes starts. */
    public void showWelcome() {
        System.out.println(WELCOME);
    }

    /**
     * Reports whether the user has typed anything more.
     *
     * @return false once input has run out, so the caller can stop reading
     */
    public boolean hasNextCommand() {
        return scanner.hasNextLine();
    }

    /**
     * Reads one line of input.
     *
     * @return the line with surrounding whitespace removed
     */
    public String readCommand() {
        return scanner.nextLine().trim();
    }

    /**
     * Prints a single message wrapped in divider lines, so every reply from
     * Hermes has the same shape.
     *
     * @param message the text to show to the user
     */
    public void show(String message) {
        System.out.println(DIVIDER);
        System.out.println(message.strip());
        System.out.println(DIVIDER);
        System.out.println();
    }

    /** Prints the parting message shown when the user types bye. */
    public void showGoodbye() {
        show("Goodbye, thank you for contacting me!");
    }

    /**
     * Reports that the user typed something Hermes does not recognise.
     *
     * @param keyword the word the user typed in place of a command
     */
    public void showUnknownCommand(String keyword) {
        show(String.format("Sorry, I am not familiar with the '%s' command.", keyword));
    }

    /**
     * Reports that some of the stored records could not be read.
     *
     * <p>Skipped lines are not held in memory, so the next save rewrites the
     * file without them. Saying so up front gives the user the chance to
     * repair the file before that happens.
     *
     * @param skippedLines how many lines were unreadable, always at least one
     * @param path where those records are stored, so the message can name it
     */
    public void showLoadingError(int skippedLines, String path) {
        show(String.format("""
                Sorry, I could not read %d line%s in my records and have skipped %s.
                Anything I cannot read is lost the next time I save, so please check
                %s first if you need it.
                """, skippedLines, skippedLines == 1 ? "" : "s",
                skippedLines == 1 ? "it" : "them", path));
    }

    /**
     * Reports something that went wrong.
     *
     * <p>Errors are framed exactly like any other reply; this exists so callers
     * can say which of the two they mean.
     *
     * @param message the explanation to show to the user
     */
    public void showError(String message) {
        show(message);
    }
}

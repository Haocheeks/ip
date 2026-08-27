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
            ____________________________________________________________
            
            """;

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

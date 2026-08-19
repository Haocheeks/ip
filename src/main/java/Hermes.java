import java.util.Scanner;

public class Hermes {

    private static final String EXIT_COMMAND = "bye"; // possible to use ENUM here
    private static final String LIST_COMMAND = "list";
    private static final String MARK_COMMAND = "mark";
    private static final String UNMARK_COMMAND = "unmark";
    private static final String DIVIDER =
            "____________________________________________________________";

    private static LogBook logBook = new LogBook();

    public static void main(String[] args) {
        String opening = """
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

        System.out.println(opening);

        Scanner scanner = new Scanner(System.in);
        boolean isRunning = true;

        // The "bye" case cannot break out of the loop from inside a switch,
        // so this flag ends the conversation instead.
        while (isRunning && scanner.hasNextLine()) {
            String command = scanner.nextLine().trim();

            if (command.isEmpty()) {
                continue;
            }

            String[] parts = command.split("\\s+", 2);
            String keyword = parts[0];

            switch (keyword) {
                case EXIT_COMMAND -> {
                    respond("Goodbye, thank you for contacting me!");
                    isRunning = false;
                }
                case LIST_COMMAND -> respond(logBook.toString());
                case MARK_COMMAND -> respond(changeStatus(parts, true));
                case UNMARK_COMMAND -> respond(changeStatus(parts, false));
                default -> respond(logBook.log(command));
            }
        }
    }

    /**
     * Handles the {@code mark} and {@code unmark} commands, which both expect a
     * task number as their argument.
     *
     * @param parts      the command split into keyword and argument
     * @param markAsDone true to mark the task complete, false to un-complete it
     * @return the message to show the user, including any error message
     */
    private static String changeStatus(String[] parts, boolean markAsDone) {
        String keyword = markAsDone ? MARK_COMMAND : UNMARK_COMMAND;

        if (parts.length < 2) {
            return String.format("Please tell me which task, for example: %s 1", keyword);
        }

        try {
            int index = Integer.parseInt(parts[1].trim()) - 1;
            return markAsDone ? logBook.mark(index) : logBook.unmark(index);
        } catch (NumberFormatException e) {
            return String.format("'%s' is not a task number.", parts[1]);
        }
    }

    /**
     * Prints a single message wrapped in divider lines, so every reply from
     * Hermes has the same shape.
     *
     * @param message the text to show to the user
     */
    private static void respond(String message) {
        System.out.println(DIVIDER);
        System.out.println(message.strip());
        System.out.println(DIVIDER);
        System.out.println();
    }
}

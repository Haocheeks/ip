import java.util.Scanner;

public class Hermes {

    private static final String EXIT_COMMAND = "bye"; // possible to use ENUM here
    private static final String LIST_COMMAND = "list";
    private static final String MARK_COMMAND = "mark";
    private static final String UNMARK_COMMAND = "unmark";
    private static final String TODO_COMMAND = "todo";
    private static final String DEADLINE_COMMAND = "deadline";
    private static final String EVENT_COMMAND = "event";
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
                case TODO_COMMAND -> respond(addToDo(parts));
                case DEADLINE_COMMAND -> respond(addDeadline(parts));
                case EVENT_COMMAND -> respond(addEvent(parts));
                default -> respond(String.format(
                        "Sorry, I do not know what '%s' means.", keyword));
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
     * Creates a {@link ToDo} from a command such as {@code todo borrow book}.
     *
     * @param parts the command split into keyword and argument
     * @return the message to show the user, including any error message
     */
    private static String addToDo(String[] parts) {
        if (parts.length < 2 || parts[1].isBlank()) {
            return "A todo needs a description, for example: todo borrow book";
        }

        return logBook.log(new ToDo(parts[1].trim()));
    }

    /**
     * Creates a {@link Deadline} from a command such as
     * {@code deadline return book /by Sunday}.
     *
     * @param parts the command split into keyword and argument
     * @return the message to show the user, including any error message
     */
    private static String addDeadline(String[] parts) {
        String example = "for example: deadline complete tutorial /by Sunday";

        if (parts.length < 2 || parts[1].isBlank()) {
            return "A deadline needs a description, " + example;
        }

        String[] fields = parts[1].split("\\s*/by\\s*", 2);

        if (fields.length < 2) {
            return "A deadline needs a /by date, " + example;
        }

        String description = fields[0].trim();
        String by = fields[1].trim();

        if (description.isEmpty() || by.isEmpty()) {
            return "A deadline needs both a description and a /by date, " + example;
        }

        return logBook.log(new Deadline(description, by));
    }

    /**
     * Creates an {@link Event} from a command such as
     * {@code event project meeting /from Mon 2pm /to 4pm}.
     *
     * @param parts the command split into keyword and argument
     * @return the message to show the user, including any error message
     */
    private static String addEvent(String[] parts) {
        String example = "for example: event project meeting /from Mon 2pm /to 4pm";

        if (parts.length < 2 || parts[1].isBlank()) {
            return "An event needs a description, " + example;
        }

        String[] afterFrom = parts[1].split("\\s*/from\\s*", 2);

        if (afterFrom.length < 2) {
            return "An event needs a /from time, " + example;
        }

        String[] fromTo = afterFrom[1].split("\\s*/to\\s*", 2);

        if (fromTo.length < 2) {
            return "An event needs a /to time, " + example;
        }

        String description = afterFrom[0].trim();
        String start = fromTo[0].trim();
        String end = fromTo[1].trim();

        if (description.isEmpty() || start.isEmpty() || end.isEmpty()) {
            return "An event needs a description, a /from time and a /to time, " + example;
        }

        return logBook.log(new Event(description, start, end));
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

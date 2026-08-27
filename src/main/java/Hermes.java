import java.time.LocalDateTime;
import java.util.Scanner;

public class Hermes {

    private static final String DIVIDER =
            "____________________________________________________________";

    /** The character used to separate fields in the data file. */
    private static final String SEPARATOR = "|";

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
        warnAboutSkippedLines();

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
            Command instruction = Command.fromKeyword(keyword);

            try {
                switch (instruction) {
                    case BYE -> {
                        respond("Goodbye, thank you for contacting me!");
                        isRunning = false;
                    }
                    case LIST -> respond(logBook.toString());
                    case MARK, UNMARK -> respond(changeStatus(instruction, parts));
                    case DELETE -> respond(delete(parts));
                    case TODO -> respond(addToDo(parts));
                    case DEADLINE -> respond(addDeadline(parts));
                    case EVENT -> respond(addEvent(parts));
                    case DUE -> respond(listTasksDueBy(command));
                    case UNKNOWN -> respond(String.format(
                            "Sorry, I am not familiar with the '%s' command.", keyword));
                }
            } catch (HermesException e) {
                respond(e.getMessage());
            }
        }
    }

    /**
     * Tells the user if any lines of the data file could not be understood
     * when Hermes started.
     *
     * <p>Skipped lines are not held in memory, so the next save rewrites the
     * file without them. Saying so up front gives the user the chance to
     * repair the file before that happens.
     */
    private static void warnAboutSkippedLines() {
        int skipped = logBook.getSkippedLines();

        if (skipped == 0) {
            return;
        }

        respond(String.format("""
                Sorry, I could not read %d line%s in my records and have skipped %s.
                Anything I cannot read is lost the next time I save, so please check
                data/Hermes.txt first if you need it.
                """, skipped, skipped == 1 ? "" : "s", skipped == 1 ? "it" : "them"));
    }

    /**
     * Handles the {@code mark} and {@code unmark} commands, which both expect a
     * task number as their argument.
     *
     * @param instruction either {@link Command#MARK} or {@link Command#UNMARK}
     * @param parts the command split into keyword and argument
     * @return the message confirming the change
     * @throws HermesException if the task number is missing or not valid
     */
    private static String changeStatus(Command instruction, String[] parts) throws HermesException {
        boolean markAsDone = instruction == Command.MARK;

        if (parts.length < 2) {
            throw new HermesException(
                    "Please tell me which task, for example: " + instruction.getExample());
        }

        try {
            int index = Integer.parseInt(parts[1].trim()) - 1;
            return markAsDone ? logBook.mark(index) : logBook.unmark(index);
        } catch (NumberFormatException e) {
            throw new HermesException(String.format("'%s' is not a task number.", parts[1]));
        }
    }

    /**
     * Handles the {@code delete} command, which expects a task number.
     *
     * @param parts the command split into keyword and argument
     * @return the message confirming the removal
     * @throws HermesException if the task number is missing or not valid
     */
    private static String delete(String[] parts) throws HermesException {

        if (parts.length < 2) {
            throw new HermesException("Please tell me which task you will like to delete, for example: "
                    + Command.DELETE.getExample());
        }

        try {
            int index = Integer.parseInt(parts[1].trim()) - 1;
            return logBook.delete(index);
        } catch (NumberFormatException e) {
            throw new HermesException(String.format("'%s' is not a task number.", parts[1]));
        }

    }

    /**
     * Rejects any user-supplied field containing the '|' character.
     *
     * <p>Tasks are stored as pipe-separated fields, so a '|' inside a field
     * would make the saved line impossible to read back correctly. Refusing it
     * here means Hermes never writes a line it cannot understand later.
     *
     * @param fields the parts of the task that will be written to file
     * @throws HermesException if any field contains the separator
     */
    private static void rejectSeparator(String... fields) throws HermesException {
        for (String field : fields) {
            if (field.contains(SEPARATOR)) {
                throw new HermesException(String.format(
                        "Sorry, a task cannot contain '%s', as I use it to separate "
                                + "fields when saving your tasks.", SEPARATOR));
            }
        }
    }

    /**
     * Creates a {@link ToDo} from a command such as {@code todo borrow book}.
     *
     * @param parts the command split into keyword and argument
     * @return the message to show the user
     * @throws HermesException if a todo description is missing
     */
    private static String addToDo(String[] parts) throws HermesException {
        if (parts.length < 2 || parts[1].isBlank()) {
            throw new HermesException("A todo needs a description, for example: "
                    + Command.TODO.getExample());
        }

        String description = parts[1].trim();
        rejectSeparator(description);

        return logBook.log(new ToDo(description));
    }

    /**
     * Creates a {@link Deadline} from a command such as
     * {@code deadline return book /by Sunday}.
     *
     * @param parts the command split into keyword and argument
     * @return the message to show the user
     * @throws HermesException if any name or deadline is missing
     */
    private static String addDeadline(String[] parts) throws HermesException {
        String example = "for example: " + Command.DEADLINE.getExample();

        if (parts.length < 2 || parts[1].isBlank()) {
            throw new HermesException("A deadline needs a description, " + example);
        }

        String[] fields = parts[1].split("\\s*/by\\s*", 2);

        if (fields.length < 2) {
            throw new HermesException("A deadline needs a /by date, " + example);
        }

        String description = fields[0].trim();

        String by = fields[1].trim();

        if (description.isEmpty() || by.isEmpty()) {
            throw new HermesException("A deadline needs both a description and a /by date, " + example);
        }

        rejectSeparator(description, by);

        LocalDateTime deadline = DateTimeFormat.parseTime(by);

        return logBook.log(new Deadline(description, deadline));
    }

    /**
     * Creates an {@link Event} from a command such as
     * {@code event project meeting /from Mon 2pm /to 4pm}.
     *
     * @param parts the command split into keyword and argument
     * @return the message to show the user
     * @throws HermesException if any name, start or end time is missing
     */
    private static String addEvent(String[] parts) throws HermesException {
        String example = "for example: " + Command.EVENT.getExample();

        if (parts.length < 2 || parts[1].isBlank()) {
            throw new HermesException("An event needs a description, " + example);
        }

        String[] afterFrom = parts[1].split("\\s*/from\\s*", 2);

        if (afterFrom.length < 2) {
            throw new HermesException("An event needs a /from time, " + example);
        }

        String[] fromTo = afterFrom[1].split("\\s*/to\\s*", 2);

        if (fromTo.length < 2) {
            throw new HermesException("An event needs a /to time, " + example);
        }

        String description = afterFrom[0].trim();
        String start = fromTo[0].trim();
        String end = fromTo[1].trim();

        if (description.isEmpty() || start.isEmpty() || end.isEmpty()) {
            throw new HermesException("An event needs a description, a /from time and a /to time, " + example);
        }

        LocalDateTime startDateTime =  DateTimeFormat.parseTime(start);
        LocalDateTime endDateTime =  DateTimeFormat.parseTime(end);

        rejectSeparator(description, start, end);

        return logBook.log(new Event(description, startDateTime, endDateTime));
    }

    private static String listTasksDueBy(String command) throws HermesException {
        String example = "for example: " + Command.DUE.getExample();

        String[] parts = command.split("\\s*/by\\s*", 2);

        if (parts.length < 2 || parts[1].isBlank()) {
            throw new HermesException("A due needs a /by date, " + example);
        }

        String by = parts[1].trim();

        LocalDateTime deadline = DateTimeFormat.parseTime(by);

        return logBook.listTaskDueBy(deadline);
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

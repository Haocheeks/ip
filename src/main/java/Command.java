/**
 * The commands Hermes understands, each paired with the keyword the user types
 * and an example of correct usage.
 *
 * <p>Keeping the example next to the keyword means error messages can quote the
 * correct usage without each helper method having to repeat it.
 */
public enum Command {
    BYE("bye", "bye"),
    LIST("list", "list"),
    MARK("mark", "mark 1"),
    UNMARK("unmark", "unmark 1"),
    DELETE("delete", "delete 3"),
    TODO("todo", "todo borrow book"),
    DEADLINE("deadline", "deadline complete tutorial /by 27 Aug 2026 1500"),
    EVENT("event", "event project meeting /from 27 Aug 2026 1500 /to 27 Aug 2026 1630"),
    DUE("due", "due /by 28 Aug 2026 1600"),

    /** Anything the user typed that is not a recognised command. */
    UNKNOWN("", "");

    private final String keyword;
    private final String example;

    Command(String keyword, String example) {
        this.keyword = keyword;
        this.example = example;
    }

    /**
     * Returns an example of how this command is used, for error messages.
     *
     * @return the example usage
     */
    public String getExample() {
        return this.example;
    }

    /**
     * Finds the command matching a typed keyword.
     *
     * @param input the first word of what the user typed
     * @return the matching command, or {@link #UNKNOWN} if there is none
     */
    public static Command fromKeyword(String input) {
        for (Command command : values()) {
            if (command.keyword.equals(input)) {
                return command;
            }
        }

        return UNKNOWN;
    }
}

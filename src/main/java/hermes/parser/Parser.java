package hermes.parser;

import java.time.LocalDateTime;

import hermes.HermesException;
import hermes.command.AddCommand;
import hermes.command.ByeCommand;
import hermes.command.Command;
import hermes.command.DeleteCommand;
import hermes.command.DueCommand;
import hermes.command.ListCommand;
import hermes.command.MarkCommand;
import hermes.command.SortCommand;
import hermes.command.UnknownCommand;
import hermes.command.UnmarkCommand;
import hermes.task.Deadline;
import hermes.task.Event;
import hermes.task.Storage;
import hermes.task.Task;
import hermes.task.ToDo;

/**
 * Makes sense of what the user typed.
 *
 * <p>This class turns text into the values the rest of Hermes works with: a
 * {@link Keyword}, a task number, a {@link Task}, a cutoff date. It decides
 * whether input is usable and says so with a {@link HermesException}, but it
 * never acts on the result — the caller does that.
 */
public class Parser {

    /**
     * Turns one line of input into the command it asks for.
     *
     * <p>Everything the command needs is read and checked here, so a command
     * that comes back is ready to run. Input Hermes cannot use is reported as a
     * {@link HermesException} instead, and nothing is carried out.
     *
     * @param input one full line of input, already trimmed
     * @return the command the user asked for
     * @throws HermesException if the input names a command but cannot supply it
     */
    public Command parse(String input) throws HermesException {
        Keyword keyword = Keyword.of(parseCommandWord(input));
        String arguments = parseArguments(input);

        return switch (keyword) {
            case BYE -> new ByeCommand();
            case LIST -> new ListCommand();
            case MARK -> new MarkCommand(parseTaskNumber(arguments, keyword));
            case UNMARK -> new UnmarkCommand(parseTaskNumber(arguments, keyword));
            case DELETE -> new DeleteCommand(parseTaskNumber(arguments, keyword));
            case TODO -> new AddCommand(parseToDo(arguments));
            case DEADLINE -> new AddCommand(parseDeadline(arguments));
            case EVENT -> new AddCommand(parseEvent(arguments));
            case DUE -> new DueCommand(parseDueCutoff(arguments));
            case SORT -> new SortCommand();
            case FIND -> new FindCommand(parseFind(arguments));
            case UNKNOWN -> new UnknownCommand(parseCommandWord(input));
        };
    }

    /**
     * Returns the first word of the input, so an unrecognised command can be
     * quoted back to the user.
     *
     * @param input one full line of input, already trimmed
     * @return the word the user typed as a command
     */
    private String parseCommandWord(String input) {
        return input.split("\\s+", 2)[0];
    }

    /**
     * Returns everything the user typed after the command word.
     *
     * @param input one full line of input, already trimmed
     * @return the arguments, or an empty string if the command stood alone
     */
    private String parseArguments(String input) {
        String[] parts = input.split("\\s+", 2);
        return parts.length < 2 ? "" : parts[1];
    }

    /**
     * Checks to ensure that only one keyword is given for the find operation
     *
     * @param input what the user typed after the command keyword
     * @return the keyword
     * @throws HermesException too many keywords were provided by the user
     */
    private String parseFind(String input) throws HermesException {
        String[] parts = input.split("\\s+");

        if (parts.length > 1 ) {
            throw new HermesException("Apologies, please enter only one keyword, for example: "
                    + Keyword.FIND.getExample());
        }

        if (parts[0].isEmpty()) {
            throw new HermesException("Apologies, please enter at lease one keyword, for example: "
                    + Keyword.FIND.getExample());
        }

        return input.toLowerCase();
    }

    /**
     * Reads the task number given to a command such as {@code mark 1}.
     *
     * <p>The user counts from one and the list is indexed from zero, so the
     * number is converted here rather than by every caller.
     *
     * @param arguments what the user typed after the command word
     * @param instruction the command being run, used to quote a correct example
     * @return the task's index in the list, counting from zero
     * @throws HermesException if the number is missing or is not a number
     */
    private int parseTaskNumber(String arguments, Keyword instruction) throws HermesException {
        if (arguments.isBlank()) {
            throw new HermesException(missingTaskNumberMessage(instruction));
        }

        try {
            return Integer.parseInt(arguments.trim()) - 1;
        } catch (NumberFormatException e) {
            throw new HermesException(String.format("'%s' is not a task number.", arguments));
        }
    }

    /**
     * Builds the complaint shown when a command that needs a task number was
     * given none. Deleting is worded differently from marking, so the wording
     * follows the command.
     */
    private String missingTaskNumberMessage(Keyword instruction) {
        String request = instruction == Keyword.DELETE
                ? "Please tell me which task you will like to delete, "
                : "Please tell me which task, ";
        return request + "for example: " + instruction.getExample();
    }

    /**
     * Builds a {@link ToDo} from a command such as {@code todo borrow book}.
     *
     * @param arguments what the user typed after the command word
     * @return the task the user described
     * @throws HermesException if the description is missing or cannot be stored
     */
    private Task parseToDo(String arguments) throws HermesException {
        if (arguments.isBlank()) {
            throw new HermesException("A todo needs a description, for example: "
                    + Keyword.TODO.getExample());
        }

        String description = arguments.trim();
        Storage.rejectSeparator(description);

        return new ToDo(description);
    }

    /**
     * Builds a {@link Deadline} from a command such as
     * {@code deadline return book /by 27 Aug 2026 1500}.
     *
     * @param arguments what the user typed after the command word
     * @return the task the user described
     * @throws HermesException if the description or date is missing, cannot be
     *     stored, or is not a date Hermes recognises
     */
    private Task parseDeadline(String arguments) throws HermesException {
        String example = "for example: " + Keyword.DEADLINE.getExample();

        if (arguments.isBlank()) {
            throw new HermesException("A deadline needs a description, " + example);
        }

        String[] fields = arguments.split("\\s*/by\\s*", 2);

        if (fields.length < 2) {
            throw new HermesException("A deadline needs a /by date, " + example);
        }

        String description = fields[0].trim();
        String by = fields[1].trim();

        if (description.isEmpty() || by.isEmpty()) {
            throw new HermesException("A deadline needs both a description and a /by date, " + example);
        }

        Storage.rejectSeparator(description, by);

        return new Deadline(description, DateTimeFormat.parseTime(by));
    }

    /**
     * Builds an {@link Event} from a command such as
     * {@code event project meeting /from 27 Aug 2026 1500 /to 27 Aug 2026 1630}.
     *
     * @param arguments what the user typed after the command word
     * @return the task the user described
     * @throws HermesException if any part is missing, cannot be stored, or is
     *     not a date Hermes recognises
     */
    private Task parseEvent(String arguments) throws HermesException {
        String example = "for example: " + Keyword.EVENT.getExample();

        if (arguments.isBlank()) {
            throw new HermesException("An event needs a description, " + example);
        }

        String[] afterFrom = arguments.split("\\s*/from\\s*", 2);

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
            throw new HermesException("An event needs a description, a /from time and a /to time, "
                    + example);
        }

        LocalDateTime startDateTime = DateTimeFormat.parseTime(start);
        LocalDateTime endDateTime = DateTimeFormat.parseTime(end);

        Storage.rejectSeparator(description, start, end);

        return new Event(description, startDateTime, endDateTime);
    }

    /**
     * Reads the cutoff given to a command such as
     * {@code due /by 28 Aug 2026 1600}.
     *
     * @param arguments what the user typed after the command word
     * @return the moment tasks are being measured against
     * @throws HermesException if the date is missing or is not one Hermes
     *     recognises
     */
    private LocalDateTime parseDueCutoff(String arguments) throws HermesException {
        String[] fields = arguments.split("\\s*/by\\s*", 2);

        if (fields.length < 2 || fields[1].isBlank()) {
            throw new HermesException("A due needs a /by date, for example: "
                    + Keyword.DUE.getExample());
        }

        return DateTimeFormat.parseTime(fields[1].trim());
    }
}

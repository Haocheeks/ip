package hermes.command;

import hermes.task.LogBook;

/**
 * Reports that the user typed something Hermes does not recognise.
 *
 * <p>Not recognising a command is an ordinary outcome rather than an error, so
 * it is a Command like any other and the main loop needs no special case.
 */
public class UnknownCommand extends Command {

    private final String word;

    /**
     * @param word what the user typed in place of a command
     */
    public UnknownCommand(String word) {
        this.word = word;
    }

    @Override
    public String execute(LogBook logBook) {
        return String.format("Sorry, I am not familiar with the '%s' command.", this.word);
    }
}

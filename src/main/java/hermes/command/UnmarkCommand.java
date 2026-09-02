package hermes.command;

import hermes.HermesException;
import hermes.task.LogBook;

/** Marks one task as no longer completed. */
public class UnmarkCommand extends Command {

    private final int index;

    /**
     * @param index the task's position in the list, counting from zero
     */
    public UnmarkCommand(int index) {
        this.index = index;
    }

    @Override
    public String execute(LogBook logBook) throws HermesException {
        return logBook.unmark(this.index);
    }
}

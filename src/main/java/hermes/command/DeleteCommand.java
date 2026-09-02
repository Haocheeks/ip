package hermes.command;

import hermes.HermesException;
import hermes.task.LogBook;
import hermes.ui.Ui;

/** Removes one task from the list. */
public class DeleteCommand extends Command {

    private final int index;

    /**
     * @param index the task's position in the list, counting from zero
     */
    public DeleteCommand(int index) {
        this.index = index;
    }

    @Override
    public String execute(LogBook logBook) throws HermesException {
        return logBook.delete(this.index);
    }
}

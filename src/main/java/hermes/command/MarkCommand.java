package hermes.command;

import hermes.HermesException;
import hermes.task.LogBook;
import hermes.ui.Ui;

/** Marks one task as completed. */
public class MarkCommand extends Command {

    private final int index;

    /**
     * @param index the task's position in the list, counting from zero
     */
    public MarkCommand(int index) {
        this.index = index;
    }

    @Override
    public void execute(LogBook logBook, Ui ui) throws HermesException {
        ui.show(logBook.mark(this.index));
    }
}

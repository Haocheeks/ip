package hermes.command;

import hermes.HermesException;
import hermes.task.LogBook;
import hermes.ui.Ui;

/** Reorders the tasks by deadline and keeps that order. */
public class SortCommand extends Command {

    @Override
    public void execute(LogBook logBook, Ui ui) throws HermesException {
        ui.show(logBook.sort());
    }
}

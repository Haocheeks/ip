package hermes.command;

import hermes.HermesException;
import hermes.task.LogBook;

/** Reorders the tasks by deadline and keeps that order. */
public class SortCommand extends Command {

    @Override
    public String execute(LogBook logBook) throws HermesException {
        return logBook.sort();
    }
}

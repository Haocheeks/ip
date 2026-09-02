package hermes.command;

import hermes.task.LogBook;

/** Shows every task, numbered as the user refers to them. */
public class ListCommand extends Command {

    @Override
    public String execute(LogBook logBook) {
        return logBook.toString();
    }
}

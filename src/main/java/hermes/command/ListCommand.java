package hermes.command;

import hermes.task.LogBook;
import hermes.ui.Ui;

/** Shows every task, numbered as the user refers to them. */
public class ListCommand extends Command {

    @Override
    public String execute(LogBook logBook) {
        return logBook.toString();
    }
}

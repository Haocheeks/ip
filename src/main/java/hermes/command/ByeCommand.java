package hermes.command;

import hermes.task.LogBook;
import hermes.ui.Ui;

/** Says goodbye and ends the conversation. */
public class ByeCommand extends Command {

    @Override
    public void execute(LogBook logBook, Ui ui) {
        ui.showGoodbye();
    }

    @Override
    public boolean isExit() {
        return true;
    }
}

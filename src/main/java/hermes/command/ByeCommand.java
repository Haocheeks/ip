package hermes.command;

import hermes.task.LogBook;
import hermes.ui.Ui;

/** Says goodbye and ends the conversation. */
public class ByeCommand extends Command {

    @Override
    public String execute(LogBook logBook) {
        return "Goodbye, thank you for contacting me!";
    }

    @Override
    public boolean isExit() {
        return true;
    }
}

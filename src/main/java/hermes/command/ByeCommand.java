package hermes.command;

import hermes.task.LogBook;

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

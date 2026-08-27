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

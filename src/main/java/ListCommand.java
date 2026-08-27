/** Shows every task, numbered as the user refers to them. */
public class ListCommand extends Command {

    @Override
    public void execute(LogBook logBook, Ui ui) {
        ui.show(logBook.toString());
    }
}

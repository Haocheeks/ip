/**
 * Adds a task to the list.
 *
 * <p>One class serves todo, deadline and event: by the time a Command is built
 * the {@link Parser} has already produced the right kind of {@link Task}, so
 * adding it is the same work in all three cases.
 */
public class AddCommand extends Command {

    private final Task task;

    /**
     * @param task the task the user described
     */
    public AddCommand(Task task) {
        this.task = task;
    }

    @Override
    public void execute(LogBook logBook, Ui ui) throws HermesException {
        ui.show(logBook.log(this.task));
    }
}

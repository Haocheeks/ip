/** Removes one task from the list. */
public class DeleteCommand extends Command {

    private final int index;

    /**
     * @param index the task's position in the list, counting from zero
     */
    public DeleteCommand(int index) {
        this.index = index;
    }

    @Override
    public void execute(LogBook logBook, Ui ui) throws HermesException {
        ui.show(logBook.delete(this.index));
    }
}

/** Marks one task as no longer completed. */
public class UnmarkCommand extends Command {

    private final int index;

    /**
     * @param index the task's position in the list, counting from zero
     */
    public UnmarkCommand(int index) {
        this.index = index;
    }

    @Override
    public void execute(LogBook logBook, Ui ui) throws HermesException {
        ui.show(logBook.unmark(this.index));
    }
}

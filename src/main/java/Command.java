/**
 * One instruction from the user, parsed and ready to carry out.
 *
 * <p>A Command holds whatever the user supplied — a task, an index, a date —
 * already checked and converted, so carrying it out cannot fail for want of
 * valid input. {@link Parser} decides which Command to build; {@link Hermes}
 * runs it without knowing which one it has.
 */
public abstract class Command {

    /**
     * Carries out this command and shows the result.
     *
     * @param logBook the tasks to act on
     * @param ui where to show the outcome
     * @throws HermesException if the command could not be completed
     */
    public abstract void execute(LogBook logBook, Ui ui) throws HermesException;

    /**
     * Reports whether Hermes should stop after this command.
     *
     * @return false for every command but {@link ByeCommand}
     */
    public boolean isExit() {
        return false;
    }
}

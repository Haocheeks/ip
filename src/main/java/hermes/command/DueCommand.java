package hermes.command;

import java.time.LocalDateTime;

import hermes.task.LogBook;
import hermes.ui.Ui;

/** Shows the outstanding tasks falling due on or before a given moment. */
public class DueCommand extends Command {

    private final LocalDateTime cutoff;

    /**
     * @param cutoff the moment tasks are measured against
     */
    public DueCommand(LocalDateTime cutoff) {
        this.cutoff = cutoff;
    }

    @Override
    public void execute(LogBook logBook, Ui ui) {
        ui.show(logBook.listTaskDueBy(this.cutoff));
    }
}

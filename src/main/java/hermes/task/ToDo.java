package hermes.task;

import java.time.LocalDateTime;

/** A task with no date, so it is never due by any particular moment. */
public class ToDo extends Task {

    /** Creates a todo that is not yet completed. */
    public ToDo(String description) {
        super(description);
    }

    /** Creates a todo in a known state, used when loading from storage. */
    public ToDo(boolean isCompleted, String description) {
        super(isCompleted, description);
    }

    @Override
    public LocalDateTime dueDateTime() {
        return null;
    }

    @Override
    public String fileContent() {
        return String.format("T | %d | %s", this.isCompleted ? 1 : 0, this.description);
    }

    @Override
    public String toString() {
        return String.format("[T]%s", super.toString());
    }
}

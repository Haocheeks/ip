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
    public LocalDateTime getDueDateTime() {
        return null;
    }

    @Override
    public String getFileContent() {
        return String.format("T | %d | %s", this.isCompleted ? 1 : 0, this.taskDescription);
    }

    @Override
    public String toString() {
        return String.format("[T]%s", super.toString());
    }
}

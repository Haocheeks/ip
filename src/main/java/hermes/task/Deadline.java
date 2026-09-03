package hermes.task;

import java.time.LocalDateTime;

/** A task that must be finished by a given moment. */
public class Deadline extends Task {

    protected LocalDateTime dueDateTime;

    /** Creates a deadline that is not yet completed. */
    public Deadline(String description, LocalDateTime dueDateTime) {
        super(description);
        this.dueDateTime = dueDateTime;
    }

    /** Creates a deadline in a known state from its stored ISO-8601 date. */
    public Deadline(Boolean isCompleted, String description, String deadline) {
        super(isCompleted, description);
        this.dueDateTime = LocalDateTime.parse(deadline);
    }

    @Override
    public LocalDateTime dueDateTime() {
        return this.dueDateTime;
    }

    @Override
    public String fileContent() {
        return String.format("D | %d | %s | %s",
                this.isCompleted ? 1 : 0,
                this.description,
                this.dueDateTime);
    }

    @Override
    public String toString() {
        return String.format("[D]%s (by: %s)",
                super.toString(),
                this.dueDateTime.format(DISPLAY_FORMATTER));
    }
}

package hermes.task;

import java.time.LocalDateTime;

/** A task that must be finished by a given moment. */
public class Deadline extends Task {

    protected LocalDateTime by;

    /** Creates a deadline that is not yet completed. */
    public Deadline(String message, LocalDateTime by) {
        super(message);
        this.by = by;
    }

    /** Creates a deadline in a known state from its stored ISO-8601 date. */
    public Deadline(Boolean isCompleted, String message, String deadline) {
        super(isCompleted, message);
        this.by = LocalDateTime.parse(deadline);
    }

    @Override
    public LocalDateTime dueDateTime() {
        return this.by;
    }

    @Override
    public String fileContent() {
        return String.format("D | %d | %s | %s",
                this.isCompleted ? 1 : 0,
                this.message,
                this.by);
    }

    @Override
    public String toString() {
        return String.format("[D]%s (by: %s)",
                super.toString(),
                this.by.format(FORMATTER));
    }
}

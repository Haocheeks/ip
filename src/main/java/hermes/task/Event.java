package hermes.task;

import java.time.LocalDateTime;

/** A task that runs between two moments, measured by its start time. */
public class Event extends Task {

    protected LocalDateTime start;
    protected LocalDateTime end;

    /** Creates an event that is not yet completed. */
    public Event(String description, LocalDateTime start, LocalDateTime end) {
        super(description);
        this.start = start;
        this.end = end;
    }

    /** Creates an event in a known state from its stored ISO-8601 dates. */
    public Event(Boolean isCompleted, String description, String start, String end) {
        super(isCompleted, description);
        this.start = LocalDateTime.parse(start);
        this.end = LocalDateTime.parse(end);
    }

    @Override
    public LocalDateTime dueDateTime() {
        return this.start;
    }

    @Override
    public String fileContent() {
        return String.format("E | %d | %s | %s | %s",
                this.isCompleted ? 1 : 0,
                this.description,
                this.start,
                this.end);
    }

    @Override
    public String toString() {
        return String.format("[E]%s (from: %s to: %s)",
                super.toString(), start.format(DISPLAY_FORMATTER), end.format(DISPLAY_FORMATTER));
    }
}

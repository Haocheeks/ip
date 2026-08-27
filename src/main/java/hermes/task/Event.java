package hermes.task;

import java.time.LocalDateTime;

public class Event extends Task {

    protected LocalDateTime start;
    protected LocalDateTime end;

    public Event(String message, LocalDateTime start, LocalDateTime end) {
        super(message);
        this.start = start;
        this.end = end;
    }

    public Event(Boolean isCompleted, String message, String start, String end) {
        super(isCompleted, message);
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
                this.message,
                this.start,
                this.end);
    }

    @Override
    public String toString() {
        return String.format("[E]%s (from: %s to: %s)",
                super.toString(), start.format(formatter), end.format(formatter));
    }
}

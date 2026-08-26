import java.time.LocalDateTime;

public class Event extends Task {

    protected LocalDateTime start;
    protected LocalDateTime end;

    public Event(String message, LocalDateTime start, LocalDateTime end) {
        super(message);
        this.start = start;
        this.end = end;
    }

    public Event(Boolean isCompleted, String message, LocalDateTime start, LocalDateTime end) {
        super(isCompleted, message);
        this.start = start;
        this.end = end;
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
                super.toString(), start, end);
    }
}

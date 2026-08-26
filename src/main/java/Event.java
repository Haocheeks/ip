public class Event extends Task {

    protected String start;
    protected String end;

    public Event(String message, String start, String end) {
        super(message);
        this.start = start;
        this.end = end;
    }

    public Event(Boolean isCompleted, String message, String start, String end) {
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

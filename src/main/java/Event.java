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

    public String fileContent() {
        String output = String.format("D | %d | %s | %s | %s",
                this.isCompleted ? 1 : 0,
                this.message,
                this.start,
                this.end);
        return output;
    }

    @Override
    public String toString() {
        String output = String.format("[E]%s (from: %s to: %s)",
                super.toString(), start, end);
        return output;
    }
}

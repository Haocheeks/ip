public class Event extends Task {

    protected String start;
    protected String end;

    public Event(String message, String start, String end) {
        super(message);
        this.start = start;
        this.end = end;
    }

    @Override
    public String toString() {
        String output = String.format("[E]%s (from: %s to: %s)",
                super.toString(), start, end);
        return output;
    }
}

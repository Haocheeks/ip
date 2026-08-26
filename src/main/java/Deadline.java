public class Deadline extends Task {

    protected String by;

    public Deadline(String message,  String by) {
        super(message);
        this.by = by;
    }

    public Deadline(Boolean isCompleted, String message, String by) {
        super(isCompleted, message);
        this.by = by;
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
                super.toString(), by);
    }
}

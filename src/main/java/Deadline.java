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

    public String fileContent() {
        String output = String.format("D | %d | %s | %s",
                this.isCompleted ? 1 : 0,
                this.message,
                this.by);
        return output;
    }

    @Override
    public String toString() {
        String output = String.format("[D]%s (by: %s)",
                super.toString(), by);
        return output;
    }
}

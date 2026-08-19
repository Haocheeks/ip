public class Deadline extends Task {

    protected String by;

    public Deadline(String message,  String by) {
        super(message);
        this.by = by;
    }

    @Override
    public String toString() {
        String output = String.format("[D] %s (by: %s)",
                super.toString(), by);
        return output;
    }
}

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Deadline extends Task {

    protected LocalDateTime by;

    public Deadline(String message, LocalDateTime by) {
        super(message);
        this.by = by;
    }

    public Deadline(Boolean isCompleted, String message, String deadline) {
        super(isCompleted, message);
        this.by = LocalDateTime.parse(deadline);
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
                this.by.format(formatter));
    }
}

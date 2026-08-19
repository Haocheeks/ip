import java.util.ArrayList;
import java.util.Arrays;

public class LogBook {
    private ArrayList<Task> logBook;
    private int curr = 1;

    public LogBook(int size) {
        this.logBook = new ArrayList<>();
    }

    /**
     * Adds message to the logBook for storage and replies to provide an update
     * with a message wrapped in divider line.
     *
     * @param message the text that is logged
     */
    public void log(String message) {
        Task task = new Task(message, this.curr++);

        this.logBook.add(task);
        System.out.printf("""
                ____________________________________________________________
                Added: %s
                ____________________________________________________________ 
                
                """, message);
    }

    @Override
    public String toString() {
        String output = """
                ____________________________________________________________
                """;

        for (int i = 0; i < this.logBook.size(); i++) {
            output += logBook.get(i) + "\n";
        }

        output += """
                ____________________________________________________________
                """;

        return output;
    }
}

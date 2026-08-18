import java.util.Arrays;

public class LogBook {
    private Task[] logBook;
    private int curr = 0;

    public LogBook(int size) {
        this.logBook = new Task[size];
    }

    /**
     * Adds message to the logBook for storage and replies to provide an update
     * with a message wrapped in divider line.
     *
     * @param message the text that is logged
     */
    public void log(String message) {
        Task task = new Task(message, this.curr + 1);

        this.logBook[curr++] = task;
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

        for (int i = 0; i < curr; i++) {
            output += logBook[i].toString() + "\n";
        }

        output += """
                ____________________________________________________________
                """;

        return output;
    }
}

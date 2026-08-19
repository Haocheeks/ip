import java.util.ArrayList;

public class LogBook {
    private ArrayList<Task> logBook;
    private int curr = 1;

    public LogBook() {
        this.logBook = new ArrayList<>();
    }

    /**
     * Adds message to the logBook for storage and replies to provide an update
     * with a message wrapped in divider line.
     *
     * @param message the text that is logged
     */
    public String log(String message) {
        Task task = new Task(message, this.curr++);

        this.logBook.add(task);
        return String.format("Added: %s", message);
    }

    /**
     * Marks a task as completed
     *
     * @param id the task index in the array
     * @return a response
     */
    public String mark(int id) {
        if (id < 0 || id >= this.logBook.size()) {
            return "Sorry, I have no task numbered " + (id + 1) + ".";
        }
        return this.logBook.get(id).mark();
    }

    /**
     * Marks a task as incomplete
     *
     * @param id the task index in the array
     * @return a response
     */
    public String unmark(int id) {
        if (id < 0 || id >= this.logBook.size()) {
            return "Sorry, I have no task numbered " + (id + 1) + ".";
        }
        return this.logBook.get(id).unmark();
    }

    @Override
    public String toString() {
        StringBuilder output = new StringBuilder();

        for (int i = 0; i < this.logBook.size(); i++) {
            String temp = String.format("%d. %s\n", i + 1, this.logBook.get(i));
            output.append(temp);
        }

        return output.toString();
    }
}

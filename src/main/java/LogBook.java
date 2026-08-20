import java.util.ArrayList;

public class LogBook {
    private ArrayList<Task> logBook;

    public LogBook() {
        this.logBook = new ArrayList<>();
    }

    /**
     * Stores an already-built task and reports how many tasks are now held.
     *
     * <p>The caller decides which kind of {@link Task} to create, so this
     * method works unchanged for todos, deadlines and events.
     *
     * @param task the task to store
     * @return the message confirming the task was added
     */
    public String log(Task task) {
        this.logBook.add(task);
        return String.format("""
                Got it. I've added this task:
                  %s
                Now you have %d task%s in the list.
                """, task, this.logBook.size(), (this.logBook.size() == 1 ? "" : "s"));
    }

    /**
     * Marks a task as completed
     *
     * @param id the task index in the array
     * @return a response
     */
    public String mark(int id) throws HermesException {
        checkIndex(id);
        return this.logBook.get(id).mark();
    }

    /**
     * Marks a task as incomplete
     *
     * @param id the task index in the array
     * @return a response
     */
    public String unmark(int id) throws HermesException {
        checkIndex(id);
        return this.logBook.get(id).unmark();
    }

    /**
     * Deletes a task from storage
     *
     * @param id the task index in the array
     * @return a response
     */
    public String delete(int id) throws HermesException {
        checkIndex(id);

        Task removed = this.logBook.remove(id);
        int remaining = this.logBook.size();

        return String.format("""
                Roger, I've removed this task:
                  %s
                Now you have %d task%s in the list. 
                """, removed, remaining, (remaining == 1 ? "" : "s"));
    }

    /**
     * Checks to ensure that the index inputted into the method is a valid one
     *
     * @param id index of task we are manipulating
     * @throws HermesException error indicating index out of bounds exception
     */
    private void checkIndex(int id) throws HermesException {
        if (id < 0 || id >= this.logBook.size()) {
            throw new HermesException("Sorry, I have no task numbered " + (id + 1) + ".");
        }
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

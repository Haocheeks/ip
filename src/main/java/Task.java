public class Task {

    private String message;
    private boolean completed = false;

    public Task(String message, int id) {
        this.message = message;
    }

    /**
     * Marks the task as complete
     *
     * @return a response
     */
    public String mark() {
        if (!this.completed) {
            this.completed = true;
            String response = String.format("""
                    Roger, I will mark this task as completed:
                      %s
                    """, this);
            return response;
        } else {
            return "The task is already marked as completed.";
        }
    }

    /**
     * Marks a task as incomplete
     *
     * @return
     */
    public String unmark() {
        if (this.completed) {
            this.completed = false;

            String response = String.format("""
                    Alright, I will mark this task as incomplete:
                      %s
                    """, this);

            return response;
        } else {
            return "The task is already marked as incomplete.";
        }
    }

    @Override
    public String toString() {
        String output = String.format(
                "[%c] %s",
                completed ? 'X' : ' ',
                this.message);
        return output;
    }
}

public class Task {

    protected String message;
    protected boolean isCompleted = false;

    public Task(String message) {
        this.message = message;
    }

    public Task(boolean isCompleted, String message) {
        this.isCompleted = isCompleted;
        this.message = message;
    }

    /**
     * Marks the task as complete
     *
     * @return a response
     */
    public String mark() {
        if (!this.isCompleted) {
            this.isCompleted = true;
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
        if (this.isCompleted) {
            this.isCompleted = false;

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
                isCompleted ? 'X' : ' ',
                this.message);
        return output;
    }
}

public abstract class Task {

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
            return String.format("""
                    Roger, I will mark this task as completed:
                      %s
                    """, this);
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

    public abstract String fileContent();

    @Override
    public String toString() {
        return String.format(
                "[%c] %s",
                isCompleted ? 'X' : ' ',
                this.message);
    }
}

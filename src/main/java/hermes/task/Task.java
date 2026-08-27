package hermes.task;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public abstract class Task implements Comparable<Task> {

    protected String message;
    protected boolean isCompleted = false;
    protected static final DateTimeFormatter formatter =
            DateTimeFormatter.ofPattern("dd MMM yyyy HHmm");

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

    public abstract LocalDateTime dueDateTime();

    public boolean isDueBy(LocalDateTime deadline) {
        LocalDateTime due = dueDateTime();
        return due != null && !due.isAfter(deadline);
    }

    /**
     * Groups tasks for sorting: 0 = active and dated, 1 = active but undated,
     * 2 = completed. Tasks with something to do soonest come first.
     */
    private int sortRank() {
        if (this.isCompleted) {
            return 2;
        }
        return this.dueDateTime() == null ? 1 : 0;
    }

    @Override
    public int compareTo(Task otherTask) {
        int rankDifference = Integer.compare(this.sortRank(), otherTask.sortRank());
        if (rankDifference != 0) {
            return rankDifference;
        }
        // Same rank, so only dated active tasks have anything left to separate them.
        if (this.sortRank() == 0) {
            return this.dueDateTime().compareTo(otherTask.dueDateTime());
        }
        return 0;
    }

    @Override
    public String toString() {
        return String.format(
                "[%c] %s",
                isCompleted ? 'X' : ' ',
                this.message);
    }
}

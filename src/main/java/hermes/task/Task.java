package hermes.task;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/** One thing the user wants to keep track of. */
public abstract class Task implements Comparable<Task> {

    protected static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("dd MMM yyyy HHmm");

    protected String message;
    protected boolean isCompleted = false;

    /** Creates a task that is not yet completed. */
    public Task(String message) {
        this.message = message;
    }

    /** Creates a task in a known state, used when loading from storage. */
    public Task(boolean isCompleted, String message) {
        this.isCompleted = isCompleted;
        this.message = message;
    }

    /** Marks the task as completed and returns the reply to show the user. */
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

    /** Marks the task as not completed and returns the reply to show the user. */
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

    /** Returns this task as one line of the data file. */
    public abstract String fileContent();

    /** Returns the moment this task is measured against, or null if it has none. */
    public abstract LocalDateTime dueDateTime();

    /** Returns true if this task has a date falling no later than the given moment. */
    public boolean isDueBy(LocalDateTime deadline) {
        LocalDateTime due = dueDateTime();
        return due != null && !due.isAfter(deadline);
    }

    /** Groups tasks for sorting: 0 = active and dated, 1 = active undated, 2 = completed. */
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

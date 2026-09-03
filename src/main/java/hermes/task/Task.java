package hermes.task;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/** One thing the user wants to keep track of. */
public abstract class Task implements Comparable<Task> {

    protected static final DateTimeFormatter DISPLAY_FORMATTER =
            DateTimeFormatter.ofPattern("dd MMM yyyy HHmm");

    protected String description;
    protected boolean isCompleted = false;

    /** Creates a task that is not yet completed. */
    public Task(String description) {
        this.description = description;
    }

    /** Creates a task in a known state, used when loading from storage. */
    public Task(boolean isCompleted, String description) {
        this.isCompleted = isCompleted;
        this.description = description;
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
            return String.format("""
                    Alright, I will mark this task as incomplete:
                      %s
                    """, this);
        } else {
            return "The task is already marked as incomplete.";
        }
    }

    /** Returns true if this task has been completed. */
    public boolean isCompleted() {
        return this.isCompleted;
    }

    /** Returns the text the user gave to describe this task. */
    public String getDescription() {
        return this.description;
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
                this.description);
    }
}

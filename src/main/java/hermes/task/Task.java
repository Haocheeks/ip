package hermes.task;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/** One thing the user wants to keep track of. */
public abstract class Task implements Comparable<Task> {

    protected static final DateTimeFormatter DISPLAY_FORMATTER =
            DateTimeFormatter.ofPattern("dd MMM yyyy HHmm");

    protected String taskDescription;
    protected boolean isCompleted = false;

    /** Creates a task that is not yet completed. */
    public Task(String taskDescription) {
        this.taskDescription = taskDescription;
    }

    /** Creates a task in a known state, used when loading from storage. */
    public Task(boolean isCompleted, String taskDescription) {
        this.isCompleted = isCompleted;
        this.taskDescription = taskDescription;
    }

    /** Marks the task as completed and returns the reply to show the user. */
    public boolean mark() {
        if (isCompleted) {
            return false;
        }

        this.isCompleted = true;
        return true;
    }

    /** Marks the task as not completed and returns the reply to show the user. */
    public boolean unmark() {
        if (!this.isCompleted) {
            return false;
        }

        this.isCompleted = false;
        return true;
    }

    /** Returns true if this task has been completed. */
    public boolean isCompleted() {
        return this.isCompleted;
    }

    /** Returns the text the user gave to describe this task. */
    public String getTaskDescription() {
        return this.taskDescription;
    }

    /** Returns this task as one line of the data file. */
    public abstract String getFileContent();

    /** Returns the moment this task is measured against, or null if it has none. */
    public abstract LocalDateTime getDueDateTime();

    /** Returns an independent copy of this task. */
    public abstract Task copy();

    /** Returns true if this task has a date falling no later than the given moment. */
    public boolean isDueBy(LocalDateTime deadline) {
        LocalDateTime dueDateTime = getDueDateTime();
        return dueDateTime != null && !dueDateTime.isAfter(deadline);
    }

    enum TaskOrder {
        ACTIVE_DATED,
        ACTIVE_UNDATED,
        COMPLETED
    }

    /** Groups tasks for sorting; the constants are declared in the order they sort. */
    private TaskOrder getSortRank() {
        if (this.isCompleted) {
            return TaskOrder.COMPLETED;
        }
        return this.getDueDateTime() == null ? TaskOrder.ACTIVE_UNDATED : TaskOrder.ACTIVE_DATED;
    }

    @Override
    public int compareTo(Task otherTask) {
        TaskOrder thisSortRank = getSortRank();
        TaskOrder otherSortRank = otherTask.getSortRank();
        int rankDifference = thisSortRank.compareTo(otherSortRank);

        if (rankDifference != 0) {
            return rankDifference;
        }

        // Same rank, so only dated active tasks have anything left to separate them.
        if (this.getSortRank() == TaskOrder.ACTIVE_DATED) {
            assert this.getDueDateTime() != null && otherTask.getDueDateTime() != null
                    : "Rank 0 means dated, so neither due date can be null";
            return this.getDueDateTime().compareTo(otherTask.getDueDateTime());
        }

        return 0;
    }

    @Override
    public String toString() {
        return String.format(
                "[%c] %s",
                isCompleted ? 'X' : ' ',
                this.taskDescription);
    }
}

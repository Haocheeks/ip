package hermes.task;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.stream.Collectors;

import hermes.HermesException;

/**
 * Holds the tasks Hermes is keeping track of and the operations that change
 * them.
 *
 * <p>The list in memory is the single source of truth. Every change is handed
 * to {@link Storage} straight away, so the file always matches the list.
 */
public class LogBook {
    private final ArrayList<Task> tasks;
    private final Storage storage;

    /**
     * Starts from whatever the given storage already holds.
     *
     * @param storage where tasks are read from and written back to
     */
    public LogBook(Storage storage) {
        this.storage = storage;
        this.tasks = storage.load();
    }

    /**
     * Returns how many stored records could not be read when these tasks were
     * loaded.
     *
     * <p>Storage counts them, since it is the class that reads the file. This
     * exists so a caller can ask the task list whether it holds everything that
     * was saved, without needing a reference to storage of its own.
     *
     * @return the number of skipped records, zero if everything loaded
     */
    public int getSkippedLines() {
        return this.storage.getSkippedLines();
    }

    /**
     * Stores an already-built task and reports how many tasks are now held.
     *
     * <p>The caller decides which kind of {@link Task} to create, so this
     * method works unchanged for todos, deadlines and events.
     *
     * @param task the task to store
     * @return the message confirming the task was added
     * @throws HermesException if the task could not be written to storage
     */
    public String log(Task task) throws HermesException {
        this.tasks.add(task);
        this.storage.save(this.tasks);
        return String.format("""
                Got it. I've added this task:
                  %s
                Now you have %d task%s in the list.
                """, task, this.tasks.size(), (this.tasks.size() == 1 ? "" : "s"));
    }

    /**
     * Marks a task as completed
     *
     * @param id the task index in the array
     * @return a response
     */
    public String mark(int id) throws HermesException {
        checkIndex(id);
        String output = this.tasks.get(id).mark();
        this.storage.save(this.tasks);
        return output;
    }

    /**
     * Marks a task as incomplete
     *
     * @param id the task index in the array
     * @return a response
     */
    public String unmark(int id) throws HermesException {
        checkIndex(id);
        String output = this.tasks.get(id).unmark();
        this.storage.save(this.tasks);
        return output;
    }

    /**
     * Deletes a task from storage
     *
     * @param id the task index in the array
     * @return a response
     */
    public String delete(int id) throws HermesException {
        checkIndex(id);

        Task removed = this.tasks.remove(id);
        int remaining = this.tasks.size();
        this.storage.save(this.tasks);

        return String.format("""
                Roger, I've removed this task:
                  %s
                Now you have %d task%s in the list.
                """, removed, remaining, (remaining == 1 ? "" : "s"));
    }

    /**
     * List all tasks in the list due no later than a specific date and time
     *
     * @param deadline filter condition
     * @return all tasks due not later than the deadline
     */
    public String listTaskDueBy(LocalDateTime deadline) {
        String output = this.tasks.stream()
                .filter(task -> task.isDueBy(deadline) && !task.isCompleted)
                .sorted(Comparator.comparing(Task::dueDateTime))
                .map(Task::toString)
                .collect(Collectors.joining("\n"));
        String outIfEmpty = "Nothing is due by " + deadline.format(Task.FORMATTER);
        return output.isEmpty() ? outIfEmpty : output;
    }

    /**
     * Reorders the tasks by deadline, soonest first. Tasks without a date follow
     * the dated ones, and completed tasks come last. The new order is written to
     * storage, so it is still in place the next time Hermes starts.
     *
     * @return the reordered tasks, numbered exactly as the list command shows them
     * @throws HermesException if the reordered tasks could not be written to storage
     */
    public String sort() throws HermesException {
        if (this.tasks.isEmpty()) {
            return "There is nothing to sort, your list is empty!";
        }

        // Comparator.naturalOrder() routes through Task.compareTo, which already
        // defines this ordering, rather than restating it here.
        this.tasks.sort(Comparator.naturalOrder());
        this.storage.save(this.tasks);

        return String.format("I have sorted your tasks by deadline:%n%s", this);
    }

    /**
     * List out the tasks that contain the keyword in its description
     */
    public String findTask(String keyword) {
        if (this.tasks.isEmpty()) {
            return "There is nothing to search, your list is empty!";
        }

        String output = this.tasks.stream()
                .filter(task -> task.message.toLowerCase().contains(keyword))
                .map(Task::toString)
                .collect(Collectors.joining("\n"));

        String outputIfEmpty = "Apologies, no task match " + keyword + " :<";

        return output.isEmpty() ? outputIfEmpty : output;
    }

    /**
     * Checks to ensure that the index inputted into the method is a valid one
     *
     * @param id index of task we are manipulating
     * @throws HermesException error indicating index out of bounds exception
     */
    private void checkIndex(int id) throws HermesException {
        if (id < 0 || id >= this.tasks.size()) {
            throw new HermesException("Sorry, I have no task numbered " + (id + 1) + ".");
        }
    }

    @Override
    public String toString() {
        StringBuilder output = new StringBuilder();

        for (int i = 0; i < this.tasks.size(); i++) {
            String temp = String.format("%d. %s\n", i + 1, this.tasks.get(i));
            output.append(temp);
        }

        return output.toString();
    }
}

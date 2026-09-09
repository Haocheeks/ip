package hermes.task;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Stack;
import java.util.stream.Collectors;

import hermes.HermesException;

/**
 * Holds the tasks Hermes is keeping track of and the operations that change them.
 *
 * <p>The list in memory is the single source of truth. Every change is handed
 * to {@link Storage} straight away, so the file always matches the list.
 */
public class LogBook {
    private final Storage storage;
    private ArrayList<Task> tasks;
    private Stack<ArrayList<Task>> cachedTasks;

    /**
     * Starts from whatever the given storage already holds.
     *
     * @param storage where tasks are read from and written back to.
     */
    public LogBook(Storage storage) {
        this.storage = storage;
        this.tasks = storage.load();
        this.cachedTasks = new Stack<>();
    }

    /**
     * Returns how many stored records could not be read when these tasks were
     * loaded.
     *
     * <p>Storage counts them, since it is the class that reads the file. This
     * exists so a caller can ask the task list whether it holds everything that
     * was saved, without needing a reference to storage of its own.
     *
     * @return the number of skipped records, zero if everything loaded.
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
     * @param task the task to store.
     * @return the message confirming the task was added.
     * @throws HermesException if the task could not be written to storage.
     */
    public String log(Task task) throws HermesException {
        cache();
        this.tasks.add(task);
        this.storage.save(this.tasks);
        return String.format("""
                Got it. I've added this task:
                  %s
                Now you have %d task%s in the list.
                """, task, this.tasks.size(), (this.tasks.size() == 1 ? "" : "s"));
    }

    /**
     * Marks every tasks listed as completed.
     *
     * @param indexes the task's position in the list, counting from zero.
     * @return the message confirming the change.
     * @throws HermesException if the number names no task, or the change could
     *                         not be written to storage.
     */
    public String mark(int... indexes) throws HermesException {
        // Every index is checked first before any tasks is marked,
        // checking as each task goes will leave the list half changed
        for (int index : indexes) {
            checkIndex(index);
        }

        cache();

        List<Task> marked = new ArrayList<>();
        List<Task> alreadyMarked = new ArrayList<>();

        for (int index : indexes) {
            Task taskToBeMarked = this.tasks.get(index);
            boolean isMarkedSuccessfully = taskToBeMarked.mark();

            if (isMarkedSuccessfully) {
                marked.add(taskToBeMarked);
            } else {
                alreadyMarked.add(taskToBeMarked);
            }
        }

        int numberMarked = marked.size();
        int numberAlreadyMarked = alreadyMarked.size();

        String markedMessage = numberMarked == 0
                ? ""
                : String.format("""
                Alright, I will mark the following task%s as completed:
                  %s
                """,
                numberMarked == 1 ? "" : "s",
                marked.stream().map(Task::toString).collect(Collectors.joining("\n  ")));

        String alreadyMarkedMessage = numberAlreadyMarked == 0
                ? ""
                : String.format("""
                The following task%s %s already marked as completed:
                  %s
                """,
                numberAlreadyMarked == 1 ? "" : "s",
                numberAlreadyMarked == 1 ? "was" : "were",
                alreadyMarked.stream().map(Task::toString).collect(Collectors.joining("\n  ")));

        this.storage.save(this.tasks);

        if (numberMarked > 0 && numberAlreadyMarked > 0) {
            return markedMessage + "\n" + alreadyMarkedMessage;
        } else if (numberMarked > 0) {
            return markedMessage;
        } else {
            return alreadyMarkedMessage;
        }
    }

    /**
     * Marks one task as no longer completed.
     *
     * @param indexes the task's position in the list, counting from zero.
     * @return the message confirming the change.
     * @throws HermesException if the number names no task, or the change could
     *     not be written to storage.
     */
    public String unmark(int... indexes) throws HermesException {
        // Every index is checked first before any tasks is unmarked,
        // checking as each task goes will leave the list half changed
        for (int index : indexes) {
            checkIndex(index);
        }

        cache();

        List<Task> unmarked = new ArrayList<>();
        List<Task> alreadyUnmarked = new ArrayList<>();

        for (int index : indexes) {
            Task taskToBeUnmarked = this.tasks.get(index);
            boolean isUnmarkedSuccessfully = taskToBeUnmarked.unmark();

            if (isUnmarkedSuccessfully) {
                unmarked.add(taskToBeUnmarked);
            } else {
                alreadyUnmarked.add(taskToBeUnmarked);
            }
        }

        int numberUnmarked = unmarked.size();
        int numberAlreadyUnmarked = alreadyUnmarked.size();

        String unmarkedMessage = numberUnmarked == 0
                ? ""
                : String.format("""
                Alright, I will mark the following task%s as incomplete:
                  %s
                """,
                numberUnmarked == 1 ? "" : "s",
                unmarked.stream().map(Task::toString).collect(Collectors.joining("\n  ")));

        String alreadyUnmarkedMessage = numberAlreadyUnmarked == 0
                ? ""
                : String.format("""
                The following task%s %s already marked as incomplete:
                  %s
                """,
                numberAlreadyUnmarked == 1 ? "" : "s",
                numberAlreadyUnmarked == 1 ? "was" : "were",
                alreadyUnmarked.stream().map(Task::toString).collect(Collectors.joining("\n  ")));

        this.storage.save(this.tasks);

        if (numberUnmarked > 0 && numberAlreadyUnmarked > 0) {
            return unmarkedMessage + "\n" + alreadyUnmarkedMessage;
        } else if (numberUnmarked > 0) {
            return unmarkedMessage;
        } else {
            return alreadyUnmarkedMessage;
        }
    }

    /**
     * Removes every task named and reports how many are left.
     *
     * <p>All the numbers are checked before any task is removed, so one bad
     * number cancels the whole delete. Removing the tasks that could be
     * understood and only then failing would leave the list half changed and,
     * because the failure comes before the save, disagreeing with the file.
     *
     * @param indexes the tasks' positions in the list, counting from zero, in
     *     any order.
     * @return the message naming what was removed.
     * @throws HermesException if any number names no task, or the shortened
     *     list could not be written to storage.
     */
    public String delete(int... indexes) throws HermesException {
        Arrays.sort(indexes);

        // Every index is checked first before any tasks is unmarked,
        // checking as each task goes will leave the list half changed
        for (int index : indexes) {
            checkIndex(index);
        }

        cache();

        StringBuilder output = new StringBuilder();
        int numberOfTasksRemoved = 0;

        for (int index : indexes) {
            assert (index - numberOfTasksRemoved >= 0) && (index - numberOfTasksRemoved < this.tasks.size())
                    : "Compensated index out of range, indexes must be sorted and distinct";

            Task removed = this.tasks.remove(index - numberOfTasksRemoved);
            numberOfTasksRemoved++;
            output.append(removed).append("\n  ");
        }

        int remaining = this.tasks.size();
        this.storage.save(this.tasks);

        return String.format("""
                Roger, I've removed %s:
                  %s
                Now you have %d task%s in the list.
                """,
                numberOfTasksRemoved == 1 ? "this task" : "these tasks",
                output.toString().trim(), remaining, (remaining == 1 ? "" : "s"));
    }

    /**
     * Lists the outstanding tasks falling due no later than a given moment.
     *
     * @param deadline the moment tasks are measured against.
     * @return the matching tasks, soonest first, or a notice if none match.
     */
    public String listTasksDueBy(LocalDateTime deadline) {
        String output = this.tasks.stream()
                .filter(task -> task.isDueBy(deadline) && !task.isCompleted())
                .sorted(Comparator.comparing(Task::getDueDateTime))
                .map(Task::toString)
                .collect(Collectors.joining("\n"));
        String outIfEmpty = "Nothing is due by " + deadline.format(Task.DISPLAY_FORMATTER);
        return output.isEmpty() ? outIfEmpty : output;
    }

    /**
     * Reorders the tasks by deadline, soonest first. Tasks without a date follow
     * the dated ones, and completed tasks come last. The new order is written to
     * storage, so it is still in place the next time Hermes starts.
     *
     * @return the reordered tasks, numbered exactly as the list command shows them.
     * @throws HermesException if the reordered tasks could not be written to storage.
     */
    public String sort() throws HermesException {
        if (this.tasks.isEmpty()) {
            return "There is nothing to sort, your list is empty!";
        }

        cache();

        // Comparator.naturalOrder() routes through Task.compareTo, which already
        // defines this ordering, rather than restating it here.
        this.tasks.sort(Comparator.naturalOrder());
        this.storage.save(this.tasks);

        return String.format("I have sorted your tasks by deadline:%n%s", this);
    }

    /**
     * Lists the tasks whose taskDescription contains a given keyword.
     *
     * @param keyword the text to look for, already in lower case.
     * @return the matching tasks, or a notice if none match.
     */
    public String findTasks(String keyword) {
        if (this.tasks.isEmpty()) {
            return "There is nothing to search, your list is empty!";
        }

        String output = this.tasks.stream()
                .filter(task -> task.getTaskDescription().toLowerCase().contains(keyword))
                .map(Task::toString)
                .collect(Collectors.joining("\n"));

        String outputIfEmpty = "Apologies, no task match " + keyword + " :<";

        return output.isEmpty() ? outputIfEmpty : output;
    }

    /**
     * Reverts the list to its previous state by one step.
     *
     * @return the updated tasks after the undo action was performed
     * @throws HermesException if the previous tasks could not be written to storage.
     */
    public String undo() throws HermesException {
        if (this.cachedTasks.isEmpty()) {
            return "There is nothing to undo, you are at the most recent state for this session already.";
        }

        this.tasks = cachedTasks.pop();
        this.storage.save(this.tasks);
        return String.format("I have undone the most recent action, here is the current list: %n%s", this);
    }

    /**
     * Saves the current state of the task list before making any updates,
     * enables easy undo action.
     */
    private void cache() {
        ArrayList<Task> tasksToSave = new ArrayList<>();

        for (Task task : this.tasks) {
            tasksToSave.add(task.copy());
        }

        cachedTasks.push(tasksToSave);
    }

    /**
     * Checks to ensure that the index inputted into the method is a valid one
     *
     * @param index index of task we are manipulating.
     * @throws HermesException error indicating index out of bounds exception.
     */
    private void checkIndex(int index) throws HermesException {
        if (index < 0 || index >= this.tasks.size()) {
            throw new HermesException("Sorry, I have no task numbered " + (index + 1) + ".");
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

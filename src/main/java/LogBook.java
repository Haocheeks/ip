import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;


public class LogBook {
    private ArrayList<Task> logBook;
    private File hermesFile;
    private int skippedLines = 0;

    public LogBook() {
        this.logBook = new ArrayList<>();
        this.hermesFile = new File("data/Hermes.txt");
        loadLogBook();
    }

    /**
     * Loads saved tasks from data/Hermes.txt into this LogBook.
     *
     * <p>A missing file is treated as a first run rather than an error: the
     * data directory is created if needed and the LogBook simply starts
     * empty, leaving {@link #save()} to create the file.
     */
    private void loadLogBook() {
        if (!this.hermesFile.exists()) {
            this.hermesFile.getParentFile().mkdirs();
            return;
        }

        try {
            Scanner scanner = new Scanner(this.hermesFile);
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine().trim();
                String[] parts = line.split("\\|");
                String type = parts[0].trim();

                // How many fields a well-formed line of this type must have.
                // A type letter we do not recognise expects none, so it is skipped.
                int expectedFields = switch (type) {
                    case "T" -> 3;
                    case "D" -> 4;
                    case "E" -> 5;
                    default -> 0;
                };

                if (expectedFields == 0 || !isWellFormed(parts, expectedFields)) {
                    this.skippedLines++;
                    continue;
                }

                boolean isCompleted = "1".equals(parts[1].trim());

                try {
                    switch (type) {
                        case "T" -> logBook.add(new ToDo(isCompleted, parts[2].trim()));
                        case "D" -> logBook.add(new Deadline(isCompleted, parts[2].trim(), parts[3].trim()));
                        case "E" -> logBook.add(
                                new Event(isCompleted, parts[2].trim(), parts[3].trim(), parts[4].trim()));
                    }
                } catch (DateTimeParseException e) {
                    this.skippedLines++;
                }
            }
            scanner.close();
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Checks that a line read from the data file has the shape its type letter
     * promises, so the caller can index into it safely.
     *
     * <p>The field count must match exactly. Too few fields would cause an
     * {@code ArrayIndexOutOfBoundsException}; too many means a field contained
     * the '|' separator, which would silently truncate the task.
     *
     * @param parts the line split on '|'
     * @param expectedFields how many fields this type of task is stored with
     * @return true if the line is safe to read
     */
    private boolean isWellFormed(String[] parts, int expectedFields) {
        if (parts.length != expectedFields) {
            return false;
        }

        // split() discards trailing empty fields, so length alone cannot tell
        // us a field is present but empty.
        for (String part : parts) {
            if (part.isBlank()) {
                return false;
            }
        }

        String flag = parts[1].trim();
        return flag.equals("0") || flag.equals("1");
    }

    /**
     * Returns how many lines of the data file could not be understood on the
     * most recent load.
     *
     * @return the number of skipped lines, zero if the file was read in full
     */
    public int getSkippedLines() {
        return this.skippedLines;
    }

    /**
     * Stores an already-built task and reports how many tasks are now held.
     *
     * <p>The caller decides which kind of {@link Task} to create, so this
     * method works unchanged for todos, deadlines and events.
     *
     * @param task the task to store
     * @return the message confirming the task was added
     * @throws HermesException if the task could not be written to the data file
     */
    public String log(Task task) throws HermesException {
        this.logBook.add(task);
        this.save();
        return String.format("""
                Got it. I've added this task:
                  %s
                Now you have %d task%s in the list.
                """, task, this.logBook.size(), (this.logBook.size() == 1 ? "" : "s"));
    }

    /**
     * Writes every task held in memory to data/Hermes.txt, replacing whatever
     * the file held before.
     *
     * <p>The in-memory list is the single source of truth: the file is only ever
     * a dump of it. Rewriting the file in full means no operation has to keep
     * file lines and list indices in step by hand.
     *
     * <p>A failed write is reported rather than fatal: the change is already in
     * memory, so the session can carry on and the user is told it will not
     * survive being closed.
     *
     * @throws HermesException if the data file could not be written
     */
    private void save() throws HermesException {
        List<String> lines = new ArrayList<>();

        for (Task task : this.logBook) {
            lines.add(task.fileContent());
        }

        try {
            Files.write(this.hermesFile.toPath(), lines);
        } catch (IOException e) {
            throw new HermesException(String.format("""
                    Sorry, I could not save to %s.
                    Your change applies to this session, but I will not remember it
                    once I close.
                    """, this.hermesFile));
        }
    }

    /**
     * Marks a task as completed
     *
     * @param id the task index in the array
     * @return a response
     */
    public String mark(int id) throws HermesException {
        checkIndex(id);
        String output = this.logBook.get(id).mark();
        this.save();
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
        String output = this.logBook.get(id).unmark();
        this.save();
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

        Task removed = this.logBook.remove(id);
        int remaining = this.logBook.size();
        this.save();

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
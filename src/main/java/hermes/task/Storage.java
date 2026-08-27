package hermes.task;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import hermes.HermesException;

/**
 * Reads tasks from the data file and writes them back to it.
 *
 * <p>This is the only class that knows the file exists, what a stored line
 * looks like, or how to turn one into a {@link Task}. Everything else works
 * with tasks in memory.
 */
public class Storage {

    /** The character used to separate fields in a stored line. */
    private static final String SEPARATOR = "|";

    private final File file;

    /** How many lines the most recent {@link #load()} could not understand. */
    private int skippedLines = 0;

    /**
     * @param filePath where the tasks are stored, relative to the working directory
     */
    public Storage(String filePath) {
        this.file = new File(filePath);
    }

    /**
     * Reads every task the file holds.
     *
     * <p>A missing file is treated as a first run rather than an error: the
     * data directory is created if needed and no tasks are returned, leaving
     * {@link #save(List)} to create the file.
     *
     * <p>A line that cannot be understood is counted and skipped rather than
     * allowed to stop the load, so one damaged line does not cost the user
     * every other task. Ask {@link #getSkippedLines()} how many were lost.
     *
     * @return the tasks read from the file, in the order they were stored
     */
    public ArrayList<Task> load() {
        ArrayList<Task> tasks = new ArrayList<>();
        this.skippedLines = 0;

        if (!this.file.exists()) {
            this.file.getParentFile().mkdirs();
            return tasks;
        }

        try {
            Scanner scanner = new Scanner(this.file);
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
                        case "T" -> tasks.add(new ToDo(isCompleted, parts[2].trim()));
                        case "D" -> tasks.add(new Deadline(isCompleted, parts[2].trim(), parts[3].trim()));
                        case "E" -> tasks.add(
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

        return tasks;
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
     * Writes every given task to the data file, replacing whatever it held
     * before.
     *
     * <p>The caller's list is the single source of truth: the file is only ever
     * a dump of it. Rewriting the file in full means no operation has to keep
     * file lines and list indices in step by hand.
     *
     * <p>A failed write is reported rather than fatal: the change is already in
     * memory, so the session can carry on and the user is told it will not
     * survive being closed.
     *
     * @param tasks the tasks to write, in the order they should be stored
     * @throws HermesException if the data file could not be written
     */
    public void save(List<Task> tasks) throws HermesException {
        List<String> lines = new ArrayList<>();

        for (Task task : tasks) {
            lines.add(task.fileContent());
        }

        try {
            Files.write(this.file.toPath(), lines);
        } catch (IOException e) {
            throw new HermesException(String.format("""
                    Sorry, I could not save to %s.
                    Your change applies to this session, but I will not remember it
                    once I close.
                    """, this.file));
        }
    }

    /**
     * Rejects any user-supplied field containing the '|' character.
     *
     * <p>Tasks are stored as pipe-separated fields, so a '|' inside a field
     * would make the saved line impossible to read back correctly. Refusing it
     * before a task is built means Hermes never writes a line it cannot
     * understand later.
     *
     * <p>This is static because it describes the storage format itself rather
     * than any particular file, so a caller can check text without holding a
     * Storage of its own.
     *
     * @param fields the parts of the task that will be written to file
     * @throws HermesException if any field contains the separator
     */
    public static void rejectSeparator(String... fields) throws HermesException {
        for (String field : fields) {
            if (field.contains(SEPARATOR)) {
                throw new HermesException(String.format(
                        "Sorry, a task cannot contain '%s', as I use it to separate "
                                + "fields when saving your tasks.", SEPARATOR));
            }
        }
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
}

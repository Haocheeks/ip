import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;


public class LogBook {
    private ArrayList<Task> logBook;
    private File hermesFile;

    public LogBook() {
        this.logBook = new ArrayList<>();
        this.hermesFile = new File("data/Hermes.txt");
        loadLogBook();
    }

    /**
     * Loads data from data/Hermes.txt into the LogBook class
     */
    private void loadLogBook() {
        try {
            Scanner scanner = new Scanner(this.hermesFile);
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine().trim();
                String[] parts = line.split("\\|");
                String type =  parts[0].trim();

                switch (type) {
                    case "T" -> {
                        Task task = new ToDo("1".equals(parts[1].trim()), parts[2].trim());
                        logBook.add(task);
                    }
                    case "D" -> {
                        Task task = new Deadline("1".equals(parts[1].trim()), parts[2].trim(), parts[3].trim());
                        logBook.add(task);
                    }
                    case "E" -> {
                        Task task = new Event("1".equals(parts[1].trim()), parts[2].trim(), parts[3].trim(), parts[4].trim());
                        logBook.add(task);
                    }
                }
            }

            scanner.close();
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Stores an already-built task and reports how many tasks are now held.
     *
     * <p>The caller decides which kind of {@link Task} to create, so this
     * method works unchanged for todos, deadlines and events.
     *
     * @param task the task to store
     * @return the message confirming the task was added
     */
    public String log(Task task) {
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
     */
    private void save() {
        List<String> lines = new ArrayList<>();

        for (Task task : this.logBook) {
            lines.add(task.fileContent());
        }

        try {
            Files.write(this.hermesFile.toPath(), lines);
        } catch (IOException e) {
            throw new RuntimeException(e);
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
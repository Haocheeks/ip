public class Task {

    private String message;
    private boolean completed = false;
    private int id;
    public Task(String message, int id) {
        this.message = message;
        this.id = id;
    }

    @Override
    public String toString() {
        String output = String.format(
                "%d. [%c] %s",
                this.id,
                completed ? 'X' : ' ',
                this.message);
        return output;
    }
}

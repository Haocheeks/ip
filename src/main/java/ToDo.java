public class ToDo extends Task {

    public ToDo(String message) {
        super(message);
    }

    public ToDo(boolean isCompleted, String message) {
        super(isCompleted, message);
    }

    @Override
    public String fileContent() {
        return String.format("T | %d | %s", this.isCompleted ? 1 : 0, this.message);
    }

    @Override
    public String toString(){
        return String.format("[T]%s", super.toString());
    }
}

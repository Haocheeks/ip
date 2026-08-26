public class ToDo extends Task {

    public ToDo(String message) {
        super(message);
    }

    public ToDo(boolean isCompleted, String message) {
        super(isCompleted, message);
    }

    public String fileContent() {
        String output = String.format("T | %d | %s", this.isCompleted ? 1 : 0, this.message);
        return output;
    }

    @Override
    public String toString(){
        String output = String.format("[T]%s", super.toString());
        return output;
    }
}

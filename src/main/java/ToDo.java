public class ToDo extends Task {

    public ToDo(String message) {
        super(message);
    }

    @Override
    public String toString(){
        String output = String.format("[T]%s", super.toString());
        return output;
    }
}

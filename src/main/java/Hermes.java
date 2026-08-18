import java.util.Scanner;

public class Hermes {

    private static final String EXIT_COMMAND = "bye"; // possible to use ENUM here
    private static final String LIST_COMMAND = "list";
    private static LogBook logBook = new LogBook(100);

    public static void main(String[] args) {
        String opening = """
                ____________________________________________________________
                 _   _
                | | | | ___ _ __ _ __ ___   ___  ___
                | |_| |/ _ \\ '__| '_ ` _ \\ / _ \\/ __|
                |  _  |  __/ |  | | | | | |  __/\\__ \\
                |_| |_|\\___|_|  |_| |_| |_|\\___||___/
                Greetings! I am Hermes.
                How may I assist you today?
                ____________________________________________________________
                
                """;

        Scanner scanner = new Scanner(System.in);

        // While loop going through all the input
        while (scanner.hasNextLine()) {
            String command = scanner.nextLine().trim();

            if (command.equals(EXIT_COMMAND)) {
                System.out.println("""
                ____________________________________________________________
                Goodbye, thank you for contacting me!
                ____________________________________________________________
                
                """);
                break;
            }

            if (command.equals(LIST_COMMAND)) {
                System.out.println(logBook);
                continue;
            }

            logBook.log(command);
        }
    }
}

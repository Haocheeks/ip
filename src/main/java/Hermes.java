import java.util.Scanner;

public class Hermes {

    private static final String EXIT_COMMAND = "bye"; // possible to use ENUM here
    private static final String LIST_COMMAND = "list";
    private static String[] logBook = new String[100];
    private static int curr = 0;

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
                respond("Goodbye, thank you for contacting me!");
                break;
            }

            if (command.equals(LIST_COMMAND)) {
                vommit();
                continue;
            }

            log(command);
        }
    }

    /**
     * Prints a single message wrapped in divider lines, so that every reply
     * from Hermes has the same shape.
     *
     * @param message the text to show to the user
     */
    private static void respond(String message) {

        String output = String.format("""
                ____________________________________________________________
                %s
                ____________________________________________________________
                
                """, message);

        System.out.println(output);
    }

    private static void log(String message) {
        Hermes.logBook[curr++] = message;
        respond("Added: " + message);
    }

    private static void vommit() {
        String output = """
               ____________________________________________________________
                
                """;

        for (int i = 0; i < Hermes.curr; i++) {
            String temp = String.format("%d. %s\n", i + 1, Hermes.logBook[i]);
            output += temp;
        }

        output += "____________________________________________________________";

        System.out.println(output);
    }
}

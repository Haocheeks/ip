package hermes;

import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

/** Main Window*/
public class MainWindow extends AnchorPane {

    @FXML
    private ScrollPane scrollPane;
    @FXML
    private TextField userInput;
    @FXML
    private Button sendButton;
    @FXML
    private VBox dialogContainer;

    private Hermes hermes;

    private String openingGreeting = "Greetings! I am Hermes. How may I assist you today?";

    private Image userImage = new Image(this.getClass().getResourceAsStream("/images/userImage.png"));
    private Image hermesImage = new Image(this.getClass().getResourceAsStream("/images/hermesImage.png"));

    @FXML
    public void initialize() {
        scrollPane.vvalueProperty().bind(dialogContainer.heightProperty());
        dialogContainer.getChildren().add(
                DialogBox.getHermesDialog(this.openingGreeting, hermesImage));
    }

    public void setHermes(Hermes hermes) {
        this.hermes = hermes;
        String warning = this.hermes.warnAboutSkippedLines();
        if (!warning.isEmpty()) {
            dialogContainer.getChildren().add(
                    DialogBox.getHermesDialog(warning, hermesImage));
        }
    }

    @FXML
    private void handleUserInput() {
        String input = userInput.getText();
        String response = this.hermes.getResponse(input);

        dialogContainer.getChildren().addAll(
                DialogBox.getUserDialog(input, userImage),
                DialogBox.getHermesDialog(response, hermesImage)
        );
        userInput.clear();

        if (input.equalsIgnoreCase("bye")) {
            PauseTransition pause = new PauseTransition(Duration.seconds(2));
            pause.setOnFinished(event -> Platform.exit());
            pause.play();
        }
    }
}

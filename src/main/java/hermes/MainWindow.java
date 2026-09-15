package hermes;

import hermes.ui.Ui;
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

/**
 * Main Window
 */
public class MainWindow extends AnchorPane {

    @FXML
    private ScrollPane scrollPane;
    @FXML
    private TextField userInput;
    @FXML
    private Button sendButton;
    @FXML
    private VBox dialogContainer;

    private final Image userImage =
            new Image(this.getClass().getResourceAsStream("/images/userImage.png"));
    private final Image hermesImage =
            new Image(this.getClass().getResourceAsStream("/images/hermesImage.png"));

    private Hermes hermes;

    /**
     * Prepares the window once FXML has supplied the controls it declares.
     *
     * <p>The greeting is shown here rather than alongside the load warning
     * because this runs before the back end is attached, and the greeting is
     * the part that needs nothing from it.
     */
    @FXML
    public void initialize() {
        scrollPane.vvalueProperty().bind(dialogContainer.heightProperty());
        dialogContainer.getChildren().add(
                DialogBox.getHermesDialog(Ui.GREETING, hermesImage));
    }

    public void setHermes(Hermes hermes) {
        this.hermes = hermes;
        String warning = this.hermes.describeSkippedLines();
        if (!warning.isEmpty()) {
            dialogContainer.getChildren().add(
                    DialogBox.getHermesDialog(warning, hermesImage));
        }
    }

    @FXML
    private void handleUserInput() {
        String input = userInput.getText().trim();

        if (input.isEmpty()) {
            return;
        }

        Response response = this.hermes.getResponse(input);
        DialogBox reply = response.isError()
                ? DialogBox.getErrorDialog(response.text(), hermesImage)
                : DialogBox.getHermesDialog(response.text(), hermesImage);

        dialogContainer.getChildren().addAll(
                DialogBox.getUserDialog(input, userImage),
                reply
        );
        userInput.clear();

        if (response.isExit()) {
            PauseTransition pause = new PauseTransition(Duration.seconds(2));
            pause.setOnFinished(event -> Platform.exit());
            pause.play();
        }
    }
}

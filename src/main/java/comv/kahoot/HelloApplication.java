package comv.kahoot;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Optional;

public class HelloApplication extends Application {

    private Stage primaryStage;
    private Stage answerWindow;

    @Override
    public void start(Stage stage) throws IOException {
        // To test server
        Server server = new Server("Kahoot GmbH", 6000);
        server.startup();

        primaryStage = stage;

        FXMLLoader question_window = new FXMLLoader(getClass().getResource("question_window.fxml"));
        FXMLLoader answer_window = new FXMLLoader(getClass().getResource("answer_window.fxml"));
        Pane pane = question_window.load();
        Scene scene = new Scene(pane, 600, 400);
        stage.setFullScreen(true);
        stage.setTitle("Hello!");
        stage.setScene(scene);

        // Ereignisbehandlung für die Knöpfe
        Button answerButton1 = (Button) answer_window.getNamespace().get("answerButton1");
        Button answerButton2 = (Button) answer_window.getNamespace().get("answerButton2");
        Button answerButton3 = (Button) answer_window.getNamespace().get("answerButton3");
        Button answerButton4 = (Button) answer_window.getNamespace().get("answerButton4");

        answerButton1.setOnAction(event -> openAnswerWindow());
        answerButton2.setOnAction(event -> openAnswerWindow());
        answerButton3.setOnAction(event -> openAnswerWindow());
        answerButton4.setOnAction(event -> openAnswerWindow());

        stage.addEventHandler(KeyEvent.KEY_RELEASED, event -> {
            if (event.getCode() == KeyCode.ESCAPE) {
                event.consume();
                showCloseConfirmationDialog();
            }
        });

        stage.show();
    }

    private void openAnswerWindow() {
        try {
            if (answerWindow == null) {
                answerWindow = new Stage();
                FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("answer_window.fxml"));
                Pane pane = fxmlLoader.load();
                Scene scene = new Scene(pane, 600, 400);
                answerWindow.setScene(scene);
                answerWindow.setTitle("Answer");
                answerWindow.setOnCloseRequest(event -> answerWindow = null);
            }
            answerWindow.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void showCloseConfirmationDialog() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Bestätigung");
        alert.setHeaderText("Fenster schließen");
        alert.setContentText("Möchten Sie das Fenster wirklich schließen?");
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            primaryStage.close();
            if (answerWindow != null) {
                answerWindow.close();
            }
        }
    }

    public static void main(String[] args) {
        launch();
    }
}

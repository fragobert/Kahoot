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
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Optional;

public class GUI extends Application {
    private Stage primaryStage;
    private Pane questionPane;
    private Pane trueFalsePane;
    private Pane answerPane;
    private boolean[] selectedButtons = {false, false, false, false};


    private String questionType = "trueFalse"; // default case is normal question with 4 answers (multiple choice)

    @Override
    public void start(Stage stage) throws Exception {
        switchToNextQuestion();

        boolean[] correctAnswers = {true, false, false, true};

        switch (questionType) {
            case "trueFalse" -> {
                primaryStage = stage;

                FXMLLoader trueFalseWindowLoader = new FXMLLoader(getClass().getResource("true_false_window.fxml"));
                questionPane = trueFalseWindowLoader.load();
                Scene scene = new Scene(questionPane, 1366, 768);
                primaryStage.setScene(scene);
                primaryStage.setFullScreen(false);

                Button trueButton = (Button) questionPane.lookup("#trueButton");
                Button falseButton = (Button) questionPane.lookup("#falseButton");

                trueButton.setOnAction(event -> toggleButtonSelection(trueButton, 0));
                falseButton.setOnAction(event -> toggleButtonSelection(falseButton, 1));

                boolean[] selectedButtonTF = {false, false};

                trueButton.setOnAction(event -> {
                    selectedButtonTF[0] = true;
                    switchToTrueFalseAnswer(correctAnswers, selectedButtonTF);
                });

                falseButton.setOnAction(event -> {
                    selectedButtonTF[1] = true;
                    switchToTrueFalseAnswer(correctAnswers, selectedButtonTF);
                });

                stage.addEventHandler(KeyEvent.KEY_RELEASED, event -> {
                    if (event.getCode() == KeyCode.ESCAPE) {
                        event.consume();
                        showCloseConfirmationDialog();
                    }
                });

                stage.show();
            }

            default -> {
                primaryStage = stage;

                FXMLLoader questionWindowLoader = new FXMLLoader(getClass().getResource("question_window.fxml"));
                questionPane = questionWindowLoader.load();
                Scene scene = new Scene(questionPane, 1366, 768);
                stage.setFullScreen(false);
                stage.setTitle("Kayeet");
                stage.setScene(scene);

                Button questionButton1 = (Button) questionPane.lookup("#questionButton1");
                Button questionButton2 = (Button) questionPane.lookup("#questionButton2");
                Button questionButton3 = (Button) questionPane.lookup("#questionButton3");
                Button questionButton4 = (Button) questionPane.lookup("#questionButton4");

                questionButton1.setOnAction(event -> toggleButtonSelection(questionButton1, 0));
                questionButton2.setOnAction(event -> toggleButtonSelection(questionButton2, 1));
                questionButton3.setOnAction(event -> toggleButtonSelection(questionButton3, 2));
                questionButton4.setOnAction(event -> toggleButtonSelection(questionButton4, 3));

                Button submitButton = (Button) questionPane.lookup("#submitButton");

                submitButton.setOnAction(event -> switchToAnswerWindow(correctAnswers, selectedButtons));

                stage.addEventHandler(KeyEvent.KEY_RELEASED, event -> {
                    if (event.getCode() == KeyCode.ESCAPE) {
                        event.consume();
                        showCloseConfirmationDialog();
                    }
                });

                stage.show();
            }
        }

    }

    private void toggleButtonSelection(Button button, int index) {
        Text chosenAnswer1 = (Text) questionPane.lookup("#chosenAnswer1");
        Text chosenAnswer2 = (Text) questionPane.lookup("#chosenAnswer2");
        Text chosenAnswer3 = (Text) questionPane.lookup("#chosenAnswer3");
        Text chosenAnswer4 = (Text) questionPane.lookup("#chosenAnswer4");

        selectedButtons[index] = !selectedButtons[index];

        if (selectedButtons[0]) {
            chosenAnswer1.setVisible(true);
        } else {
            chosenAnswer1.setVisible(false);
        }

        if (selectedButtons[1]) {
            chosenAnswer2.setVisible(true);
        } else {
            chosenAnswer2.setVisible(false);
        }

        if (selectedButtons[2]) {
            chosenAnswer3.setVisible(true);
        } else {
            chosenAnswer3.setVisible(false);
        }

        if (selectedButtons[3]) {
            chosenAnswer4.setVisible(true);
        } else {
            chosenAnswer4.setVisible(false);
        }
    }

    private void switchToTrueFalseAnswer(boolean[] correctAnswers, boolean[] selectedButton) {
        try {
            FXMLLoader trueFalseAnswerWindowLoader = new FXMLLoader(getClass().getResource("true_false_window_answer.fxml"));
            answerPane = trueFalseAnswerWindowLoader.load();
            Scene scene = new Scene(answerPane, 1366, 768);
            primaryStage.setScene(scene);
            primaryStage.setFullScreen(false);

            Text chosenAnswer1 = (Text) answerPane.lookup("#chosenAnswer1");
            Text chosenAnswer2 = (Text) answerPane.lookup("#chosenAnswer2");

            Text correctAnswer1 = (Text) answerPane.lookup("#correctAnswer1");
            Text correctAnswer2 = (Text) answerPane.lookup("#correctAnswer2");

            if (selectedButton[0]) {
                chosenAnswer1.setVisible(true);
            }
            if (selectedButton[1]) {
                chosenAnswer2.setVisible(true);
            }

            if (correctAnswers[0]) {
                correctAnswer1.setVisible(true);
            }
            if (correctAnswers[1]) {
                correctAnswer2.setVisible(true);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void switchToAnswerWindow(boolean[] correctAnswers, boolean[] selectedButton) {
        try {
            FXMLLoader answerWindowLoader = new FXMLLoader(getClass().getResource("answer_window.fxml"));
            answerPane = answerWindowLoader.load();
            Scene scene = new Scene(answerPane, 1366, 768);
            primaryStage.setScene(scene);
            primaryStage.setFullScreen(false);

            Text chosenAnswer1 = (Text) answerPane.lookup("#chosenAnswer1");
            Text chosenAnswer2 = (Text) answerPane.lookup("#chosenAnswer2");
            Text chosenAnswer3 = (Text) answerPane.lookup("#chosenAnswer3");
            Text chosenAnswer4 = (Text) answerPane.lookup("#chosenAnswer4");

            Text correctAnswer1 = (Text) answerPane.lookup("#correctAnswer1");
            Text correctAnswer2 = (Text) answerPane.lookup("#correctAnswer2");
            Text correctAnswer3 = (Text) answerPane.lookup("#correctAnswer3");
            Text correctAnswer4 = (Text) answerPane.lookup("#correctAnswer4");


            if (selectedButton[0]) {
                chosenAnswer1.setVisible(true);
            } else {
                chosenAnswer1.setVisible(false);
            }
            if (selectedButton[1]) {
                chosenAnswer2.setVisible(true);
            } else {
                chosenAnswer2.setVisible(false);
            }
            if (selectedButton[2]) {
                chosenAnswer3.setVisible(true);
            } else {
                chosenAnswer3.setVisible(false);
            }
            if (selectedButton[3]) {
                chosenAnswer4.setVisible(true);
            } else {
                chosenAnswer4.setVisible(false);
            }

            if (correctAnswers[0]) {
                correctAnswer1.setVisible(true);
            }
            if (correctAnswers[1]) {
                correctAnswer2.setVisible(true);
            }
            if (correctAnswers[2]) {
                correctAnswer3.setVisible(true);
            }
            if (correctAnswers[3]) {
                correctAnswer4.setVisible(true);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    private void handleAnswerButton(Button button, int index, boolean[] selectedButtons) {
        String buttonText = button.getText();
        boolean isMarked = selectedButtons[index];
        System.out.println("Pressed answer button: " + buttonText + " (Marked: " + isMarked + ")");
    }

    private void showCloseConfirmationDialog() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation");
        alert.setHeaderText("Do you want to exit the program?");
        alert.setContentText("Press OK to exit or Cancel to continue.");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            primaryStage.close();
        }
    }

    private void switchToNextQuestion() {
        this.questionType = "";
    }

    public static void main(String[] args) {
        launch(args);
    }
}
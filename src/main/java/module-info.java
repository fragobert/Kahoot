module comv.kahoot {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.base;


    opens comv.kahoot to javafx.fxml;
    exports comv.kahoot;
    exports comv.kahoot.quiz;
    opens comv.kahoot.quiz to javafx.fxml;
    exports comv.kahoot.client;
    opens comv.kahoot.client to javafx.fxml;
    exports comv.kahoot.backend;
    opens comv.kahoot.backend to javafx.fxml;
}
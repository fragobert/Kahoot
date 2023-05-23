module comv.kahoot {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.base;


    opens comv.kahoot to javafx.fxml;
    exports comv.kahoot;
}
module comv.kahoot {
    requires javafx.controls;
    requires javafx.fxml;
            
                            
    opens comv.kahoot to javafx.fxml;
    exports comv.kahoot;
}
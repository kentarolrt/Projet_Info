module com.example.medmap {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.example.medmap to javafx.fxml;
    exports com.example.medmap;
}
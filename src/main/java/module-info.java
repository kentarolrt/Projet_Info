module com.example.medmap {
    requires javafx.controls;
    requires javafx.fxml;

    exports com.example.medmap.model;
    exports com.example.medmap.algo;

    opens com.example.medmap.algo to javafx.fxml;
    opens com.example.medmap.model to javafx.fxml;
    opens com.example.medmap to javafx.fxml;

    exports com.example.medmap;
}
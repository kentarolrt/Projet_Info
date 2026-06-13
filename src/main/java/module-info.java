module com.example.medmap {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;
    requires javafx.graphics;

    exports com.example.medmap;
    exports com.example.medmap.model;
    exports com.example.medmap.algo;
    exports com.example.medmap.map;

    opens com.example.medmap     to javafx.fxml;
    opens com.example.medmap.algo  to javafx.fxml;
    opens com.example.medmap.model to javafx.fxml;
    opens com.example.medmap.map   to javafx.fxml;
}
package application;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.web.WebView;
import javafx.stage.Stage;

public class MapScreen extends Application {

    @Override
    public void start(Stage stage) {

        // Navigateur intégré
        WebView webView = new WebView();

        // Charger Google Maps
        webView.getEngine().load("https://www.google.com/maps");

        BorderPane root = new BorderPane();
        root.setCenter(webView);

        Scene scene = new Scene(root, 1200, 800);

        stage.setTitle("Google Maps");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
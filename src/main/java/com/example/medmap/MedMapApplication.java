package com.example.medmap;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

// programme main de lancement

public class MedMapApplication extends Application {

    @Override
    public void start(Stage stage) {
        HomeScreen homeScreen = new HomeScreen(stage);

        Scene scene = new Scene(homeScreen, 900, 700);
        scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
        
        
        stage.setTitle("MedMap – Accueil");
        stage.setResizable(false);
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}


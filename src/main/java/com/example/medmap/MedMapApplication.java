package com.example.medmap;

import com.example.medmap.map.MapView;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

// programme main de lancement

public class MedMapApplication extends Application {

    @Override
    public void start(Stage stage) {
        MapView mapView = new MapView();

        Scene scene = new Scene(mapView, 900, 700);
        stage.setTitle("MedMap – Déserts médicaux – Cergy-Pontoise");
        stage.setResizable(false);
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
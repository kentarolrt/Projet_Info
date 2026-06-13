package com.example.medmap;

import com.example.medmap.map.MapView;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

public class HomeScreen extends VBox {

    public HomeScreen(Stage stage) {
        setAlignment(Pos.CENTER);
        setSpacing(25);
        setPadding(new Insets(40));

        Label title = new Label("MedMap");
        title.setFont(Font.font("System", FontWeight.BOLD, 42));

        Label subtitle = new Label("Visualisation des déserts médicaux");
        subtitle.setFont(Font.font("System", FontWeight.NORMAL, 22));

        Label description = new Label(
                "Cette application permet de visualiser les zones d'influence médicale\n"
                        + "à l'aide du diagramme de Voronoï et de la triangulation de Delaunay.\n\n"
                        + "Les médecins sont représentés par des points de référence.\n"
                        + "Les zones colorées indiquent les territoires associés au médecin le plus proche."
        );
        description.setFont(Font.font("System", 14));
        description.setAlignment(Pos.CENTER);
        description.setWrapText(true);
        description.setMaxWidth(650);

        Button openMapButton = new Button("Ouvrir la carte");
        openMapButton.setFont(Font.font("System", FontWeight.BOLD, 16));
        openMapButton.setPrefWidth(220);
        openMapButton.setPrefHeight(45);

        Button quitButton = new Button("Quitter");
        quitButton.setPrefWidth(220);
        quitButton.setPrefHeight(35);

        openMapButton.setOnAction(event -> {
            MapView mapView = new MapView();
            Scene mapScene = new Scene(mapView, 900, 700);

            stage.setTitle("MedMap – Déserts médicaux – Cergy-Pontoise");
            stage.setResizable(false);
            stage.setScene(mapScene);
        });

        quitButton.setOnAction(event -> stage.close());

        getChildren().addAll(title, subtitle, description, openMapButton, quitButton);
    }
}

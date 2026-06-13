package com.example.medmap;

import com.example.medmap.map.MapView;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class HomeScreen extends VBox {

    public HomeScreen(Stage stage) {
        setAlignment(Pos.CENTER);
        setPadding(new Insets(40));
        getStyleClass().add("home-root");

        VBox card = new VBox();
        card.setAlignment(Pos.CENTER_LEFT);
        card.setSpacing(22);
        card.setPadding(new Insets(48));
        card.getStyleClass().add("home-card");

        Label badge = new Label("OUTIL D’AIDE À LA DÉCISION PUBLIQUE");
        badge.getStyleClass().add("badge");

        Label title = new Label("MedMap");
        title.getStyleClass().add("title");

        Label subtitle = new Label("Cartographier les déserts médicaux");
        subtitle.getStyleClass().add("subtitle");

        Label description = new Label(
                "Analysez les zones d’influence médicale à partir des diagrammes de Voronoï "
                        + "et de la triangulation de Delaunay.\n\n"
                        + "Les médecins sont représentés par des points de référence. "
                        + "Les patients sont rattachés au médecin le plus proche afin d’identifier "
                        + "les zones de tension, de couverture ou de déséquilibre territorial."
        );
        description.setWrapText(true);
        description.setMaxWidth(620);
        description.getStyleClass().add("description");

        HBox stats = new HBox(16);
        stats.setAlignment(Pos.CENTER_LEFT);

        VBox stat1 = createStat("Voronoï", "Zones d’influence");
        VBox stat2 = createStat("Delaunay", "Analyse spatiale");
        VBox stat3 = createStat("ARS", "Aide à la décision");

        stats.getChildren().addAll(stat1, stat2, stat3);

        HBox buttons = new HBox(14);
        buttons.setAlignment(Pos.CENTER_LEFT);

        Button openMapButton = new Button("Ouvrir la carte");
        openMapButton.getStyleClass().add("primary-button");

        Button quitButton = new Button("Quitter");
        quitButton.getStyleClass().add("secondary-button");

        buttons.getChildren().addAll(openMapButton, quitButton);

        openMapButton.setOnAction(event -> {
            MapView mapView = new MapView();
            Scene mapScene = new Scene(mapView, 900, 700);
            mapScene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());

            stage.setTitle("MedMap – Déserts médicaux – Cergy-Pontoise");
            stage.setResizable(false);
            stage.setScene(mapScene);
        });

        quitButton.setOnAction(event -> stage.close());

        card.getChildren().addAll(badge, title, subtitle, description, stats, buttons);
        getChildren().add(card);
    }

    private VBox createStat(String title, String text) {
        VBox box = new VBox(4);
        box.getStyleClass().add("stat-card");

        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("stat-title");

        Label textLabel = new Label(text);
        textLabel.getStyleClass().add("stat-text");

        box.getChildren().addAll(titleLabel, textLabel);
        return box;
    }
}

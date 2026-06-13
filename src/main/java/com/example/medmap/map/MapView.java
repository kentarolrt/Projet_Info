package com.example.medmap.map;

import com.example.medmap.algo.DelaunayVoronoi;
import com.example.medmap.model.Doctor;
import com.example.medmap.model.DoctorData;
import com.example.medmap.model.Point;
import com.example.medmap.utils.ConsoleLogger;
import javafx.application.Platform;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.TextInputDialog;
import javafx.scene.image.Image;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.input.MouseButton;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import com.example.medmap.model.Patient;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class MapView extends Pane {
	private int zoom = 9;
	private static final int MIN_ZOOM = 9;
	private static final int MAX_ZOOM = 16;

	private static final double MAP_W = 900;
	private static final double MAP_H = 700;

	// Limites approximatives de l'Île-de-France
	private static final double IDF_MIN_LAT = 48.05;
	private static final double IDF_MAX_LAT = 49.25;
	private static final double IDF_MIN_LNG = 1.40;
	private static final double IDF_MAX_LNG = 3.60;

    // canvas
    private final Canvas tileCanvas    = new Canvas(MAP_W, MAP_H);
    private final Canvas overlayCanvas = new Canvas(MAP_W, MAP_H);

    // données
    private final List<Doctor>   doctors;
    private Doctor movingDoctor = null;
    private final List<double[]> screenCoords = new ArrayList<>();
    private final List<Color>    palette;
    
    private final List<Patient> patients = new ArrayList<>();
    private final List<double[]> patientScreenCoords = new ArrayList<>();

    // variables interface et analyse
    private Doctor selectedDoctor = null;
    private double maxVertexDistanceKm = 0.0;
    private Point furthestVertex = null;
    private double selectedDocX = -1;
    private double selectedDocY = -1;
    private final double SEUIL_VIDE_ABSOLU_KM = 3.5;

    // déplacement et ajout
    private double lastMouseX;
    private double lastMouseY;
    private boolean isAddMode = false;
    private Button addModeBtn;
    private Button generatePatientsBtn;

    private double offsetX;
    private double offsetY;

    // constructeur
    public MapView() {
        doctors = new ArrayList<>(DoctorData.loadDoctors());
        palette = buildPalette(doctors.size());

        setPrefSize(MAP_W, MAP_H);

        setupUI();
        getChildren().addAll(tileCanvas, overlayCanvas, addModeBtn, generatePatientsBtn);
        setupZoomHandler();
        setupClickHandler();
        setupDragHandler();

        computeOffset();
        computeScreenCoords();
        computePatientScreenCoords();
        loadTiles();
        drawOverlay();

        ConsoleLogger.log("SYSTÈME", "Moteur Med-Map démarré avec " + doctors.size() + " centres.");
    }
    
 // empêche la carte de sortir trop loin de l'Île-de-France
    private void clampOffsetToIleDeFrance() {
        double minWorldX = MapProjection.lngToPixelX(IDF_MIN_LNG, zoom);
        double maxWorldX = MapProjection.lngToPixelX(IDF_MAX_LNG, zoom);

        // En projection Web Mercator, plus la latitude est au nord, plus Y est petit
        double minWorldY = MapProjection.latToPixelY(IDF_MAX_LAT, zoom);
        double maxWorldY = MapProjection.latToPixelY(IDF_MIN_LAT, zoom);

        double minOffsetX = minWorldX;
        double maxOffsetX = maxWorldX - MAP_W;

        double minOffsetY = minWorldY;
        double maxOffsetY = maxWorldY - MAP_H;

        // Si la zone est plus petite que la fenêtre, on centre au lieu de bloquer bizarrement
        if (minOffsetX > maxOffsetX) {
            offsetX = (minWorldX + maxWorldX - MAP_W) / 2.0;
        } else {
            offsetX = Math.clamp(offsetX, minOffsetX, maxOffsetX);
        }

        if (minOffsetY > maxOffsetY) {
            offsetY = (minWorldY + maxWorldY - MAP_H) / 2.0;
        } else {
            offsetY = Math.clamp(offsetY, minOffsetY, maxOffsetY);
        }
    }

    private void setupUI() {
        addModeBtn = new Button("Mode : Sélection");
        addModeBtn.setStyle("-fx-background-color: #2c3e50; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 15;");
        addModeBtn.setLayoutX(MAP_W - 180);
        addModeBtn.setLayoutY(20);

        addModeBtn.setOnAction(e -> {
            isAddMode = !isAddMode;
            if (isAddMode) {
                addModeBtn.setText("Mode : Ajout de Centre");
                addModeBtn.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 15;");
                selectedDoctor = null;
                ConsoleLogger.log("UI", "Passage en mode AJOUT. Attente d'un clic sur la carte...");
                drawOverlay();
            } else {
                addModeBtn.setText("Mode : Sélection");
                addModeBtn.setStyle("-fx-background-color: #2c3e50; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 15;");
                ConsoleLogger.log("UI", "Passage en mode SÉLECTION.");
            }
        });
        PatientsBtn = new Button("Générer patients");
        PatientsBtn.setStyle("-fx-background-color: #8e44ad; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 15;");
        PatientsBtn.setLayoutX(MAP_W - 180);
        PatientsBtn.setLayoutY(60);

        PatientsBtn.setOnAction(e -> {
            RandomPatients(30);
        });
    }

    private void setupClickHandler() {
        overlayCanvas.setOnMouseClicked(event -> {
            // On s'assure que la souris n'a pas bougé
            if (event.isStillSincePress()) {
                double mouseX = event.getX();
                double mouseY = event.getY();

                // MODE AJOUT
                if (isAddMode) {
                    if (event.getButton() == MouseButton.PRIMARY) { // Clic gauche uniquement
                        handlePointAddition(mouseX, mouseY);
                    }
                }
                // MODE SÉLECTION / SUPPRESSION
                else {
                    int nearestIndex = -1;
                    double minDistSq = Double.MAX_VALUE;

                    // Recherche du centre le plus proche
                    for (int i = 0; i < screenCoords.size(); i++) {
                        double[] sc = screenCoords.get(i);
                        double dx = sc[0] - mouseX;
                        double dy = sc[1] - mouseY;
                        double distSq = dx * dx + dy * dy;

                        if (distSq < minDistSq) {
                            minDistSq = distSq;
                            nearestIndex = i;
                        }
                    }

                    if (nearestIndex != -1) {
                        Doctor clickedDoctor = doctors.get(nearestIndex);

                        if (event.getButton() == MouseButton.PRIMARY) {
                            // CLIC GAUCHE : Sélection de la zone
                            if (selectedDoctor != clickedDoctor) {
                                selectedDoctor = clickedDoctor;
                                ConsoleLogger.log("ACTION", "Sélection de la cellule : " + selectedDoctor.getName());
                                drawOverlay();
                            }
                        } else if (event.getButton() == MouseButton.SECONDARY) {
                            // CLIC DROIT : Suppression
                            handlePointRemoval(clickedDoctor);
                        }
                    }
                }
            }
        });
    }

    // suppression d'un médecin
    private void handlePointRemoval(Doctor doctorToRemove) {
        // Sécurité géométrique : Delaunay a besoin de 3 points minimum !
        if (doctors.size() <= 3) {
            ConsoleLogger.log("ALERTE", "Impossible de supprimer : 3 centres minimum requis pour le calcul de Voronoï.");

            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Suppression impossible");
            alert.setHeaderText(null);
            alert.setContentText("La géométrie nécessite au minimum 3 points d'accès. Vous ne pouvez pas supprimer plus de centres.");
            alert.showAndWait();
            return;
        }

        // Boîte de dialogue de confirmation
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Supprimer un centre");
        alert.setHeaderText(null);
        alert.setContentText("Voulez-vous vraiment supprimer le centre : " + doctorToRemove.getName() + " ?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {

            // 1. Retirer de la liste mémoire
            doctors.remove(doctorToRemove);

            // 2. Mettre à jour le fichier texte complet
            DoctorData.saveAllDoctors(doctors);

            // 3. Désélectionner si c'était la zone actuellement étudiée
            if (selectedDoctor == doctorToRemove) {
                selectedDoctor = null;
            }

            ConsoleLogger.log("DONNÉES", "Centre supprimé : " + doctorToRemove.getName());
            ConsoleLogger.log("MOTEUR", "Recalcul de la carte après suppression...");

            // 4. Mettre à jour les coordonnées écran et redessiner
            screenCoords.clear();
            computeScreenCoords();
            drawOverlay();
        }
    }

    private void handlePointAddition(double mouseX, double mouseY) {
        TextInputDialog dialog = new TextInputDialog("Nouveau Centre Médical");
        dialog.setTitle("Ajouter un point d'accès");
        dialog.setHeaderText(null);
        dialog.setContentText("Nom du médecin ou du centre :");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(name -> {
            double lat = pixelYToLat(mouseY);
            double lng = pixelXToLng(mouseX);

            Doctor newDoctor = new Doctor(name, lat, lng);
            doctors.add(newDoctor);
            palette.add(Color.hsb(Math.random() * 360, 0.60, 0.90));

            DoctorData.appendDoctor(newDoctor);
            assignPatientsToNearestDoctors();

            screenCoords.clear();
            computeScreenCoords();
            computePatientScreenCoords();

            
            isAddMode = false;
            addModeBtn.setText("Mode : Sélection");
            addModeBtn.setStyle("-fx-background-color: #2c3e50; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 15;");

            ConsoleLogger.log("DONNÉES", "Nouveau centre ajouté : " + name + " (Lat: " + String.format("%.4f", lat) + ", Lng: " + String.format("%.4f", lng) + ")");
            ConsoleLogger.log("MOTEUR", "Recalcul complet du QuadEdge et du diagramme de Voronoï...");

            drawOverlay();
        });
    }

    private double pixelXToLng(double pxX) {
        double worldPx = MapProjection.TILE_SIZE * (1 << zoom);
        double absoluteX = pxX + offsetX;
        return (absoluteX / worldPx) * 360.0 - 180.0;
    }

    private double pixelYToLat(double pxY) {
        double worldPx = MapProjection.TILE_SIZE * (1 << zoom);
        double absoluteY = pxY + offsetY;
        double n = Math.PI - (2.0 * Math.PI * absoluteY) / worldPx;
        return Math.toDegrees(Math.atan(Math.sinh(n)));
    }

    private void setupDragHandler() {
        overlayCanvas.setOnMousePressed(event -> {
            lastMouseX = event.getX();
            lastMouseY = event.getY();

            // On ne cherche à déplacer un docteur que si on est en mode Sélection et avec un clic gauche
            if (!isAddMode && event.getButton() == MouseButton.PRIMARY) {
                double mouseX = event.getX();
                double mouseY = event.getY();

                // On regarde si le clic est sur le marqueur d'un médecin (sensibilité de 15 pixels)
                for (int i = 0; i < screenCoords.size(); i++) {
                    double[] sc = screenCoords.get(i);
                    double dx = sc[0] - mouseX;
                    double dy = sc[1] - mouseY;
                    double dist = Math.sqrt(dx * dx + dy * dy);

                    if (dist <= 15) {
                        movingDoctor = doctors.get(i);
                        ConsoleLogger.log("ACTION", "Déplacement commencé pour : " + movingDoctor.getName());
                        break;
                    }
                }
            }
        });

        overlayCanvas.setOnMouseDragged(event -> {
            if (movingDoctor != null) {
                // Conversion des pixels de la souris en coordonnées GPS réelles
                double newLng = pixelXToLng(event.getX());
                double newLat = pixelYToLat(event.getY());

                // maj de la position du docteur
                movingDoctor.setLat(newLat);
                movingDoctor.setLng(newLng);

                // Recalcul instantané des liaisons patients et des coordonnées écran
                assignPatientsToNearestDoctors();

                screenCoords.clear();
                computeScreenCoords();
                computePatientScreenCoords();

                drawOverlay();
            } else {
                // déplacement de la carte
                double deltaX = event.getX() - lastMouseX;
                double deltaY = event.getY() - lastMouseY;

                offsetX -= deltaX;
                offsetY -= deltaY;

                clampOffsetToIleDeFrance();

                lastMouseX = event.getX();
                lastMouseY = event.getY();

                screenCoords.clear();
                computeScreenCoords();
                computePatientScreenCoords();

                drawOverlay();
            }
        });

        overlayCanvas.setOnMouseReleased(event -> {
            if (movingDoctor != null) {
                // Quand on lâche le clic, on sauvegarde la nouvelle position dans le fichier texte !
                DoctorData.saveAllDoctors(doctors);
                ConsoleLogger.log("DONNÉES", "Nouvelle position sauvegardée pour : " + movingDoctor.getName());
                movingDoctor = null; // On réinitialise
            } else {
                ConsoleLogger.log("VUE", "Déplacement de la carte terminé. Chargement des tuiles OSM...");
                loadTiles();
            }
            drawOverlay();
        });
    }

    private void setupZoomHandler() {
        setOnScroll(event -> {
            if (event.getDeltaY() > 0) {
                changeZoomAtMouse(1, event.getX(), event.getY());
            } else if (event.getDeltaY() < 0) {
                changeZoomAtMouse(-1, event.getX(), event.getY());
            }
            event.consume();
        });
    }

    private void changeZoomAtMouse(int delta, double mouseX, double mouseY) {
        int oldZoom = zoom;
        int newZoom = zoom + delta;

        if (newZoom < MIN_ZOOM || newZoom > MAX_ZOOM) return;

        double worldXBeforeZoom = offsetX + mouseX;
        double worldYBeforeZoom = offsetY + mouseY;

        double scale = Math.pow(2, newZoom - oldZoom);

        double worldXAfterZoom = worldXBeforeZoom * scale;
        double worldYAfterZoom = worldYBeforeZoom * scale;

        zoom = newZoom;
        offsetX = worldXAfterZoom - mouseX;
        offsetY = worldYAfterZoom - mouseY;

        clampOffsetToIleDeFrance();

        screenCoords.clear();
        computeScreenCoords();
        computePatientScreenCoords();

        ConsoleLogger.log("VUE", "Niveau de zoom modifié : " + zoom);

        loadTiles();
        drawOverlay();
    }

    private void computeOffset() {
        double centerLat = (IDF_MIN_LAT + IDF_MAX_LAT) / 2.0;
        double centerLng = (IDF_MIN_LNG + IDF_MAX_LNG) / 2.0;

        offsetX = MapProjection.lngToPixelX(centerLng, zoom) - MAP_W / 2.0;
        offsetY = MapProjection.latToPixelY(centerLat, zoom) - MAP_H / 2.0;
    }

    private void computeScreenCoords() {
        for (Doctor d : doctors) {
            double sx = MapProjection.lngToPixelX(d.getLng(), zoom) - offsetX;
            double sy = MapProjection.latToPixelY(d.getLat(), zoom) - offsetY;
            screenCoords.add(new double[]{sx, sy});
        }
    }
    
    private void computePatientScreenCoords() {
        patientScreenCoords.clear();

        for (Patient p : patients) {
            double sx = MapProjection.lngToPixelX(p.getLng(), zoom) - offsetX;
            double sy = MapProjection.latToPixelY(p.getLat(), zoom) - offsetY;
            patientScreenCoords.add(new double[]{sx, sy});
        }
    }

    private void loadTiles() {
        GraphicsContext gc = tileCanvas.getGraphicsContext2D();

        gc.setFill(Color.web("#ddd8cc"));
        gc.fillRect(0, 0, MAP_W, MAP_H);

        int txStart = MapProjection.pixelToTileIndex(offsetX);
        int tyStart = MapProjection.pixelToTileIndex(offsetY);
        int txEnd   = MapProjection.pixelToTileIndex(offsetX + MAP_W);
        int tyEnd   = MapProjection.pixelToTileIndex(offsetY + MAP_H);

        for (int tx = txStart; tx <= txEnd; tx++) {
            for (int ty = tyStart; ty <= tyEnd; ty++) {
                final double drawX = tx * MapProjection.TILE_SIZE - offsetX;
                final double drawY = ty * MapProjection.TILE_SIZE - offsetY;
                final String url = "https://tile.openstreetmap.org/" + zoom + "/" + tx + "/" + ty + ".png";

                Thread t = new Thread(() -> {
                    try {
                        HttpURLConnection conn = (HttpURLConnection) new URI(url).toURL().openConnection();
                        conn.setRequestProperty("User-Agent", "MedMap/1.0 (student project; JavaFX 21)");
                        conn.setConnectTimeout(5000);
                        conn.setReadTimeout(5000);
                        conn.connect();

                        try (InputStream is = conn.getInputStream()) {
                            Image tile = new Image(is);
                            Platform.runLater(() -> {
                                gc.drawImage(tile, drawX, drawY, MapProjection.TILE_SIZE, MapProjection.TILE_SIZE);
                                drawOverlay();
                            });
                        }
                    } catch (Exception ignored) { }
                });
                t.setDaemon(true);
                t.start();
            }
        }
    }

    private void drawOverlay() {
        GraphicsContext gc = overlayCanvas.getGraphicsContext2D();
        gc.clearRect(0, 0, MAP_W, MAP_H);

        gc.save();
        gc.beginPath();
        gc.rect(0, 0, MAP_W, MAP_H);
        gc.clip();

        DelaunayVoronoi dv = buildDelaunay();

        drawStudyZone(gc);

        drawVoronoiRegions(gc, dv);
        drawDelaunayEdges(gc, dv);

        drawPatientLinks(gc);
        drawPatients(gc);

        drawMarkers(gc);

        drawDeadZoneIndicator(gc);
        drawInfoPanel(gc);

        gc.restore();
    }
    
    private void generateRandomPatients(int count) {
	    for (int i = 0; i < count; i++) {
	        // Center-weighted random distribution:
	        // more patients near the center of the analysis area,
	        // fewer patients near the edges.
	        double centeredLatRatio = (Math.random() + Math.random()) / 2.0;
	        double centeredLngRatio = (Math.random() + Math.random()) / 2.0;
	
	        double lat = IDF_MIN_LAT + centeredLatRatio * (IDF_MAX_LAT - IDF_MIN_LAT);
	        double lng = IDF_MIN_LNG + centeredLngRatio * (IDF_MAX_LNG - IDF_MIN_LNG);
	
	        Patient patient = new Patient("P" + (patients.size() + 1), lat, lng);
	        patients.add(patient);
	    }	

    assignPatientsToNearestDoctors();
    computePatientScreenCoords();

    ConsoleLogger.log("DATA", count + " patients added with center-weighted distribution.");
    drawOverlay();
	}
    
    private void assignPatientsToNearestDoctors() {
        for (Patient patient : patients) {
            Doctor nearestDoctor = findNearestDoctor(patient);
            patient.setNearestDoctor(nearestDoctor);
        }
    }

    private Doctor findNearestDoctor(Patient patient) {
        Doctor nearestDoctor = null;
        double bestDistance = Double.MAX_VALUE;

        for (Doctor doctor : doctors) {
            double distance = calculateGpsDistanceInKm(
                    patient.getLat(),
                    patient.getLng(),
                    doctor.getLat(),
                    doctor.getLng()
            );

            if (distance < bestDistance) {
                bestDistance = distance;
                nearestDoctor = doctor;
            }
        }

        return nearestDoctor;
    }

    private double calculateGpsDistanceInKm(double lat1, double lng1, double lat2, double lng2) {
        final double earthRadiusKm = 6371.0;

        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1))
                * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLng / 2)
                * Math.sin(dLng / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return earthRadiusKm * c;
    }

    private DelaunayVoronoi buildDelaunay() {
        double minX = screenCoords.stream().mapToDouble(c -> c[0]).min().orElse(0);
        double minY = screenCoords.stream().mapToDouble(c -> c[1]).min().orElse(0);
        double maxX = screenCoords.stream().mapToDouble(c -> c[0]).max().orElse(MAP_W);
        double maxY = screenCoords.stream().mapToDouble(c -> c[1]).max().orElse(MAP_H);

        double margin = Math.max(maxX - minX, maxY - minY) * 0.6 + 300;

        DelaunayVoronoi dv = new DelaunayVoronoi();
        dv.setBoundingBox(minX - margin, minY - margin, maxX + margin, maxY + margin);

        for (int i = 0; i < doctors.size(); i++) {
            double[] sc = screenCoords.get(i);
            dv.insertPoint(new Point(sc[0], sc[1], doctors.get(i).getName()));
        }
        return dv;
    }

    private void drawVoronoiRegions(GraphicsContext gc, DelaunayVoronoi dv) {
        List<Point[]> regions = dv.computeVoronoi();

        maxVertexDistanceKm = 0.0;
        furthestVertex = null;

        if (selectedDoctor != null) {
            int docIndex = doctors.indexOf(selectedDoctor);
            selectedDocX = screenCoords.get(docIndex)[0];
            selectedDocY = screenCoords.get(docIndex)[1];
        }

        for (int i = 0; i < regions.size(); i++) {
            Point[] poly = regions.get(i);
            if (poly.length < 3 || hasInvalidCoords(poly)) continue;

            if (selectedDoctor != null && isPointInPolygon(poly, selectedDocX, selectedDocY)) {
                double maxDist = -1;
                Point localFurthest = null;

                // Calcul des limites de l'IDF à l'écran
                double zoneX1 = MapProjection.lngToPixelX(IDF_MIN_LNG, zoom) - offsetX;
                double zoneX2 = MapProjection.lngToPixelX(IDF_MAX_LNG, zoom) - offsetX;
                double zoneY1 = MapProjection.latToPixelY(IDF_MAX_LAT, zoom) - offsetY;
                double zoneY2 = MapProjection.latToPixelY(IDF_MIN_LAT, zoom) - offsetY;

                for (Point p : poly) {
                    // Contrainte stricte aux frontières de l'Île-de-France
                    double clampedX = Math.clamp(p.getX(), zoneX1, zoneX2);
                    double clampedY = Math.clamp(p.getY(), zoneY1, zoneY2);

                    // Sécurité pour la fenêtre
                    clampedX = Math.clamp(clampedX, 0, MAP_W);
                    clampedY = Math.clamp(clampedY, 0, MAP_H);

                    double dist = calculateDistanceInKm(selectedDocX, selectedDocY, clampedX, clampedY, selectedDoctor.getLat(), zoom);
                    if (dist > maxDist) {
                        maxDist = dist;
                        localFurthest = new Point(clampedX, clampedY, "Cible");
                    }
                }

                if (maxVertexDistanceKm != maxDist) {
                    maxVertexDistanceKm = maxDist;
                    furthestVertex = localFurthest;

                    ConsoleLogger.log("ANALYSE", "Distance critique calculée pour " + selectedDoctor.getName() + " : " + String.format("%.2f", maxVertexDistanceKm) + " km.");
                    if (maxVertexDistanceKm > SEUIL_VIDE_ABSOLU_KM) {
                        ConsoleLogger.log("ALERTE", "!!! VIDE ABSOLU DÉTECTÉ DANS CETTE ZONE !!!");
                    }
                }
            }

            double[] xs = Arrays.stream(poly).mapToDouble(Point::getX).toArray();
            double[] ys = Arrays.stream(poly).mapToDouble(Point::getY).toArray();

            Color c = palette.get(i % palette.size());

            gc.setFill(c.deriveColor(0, 1, 1, 0.20));
            gc.fillPolygon(xs, ys, poly.length);

            gc.setStroke(c.deriveColor(0, 1, 0.7, 0.80));
            gc.setLineWidth(1.8);
            gc.strokePolygon(xs, ys, poly.length);
        }
    }

    private void drawDelaunayEdges(GraphicsContext gc, DelaunayVoronoi dv) {
        // Changement ici : on passe en Noir (0,0,0) avec une forte opacité (0.8)
        gc.setStroke(Color.rgb(0, 0, 0, 0.8));
        gc.setLineWidth(1.5); // Trait plus épais
        gc.setLineDashes(5, 4);

        for (Point[] edge : dv.computeEdges()) {
            gc.strokeLine(edge[0].getX(), edge[0].getY(),
                    edge[1].getX(), edge[1].getY());
        }
        gc.setLineDashes();
    }
    
    private void drawPatientLinks(GraphicsContext gc) {
        gc.setStroke(Color.rgb(40, 40, 40, 0.25));
        gc.setLineWidth(0.8);

        for (int i = 0; i < patients.size(); i++) {
            Patient patient = patients.get(i);
            Doctor nearestDoctor = patient.getNearestDoctor();

            if (nearestDoctor == null) continue;

            int doctorIndex = doctors.indexOf(nearestDoctor);
            if (doctorIndex < 0 || doctorIndex >= screenCoords.size()) continue;
            if (i >= patientScreenCoords.size()) continue;

            double patientX = patientScreenCoords.get(i)[0];
            double patientY = patientScreenCoords.get(i)[1];

            double doctorX = screenCoords.get(doctorIndex)[0];
            double doctorY = screenCoords.get(doctorIndex)[1];

            gc.strokeLine(patientX, patientY, doctorX, doctorY);
        }
    }
    
    private void drawPatients(GraphicsContext gc) {
        for (int i = 0; i < patients.size(); i++) {
            if (i >= patientScreenCoords.size()) continue;

            double x = patientScreenCoords.get(i)[0];
            double y = patientScreenCoords.get(i)[1];

            gc.setFill(Color.rgb(0, 0, 0, 0.75));
            gc.fillOval(x - 3, y - 3, 6, 6);

            gc.setStroke(Color.WHITE);
            gc.setLineWidth(1);
            gc.strokeOval(x - 3, y - 3, 6, 6);
        }
    }

    private void drawMarkers(GraphicsContext gc) {
        gc.setFont(Font.font("System", FontWeight.BOLD, 11));

        for (int i = 0; i < doctors.size(); i++) {
            double sx   = screenCoords.get(i)[0];
            double sy   = screenCoords.get(i)[1];
            String name = doctors.get(i).getName();

            boolean isSelected = (selectedDoctor != null && selectedDoctor.getName().equals(name));

            gc.setFill(Color.rgb(0, 0, 0, 0.18));
            gc.fillOval(sx - 6, sy - 5, 14, 14);

            gc.setFill(isSelected ? Color.web("#f1c40f") : Color.web("#e74c3c"));
            gc.fillOval(sx - 7, sy - 7, 14, 14);

            gc.setStroke(Color.WHITE);
            gc.setLineWidth(isSelected ? 3 : 2);
            gc.strokeOval(sx - 7, sy - 7, 14, 14);

            double labelW = name.length() * 6.3 + 12;
            gc.setFill(Color.rgb(255, 255, 255, isSelected ? 0.95 : 0.88));
            gc.fillRoundRect(sx + 9, sy - 10, labelW, 17, 6, 6);

            gc.setFill(Color.web("#2c3e50"));
            gc.fillText(name, sx + 14, sy + 3);
        }
    }

    // Dessine un viseur sur le point le plus éloigné
    private void drawDeadZoneIndicator(GraphicsContext gc) {
        if (selectedDoctor == null || furthestVertex == null || maxVertexDistanceKm == 0) return;

        double vx = furthestVertex.getX();
        double vy = furthestVertex.getY();

        // Calcul du rayon du cercle en pixels
        double dx = vx - selectedDocX;
        double dy = vy - selectedDocY;
        double radiusPx = Math.sqrt(dx * dx + dy * dy);

        // Dessin du cercle circonscrit mathématique
        gc.setStroke(Color.web("#c0392b", 0.4)); // Rouge transparent
        gc.setLineWidth(2);
        gc.strokeOval(vx - radiusPx, vy - radiusPx, radiusPx * 2, radiusPx * 2);

        // Remplissage léger du cercle pour bien montrer le "désert"
        gc.setFill(Color.web("#e74c3c", 0.1));
        gc.fillOval(vx - radiusPx, vy - radiusPx, radiusPx * 2, radiusPx * 2);

        // 3. Le rayon en pointillé
        gc.setStroke(Color.web("#c0392b"));
        gc.setLineWidth(2);
        gc.setLineDashes(6, 6);
        gc.strokeLine(selectedDocX, selectedDocY, vx, vy);
        gc.setLineDashes();

        // 4. La cible sur le sommet
        gc.setFill(Color.web("#e74c3c", 0.5));
        gc.fillOval(vx - 15, vy - 15, 30, 30);

        gc.setFill(Color.web("#c0392b"));
        gc.fillOval(vx - 5, vy - 5, 10, 10);
    }

    private void drawInfoPanel(GraphicsContext gc) {
        if (selectedDoctor == null) return;

        double panelX = 20;
        double panelY = 20;
        double panelW = 400;
        double panelH = 150;

        gc.setFill(Color.rgb(0, 0, 0, 0.2));
        gc.fillRoundRect(panelX + 2, panelY + 2, panelW, panelH, 10, 10);
        gc.setFill(Color.rgb(255, 255, 255, 0.95));
        gc.fillRoundRect(panelX, panelY, panelW, panelH, 10, 10);
        gc.setStroke(Color.web("#34495e"));
        gc.setLineWidth(2);
        gc.strokeRoundRect(panelX, panelY, panelW, panelH, 10, 10);

        gc.setFill(Color.web("#2c3e50"));
        gc.setFont(Font.font("System", FontWeight.BOLD, 15));
        gc.fillText(selectedDoctor.getName(), panelX + 15, panelY + 25);

        gc.setStroke(Color.web("#bdc3c7"));
        gc.setLineWidth(1);
        gc.strokeLine(panelX + 10, panelY + 35, panelX + panelW - 10, panelY + 35);

        gc.setFont(Font.font("System", FontWeight.NORMAL, 12));
        gc.fillText("Latitude   : " + String.format("%.4f", selectedDoctor.getLat()), panelX + 15, panelY + 55);
        gc.fillText("Longitude  : " + String.format("%.4f", selectedDoctor.getLng()), panelX + 15, panelY + 75);

        if (maxVertexDistanceKm > 0) {
            gc.setFill(Color.web("#2c3e50"));
            gc.fillText(String.format("Distance au point le plus critique : %.2f km", maxVertexDistanceKm), panelX + 15, panelY + 100);

            if (maxVertexDistanceKm > SEUIL_VIDE_ABSOLU_KM) {
                gc.setFill(Color.web("#c0392b"));
                gc.setFont(Font.font("System", FontWeight.BOLD, 12));
                gc.fillText("▶ ALERTE : Le point ciblé est un Vide Absolu (Dead Zone)", panelX + 15, panelY + 125);
            } else {
                gc.setFill(Color.web("#27ae60"));
                gc.setFont(Font.font("System", FontWeight.BOLD, 12));
                gc.fillText("▶ Couverture maîtrisée (Aucun vide absolu détecté)", panelX + 15, panelY + 125);
            }
        }
    }

    // Dessine la zone limite d'analyse et grise l'extérieur de l'Île-de-France
    private void drawStudyZone(GraphicsContext gc) {
        double x1 = MapProjection.lngToPixelX(IDF_MIN_LNG, zoom) - offsetX;
        double x2 = MapProjection.lngToPixelX(IDF_MAX_LNG, zoom) - offsetX;
        double y1 = MapProjection.latToPixelY(IDF_MAX_LAT, zoom) - offsetY;
        double y2 = MapProjection.latToPixelY(IDF_MIN_LAT, zoom) - offsetY;

        double w = x2 - x1;
        double h = y2 - y1;

        // Ombrage
        gc.setFill(Color.rgb(0, 0, 0, 0.35));
        gc.fillRect(0, 0, MAP_W, y1);
        gc.fillRect(0, y2, MAP_W, MAP_H - y2);
        gc.fillRect(0, y1, x1, h);
        gc.fillRect(x2, y1, MAP_W - x2, h);

        // Frontière
        gc.setStroke(Color.web("#e67e22"));
        gc.setLineWidth(3);
        gc.setLineDashes(10, 10);
        gc.strokeRect(x1, y1, w, h);
        gc.setLineDashes();

        gc.setFill(Color.web("#e67e22"));
        gc.setFont(Font.font("System", FontWeight.BOLD, 14));
        gc.fillText("Limite territoriale d'analyse (Île-de-France)", x1 + 10, y1 + 20);
    }

    private boolean isPointInPolygon(Point[] poly, double px, double py) {
        boolean inside = false;
        for (int i = 0, j = poly.length - 1; i < poly.length; j = i++) {
            if ((poly[i].getY() > py) != (poly[j].getY() > py) &&
                    (px < (poly[j].getX() - poly[i].getX()) * (py - poly[i].getY()) / (poly[j].getY() - poly[i].getY()) + poly[i].getX())) {
                inside = !inside;
            }
        }
        return inside;
    }

    private double calculateDistanceInKm(double px1, double py1, double px2, double py2, double lat, int currentZoom) {
        double dx = px2 - px1;
        double dy = py2 - py1;
        double pixelDist = Math.sqrt(dx * dx + dy * dy);

        double metersPerPixel = 156543.034 * Math.cos(Math.toRadians(lat)) / Math.pow(2, currentZoom);
        return (pixelDist * metersPerPixel) / 1000.0;
    }

    private boolean hasInvalidCoords(Point[] poly) {
        for (Point p : poly) {
            if (Double.isNaN(p.getX()) || Double.isNaN(p.getY())
                    || Double.isInfinite(p.getX()) || Double.isInfinite(p.getY())) {
                return true;
            }
        }
        return false;
    }

    private List<Color> buildPalette(int n) {
        String[] hexPalette = {
                "#4285F4", "#EA4335", "#FBBC05", "#34A853", "#9C27B0",
                "#00BCD4", "#FF9800", "#E91E63", "#3F51B5", "#8BC34A",
                "#795548", "#607D8B", "#009688", "#FFEB3B", "#673AB7"
        };
        List<Color> colors = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            colors.add(Color.web(hexPalette[i % hexPalette.length]));
        }
        return colors;
    }
}

package com.example.medmap.map;

import com.example.medmap.algo.DelaunayVoronoi;
import com.example.medmap.model.Doctor;
import com.example.medmap.model.DoctorData;
import com.example.medmap.model.Point;
import javafx.application.Platform;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MapView extends Pane {
    // constantes
    private int zoom = 13;
    private static final double MAP_W = 900;
    private static final double MAP_H = 700;

    // canvas
    private final Canvas tileCanvas    = new Canvas(MAP_W, MAP_H);
    private final Canvas overlayCanvas = new Canvas(MAP_W, MAP_H);

    // données
    private final List<Doctor>   doctors;
    private final List<double[]> screenCoords = new ArrayList<>();
    private final List<Color>    palette;

    // décalage
    private double offsetX;
    private double offsetY;

    // constructeur
    public MapView() {
        doctors = DoctorData.getCergyDoctors();
        palette = buildPalette(doctors.size());

        setPrefSize(MAP_W, MAP_H);
        getChildren().addAll(tileCanvas, overlayCanvas);

        setupZoomHandler();

        computeOffset();
        computeScreenCoords();
        loadTiles();
        drawOverlay();
    }

    // active le zoom / dézoom avec la molette de la souris
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

    // change le niveau de zoom en gardant le point sous la souris au même endroit
    private void changeZoomAtMouse(int delta, double mouseX, double mouseY) {
        int oldZoom = zoom;
        int newZoom = zoom + delta;

        // limites raisonnables pour éviter de trop charger les tuiles
        if (newZoom < 11 || newZoom > 16) {
            return;
        }

        // coordonnées monde du point sous la souris avant zoom
        double worldXBeforeZoom = offsetX + mouseX;
        double worldYBeforeZoom = offsetY + mouseY;

        // facteur entre deux niveaux de zoom OpenStreetMap
        double scale = Math.pow(2, newZoom - oldZoom);

        // coordonnées monde du même point après zoom
        double worldXAfterZoom = worldXBeforeZoom * scale;
        double worldYAfterZoom = worldYBeforeZoom * scale;

        zoom = newZoom;

        // nouvel offset pour garder le même point sous la souris
        offsetX = worldXAfterZoom - mouseX;
        offsetY = worldYAfterZoom - mouseY;

        // recalcul des positions écran
        screenCoords.clear();
        computeScreenCoords();

        // recharge et redessine
        loadTiles();
        drawOverlay();
    }

    // centre la carte sur le barycentre des médecins
    private void computeOffset() {
        double avgLat = doctors.stream().mapToDouble(Doctor::getLat).average().orElse(49.0369);
        double avgLng = doctors.stream().mapToDouble(Doctor::getLng).average().orElse(2.0778);

        offsetX = MapProjection.lngToPixelX(avgLng, zoom) - MAP_W / 2.0;
        offsetY = MapProjection.latToPixelY(avgLat, zoom) - MAP_H / 2.0;
    }

    // convertit les coordonnées GPS de chaque médecin en pixels écran
    private void computeScreenCoords() {
        for (Doctor d : doctors) {
            double sx = MapProjection.lngToPixelX(d.getLng(), zoom) - offsetX;
            double sy = MapProjection.latToPixelY(d.getLat(), zoom) - offsetY;
            screenCoords.add(new double[]{sx, sy});
        }
    }

    // chargement des tuiles OSM
    private void loadTiles() {
        GraphicsContext gc = tileCanvas.getGraphicsContext2D();

        // fond neutre en attendant les tuiles
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

                // thread dédié : HttpURLConnection avec User-Agent valide exigé par OSM
                Thread t = new Thread(() -> {
                    try {
                        HttpURLConnection conn = (HttpURLConnection) new URI(url).toURL().openConnection();
                        conn.setRequestProperty("User-Agent", "MedMap/1.0 (student project; JavaFX 21)");
                        conn.setConnectTimeout(5000);
                        conn.setReadTimeout(5000);
                        conn.connect();

                        try (InputStream is = conn.getInputStream()) {
                            Image tile = new Image(is);

                            // retour sur le thread JavaFX pour dessiner
                            Platform.runLater(() -> {
                                gc.drawImage(tile, drawX, drawY,
                                        MapProjection.TILE_SIZE, MapProjection.TILE_SIZE);
                                drawOverlay();
                            });
                        }
                    } catch (Exception e) {
                        // tuile inaccessible : on ignore pour garder l'application stable
                    }
                });
                t.setDaemon(true);
                t.start();
            }
        }
    }

    // dessine l'overlay
    private void drawOverlay() {
        GraphicsContext gc = overlayCanvas.getGraphicsContext2D();
        gc.clearRect(0, 0, MAP_W, MAP_H);

        // clip : on ne dessine rien hors du canvas
        gc.save();
        gc.beginPath();
        gc.rect(0, 0, MAP_W, MAP_H);
        gc.clip();

        DelaunayVoronoi dv = buildDelaunay();

        drawVoronoiRegions(gc, dv);
        drawDelaunayEdges(gc, dv);
        drawMarkers(gc);

        gc.restore();
    }

    // construit la triangulation de Delaunay
    private DelaunayVoronoi buildDelaunay() {
        double minX = screenCoords.stream().mapToDouble(c -> c[0]).min().orElse(0);
        double minY = screenCoords.stream().mapToDouble(c -> c[1]).min().orElse(0);
        double maxX = screenCoords.stream().mapToDouble(c -> c[0]).max().orElse(MAP_W);
        double maxY = screenCoords.stream().mapToDouble(c -> c[1]).max().orElse(MAP_H);

        // marge généreuse pour que la bounding box englobe bien tous les points
        double margin = Math.max(maxX - minX, maxY - minY) * 0.6 + 300;

        DelaunayVoronoi dv = new DelaunayVoronoi();
        dv.setBoundingBox(minX - margin, minY - margin, maxX + margin, maxY + margin);

        for (int i = 0; i < doctors.size(); i++) {
            double[] sc = screenCoords.get(i);
            dv.insertPoint(new Point(sc[0], sc[1], doctors.get(i).getName()));
        }
        return dv;
    }

    // remplit et contourne les polygones de Voronoï
    private void drawVoronoiRegions(GraphicsContext gc, DelaunayVoronoi dv) {
        List<Point[]> regions = dv.computeVoronoi();

        for (int i = 0; i < regions.size(); i++) {
            Point[] poly = regions.get(i);
            if (poly.length < 3 || hasInvalidCoords(poly)) continue;

            double[] xs = Arrays.stream(poly).mapToDouble(Point::getX).toArray();
            double[] ys = Arrays.stream(poly).mapToDouble(Point::getY).toArray();

            Color c = palette.get(i % palette.size());

            // remplissage semi-transparent
            gc.setFill(c.deriveColor(0, 1, 1, 0.20));
            gc.fillPolygon(xs, ys, poly.length);

            // contour de la cellule
            gc.setStroke(c.deriveColor(0, 1, 0.7, 0.80));
            gc.setLineWidth(1.8);
            gc.strokePolygon(xs, ys, poly.length);
        }
    }

    // dessine les arêtes de la triangulation de Delaunay
    private void drawDelaunayEdges(GraphicsContext gc, DelaunayVoronoi dv) {
        gc.setStroke(Color.rgb(50, 50, 180, 0.22));
        gc.setLineWidth(0.9);
        gc.setLineDashes(5, 4);

        for (Point[] edge : dv.computeEdges()) {
            gc.strokeLine(edge[0].getX(), edge[0].getY(),
                    edge[1].getX(), edge[1].getY());
        }

        gc.setLineDashes(); // reset tirets
    }

    // dessine les marqueurs des médecins
    private void drawMarkers(GraphicsContext gc) {
        gc.setFont(Font.font("System", FontWeight.BOLD, 11));

        for (int i = 0; i < doctors.size(); i++) {
            double sx   = screenCoords.get(i)[0];
            double sy   = screenCoords.get(i)[1];
            String name = doctors.get(i).getName();

            gc.setFill(Color.rgb(0, 0, 0, 0.18));
            gc.fillOval(sx - 6, sy - 5, 14, 14);

            gc.setFill(Color.web("#e74c3c"));
            gc.fillOval(sx - 7, sy - 7, 14, 14);

            gc.setStroke(Color.WHITE);
            gc.setLineWidth(2);
            gc.strokeOval(sx - 7, sy - 7, 14, 14);

            double labelW = name.length() * 6.3 + 12;
            gc.setFill(Color.rgb(255, 255, 255, 0.88));
            gc.fillRoundRect(sx + 9, sy - 10, labelW, 17, 6, 6);

            gc.setFill(Color.web("#2c3e50"));
            gc.fillText(name, sx + 14, sy + 3);
        }
    }

    // vérifie qu'aucun sommet du polygone n'a de coordonnées invalides
    private boolean hasInvalidCoords(Point[] poly) {
        for (Point p : poly) {
            if (Double.isNaN(p.getX()) || Double.isNaN(p.getY())
                    || Double.isInfinite(p.getX()) || Double.isInfinite(p.getY())) {
                return true;
            }
        }
        return false;
    }

    // génère les couleurs
    private List<Color> buildPalette(int n) {
        List<Color> colors = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            colors.add(Color.hsb(360.0 * i / n, 0.55, 0.88));
        }
        return colors;
    }
}

package com.example.medmap;

import com.example.medmap.algo.DelaunayVoronoi;
import com.example.medmap.model.Point;
import java.util.List;
import java.util.Locale;

public class TestDelaunay {
    public static void main(String[] args) {
        System.out.println("=== DÉBUT DU TEST MEDMAP ===");

        DelaunayVoronoi delaunayVoronoi = new DelaunayVoronoi();

        delaunayVoronoi.setBoundingBox(0, 0, 800, 600);

        System.out.println("\nInsertion des points...");
        delaunayVoronoi.insertPoint(new Point(100, 150, "Cergy"));
        delaunayVoronoi.insertPoint(new Point(400, 100, "Pontoise"));
        delaunayVoronoi.insertPoint(new Point(200, 450, "Eragny"));
        delaunayVoronoi.insertPoint(new Point(600, 500, "Vauréal"));
        delaunayVoronoi.insertPoint(new Point(350, 300, "Nouveau Cabinet Médical"));

        System.out.println("\n--- TEST DELAUNAY (Arêtes) ---");
        List<Point[]> edges = delaunayVoronoi.computeEdges();
        System.out.println("Nombre d'arêtes générées : " + edges.size());
        for (Point[] edge : edges) {
            System.out.println("Ligne entre : " + edge[0].getName() + " et " + edge[1].getName());
        }

        System.out.println("\n--- TEST DELAUNAY (Triangles) ---");
        List<Point[]> triangles = delaunayVoronoi.computeTriangles();
        System.out.println("Nombre de triangles générés : " + triangles.size());
        for (int i = 0; i < triangles.size(); i++) {
            Point[] t = triangles.get(i);
            System.out.println("Triangle " + (i + 1) + " : " + t[0].getName() + " -> " + t[1].getName() + " -> " + t[2].getName());
        }

        System.out.println("\n--- TEST VORONOI (Cellules) ---");
        List<Point[]> voronoiRegions = delaunayVoronoi.computeVoronoi();
        System.out.println("Nombre de régions de Voronoi générées : " + voronoiRegions.size());
        for (int i = 0; i < voronoiRegions.size(); i++) {
            Point[] polygone = voronoiRegions.get(i);
            System.out.print("Région " + (i + 1) + " possède " + polygone.length + " sommets : ");
            for (Point p : polygone) {
                System.out.print("[" + String.format(Locale.US, "%.2f", p.getX()) + ", " + String.format(Locale.US, "%.2f", p.getY()) + "] ");
            }
            System.out.println();
        }

        System.out.println("\n=== FIN DU TEST : TOUT FONCTIONNE ! ===");
    }
}
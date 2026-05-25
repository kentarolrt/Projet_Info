package com.example.medmap.model;

public class Point {
    // Retrait de final pour permettre la mise à jour de la Bounding Box
    private double x;
    private double y;
    private final String name;

    // ... tes constructeurs restent identiques ...

    public Point(double x, double y, String name) {
        this.x = x;
        this.y = y;
        this.name = name;
    }

    // Ajoute ces deux setters "package-private" (pas de mot-clé public ni private)
    public void setX(double x) { this.x = x; }
    public void setY(double y) { this.y = y; }

    public double getX() { return x; }
    public double getY() { return y; }
    public String getName() { return name; }
}
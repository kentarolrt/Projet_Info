package com.example.medmap.model;

public class Point {
    private double x;
    private double y;
    private final String name;

    // constructeur
    public Point(double x, double y, String name) {
        this.x = x;
        this.y = y;
        this.name = name;
    }

    public void setX(double x) { this.x = x; }
    public void setY(double y) { this.y = y; }

    public double getX() { return x; }
    public double getY() { return y; }
    public String getName() { return name; }
}
package com.example.medmap.model;

public class Doctor {
    private final String name;
    private double lat;
    private double lng;

    // constructeur
    public Doctor(String name, double lat, double lng) {
        this.name = name;
        this.lat  = lat;
        this.lng  = lng;
    }

    public String getName() { return name; }
    public double getLat()  { return lat; }
    public double getLng()  { return lng; }
    public void setLat(double lat) { this.lat = lat; }
    public void setLng(double lng) { this.lng = lng; }
}
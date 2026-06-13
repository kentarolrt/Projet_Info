package com.example.medmap.model;

public class Patient {

    private final String id;
    private final double lat;
    private final double lng;
    private Doctor nearestDoctor;

    public Patient(String id, double lat, double lng) {
        this.id = id;
        this.lat = lat;
        this.lng = lng;
        this.nearestDoctor = null;
    }

    public String getId() {
        return id;
    }

    public double getLat() {
        return lat;
    }

    public double getLng() {
        return lng;
    }

    public Doctor getNearestDoctor() {
        return nearestDoctor;
    }

    public void setNearestDoctor(Doctor nearestDoctor) {
        this.nearestDoctor = nearestDoctor;
    }

    @Override
    public String toString() {
        return "Patient{" +
                "id='" + id + '\'' +
                ", lat=" + lat +
                ", lng=" + lng +
                ", nearestDoctor=" + (nearestDoctor != null ? nearestDoctor.getName() : "none") +
                '}';
    }
}

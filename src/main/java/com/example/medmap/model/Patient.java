package com.example.medmap.model;

/**
 * Represents a user point on the map.
 * In MedMap, a patient is linked to the nearest doctor.
 */
public class Patient {

    private String id;
    private double lat;
    private double lng;
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

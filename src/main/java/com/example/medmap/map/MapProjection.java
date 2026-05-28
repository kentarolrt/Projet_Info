package com.example.medmap.map;

public class MapProjection {
    public static final int TILE_SIZE = 256;

    // longitude
    public static double lngToPixelX(double lng, int zoom) {
        double worldPx = TILE_SIZE * (1 << zoom);   // 2^zoom * 256
        return (lng + 180.0) / 360.0 * worldPx;
    }

    // latitude
    public static double latToPixelY(double lat, int zoom) {
        double worldPx  = TILE_SIZE * (1 << zoom);
        double latRad   = Math.toRadians(lat);
        double mercator = Math.log(Math.tan(latRad) + 1.0 / Math.cos(latRad));
        return (1.0 - mercator / Math.PI) / 2.0 * worldPx;
    }

    // coordonnée absolue
    public static int pixelToTileIndex(double pixel) {
        return (int) Math.floor(pixel / TILE_SIZE);
    }
}
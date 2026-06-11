package com.example.medmap.utils;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class ConsoleLogger {
    // Formateur pour la console, défini une seule fois
    private static final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");

    public static void log(String tag, String message) {
        String time = LocalTime.now().format(timeFormatter);
        System.out.printf("[%s] [%-8s] %s%n", time, tag, message);
    }
}

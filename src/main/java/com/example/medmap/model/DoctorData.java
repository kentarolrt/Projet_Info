package com.example.medmap.model;

import com.example.medmap.utils.ConsoleLogger;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class DoctorData {
    // fichier créé à la racine
    private static final String FILE_PATH = "doctors.txt";

    // Lit le fichier texte et construit la liste des médecins
    public static List<Doctor> loadDoctors() {
        List<Doctor> doctors = new ArrayList<>();
        File file = new File(FILE_PATH);

        if (!file.exists()) {
            ConsoleLogger.log("FICHIER", "Fichier introuvable. Création de " + FILE_PATH + " avec les données par défaut.");
        }

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                // On sépare la ligne grâce au point-virgule
                String[] parts = line.split(";");
                if (parts.length == 3) {
                    String name = parts[0];
                    // On remplace les virgules éventuelles par des points pour éviter les crashs de conversion
                    double lat = Double.parseDouble(parts[1].replace(",", "."));
                    double lng = Double.parseDouble(parts[2].replace(",", "."));
                    doctors.add(new Doctor(name, lat, lng));
                }
            }
            ConsoleLogger.log("FICHIER", doctors.size() + " centres chargés depuis " + FILE_PATH);
        } catch (IOException | NumberFormatException e) {
            ConsoleLogger.log("ERREUR", "Impossible de lire le fichier : " + e.getMessage());
        }

        return doctors;
    }

    // Ajoute un médecin à la fin du texte
    public static void appendDoctor(Doctor doctor) {
        // Le paramètre "true" dans FileWriter permet d'ajouter à la fin du fichier (mode Append)
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(FILE_PATH, true))) {
            bw.write(doctor.getName() + ";" + doctor.getLat() + ";" + doctor.getLng());
            bw.newLine();
            ConsoleLogger.log("FICHIER", "Sauvegarde réussie de " + doctor.getName() + " dans " + FILE_PATH);
        } catch (IOException e) {
            ConsoleLogger.log("ERREUR", "Impossible de sauvegarder dans le fichier : " + e.getMessage());
        }
    }

    // sauvegarde les docteurs dans le fichier
    public static void saveAllDoctors(List<Doctor> doctors) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(FILE_PATH))) {
            for (Doctor d : doctors) {
                bw.write(d.getName() + ";" + d.getLat() + ";" + d.getLng());
                bw.newLine();
            }
            ConsoleLogger.log("FICHIER", "Fichier mis à jour. Il reste " + doctors.size() + " centres.");
        } catch (IOException e) {
            ConsoleLogger.log("ERREUR", "Impossible de mettre à jour le fichier : " + e.getMessage());
        }
    }


}
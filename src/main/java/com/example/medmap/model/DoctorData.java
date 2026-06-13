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
            createDefaultFile();
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

    private static void createDefaultFile() {
        List<Doctor> defaultDoctors = List.of(
                // --- VALLÉE DE L'OISE & VEXIN (Ton cœur de projet) ---
                new Doctor("Dr. Cergy-Préfecture", 49.0369, 2.0778),
                new Doctor("Dr. Cergy-Saint-Christophe", 49.0452, 2.0613),
                new Doctor("Dr. Pontoise", 49.0505, 2.1005),
                new Doctor("Dr. Osny", 49.0620, 2.0600),
                new Doctor("Dr. Vauréal", 49.0319, 2.0291),
                new Doctor("Dr. Jouy-le-Moutier", 49.0178, 2.0378),
                new Doctor("Dr. Menucourt", 49.0275, 1.9835),
                new Doctor("Dr. Courdimanche", 49.0350, 2.0010),
                new Doctor("Dr. Boisemont", 49.0285, 2.0012),
                new Doctor("Dr. Éragny-sur-Oise", 49.0150, 2.0950),
                new Doctor("Dr. Neuville-sur-Oise", 49.0145, 2.0590),
                new Doctor("Dr. Maurecourt", 48.9950, 2.0650),
                new Doctor("Dr. Conflans-Sainte-Honorine", 48.9990, 2.0945),
                new Doctor("Dr. Magny-en-Vexin", 49.1554, 1.7865),

                // --- PARIS ---
                new Doctor("Dr. Paris-Centre (Hôtel-Dieu)", 48.8534, 2.3488),
                new Doctor("Dr. Paris-Nord (Bichat)", 48.8996, 2.3323),
                new Doctor("Dr. Paris-Sud (Cochin)", 48.8384, 2.3396),
                new Doctor("Dr. Paris-Est (Tenon)", 48.8659, 2.4005),
                new Doctor("Dr. Paris-Ouest (HEGP)", 48.8396, 2.2731),

                // --- YVELINES (78) ---
                new Doctor("Dr. Versailles", 48.8014, 2.1301),
                new Doctor("Dr. Mantes-la-Jolie", 48.9907, 1.7171),
                new Doctor("Dr. Rambouillet", 48.6492, 1.8296),
                new Doctor("Dr. Saint-Germain-en-Laye", 48.8989, 2.0938),
                new Doctor("Dr. Poissy", 48.9298, 2.0436),
                new Doctor("Dr. Trappes", 48.7758, 1.9926),
                new Doctor("Dr. Plaisir", 48.8179, 1.9545),

                // --- ESSONNE (91) ---
                new Doctor("Dr. Évry", 48.6298, 2.4418),
                new Doctor("Dr. Corbeil-Essonnes", 48.6139, 2.4820),
                new Doctor("Dr. Massy", 48.7307, 2.2713),
                new Doctor("Dr. Étampes", 48.4346, 2.1619),
                new Doctor("Dr. Palaiseau", 48.7145, 2.2457),
                new Doctor("Dr. Sainte-Geneviève-des-Bois", 48.6385, 2.3304),

                // --- HAUTS-DE-SEINE (92) ---
                new Doctor("Dr. Boulogne-Billancourt", 48.8397, 2.2399),
                new Doctor("Dr. Nanterre", 48.8924, 2.2069),
                new Doctor("Dr. Courbevoie", 48.8973, 2.2522),
                new Doctor("Dr. Colombes", 48.9229, 2.2532),
                new Doctor("Dr. Antony", 48.7539, 2.2975),

                // --- SEINE-SAINT-DENIS (93) ---
                new Doctor("Dr. Montreuil", 48.8623, 2.4412),
                new Doctor("Dr. Saint-Denis", 48.9362, 2.3574),
                new Doctor("Dr. Aulnay-sous-Bois", 48.9386, 2.4905),
                new Doctor("Dr. Aubervilliers", 48.9131, 2.3831),
                new Doctor("Dr. Bobigny", 48.9086, 2.4397),

                // --- VAL-DE-MARNE (94) ---
                new Doctor("Dr. Créteil", 48.7904, 2.4556),
                new Doctor("Dr. Vitry-sur-Seine", 48.7876, 2.3928),
                new Doctor("Dr. Champigny-sur-Marne", 48.8172, 2.5153),
                new Doctor("Dr. Villejuif", 48.7925, 2.3638),

                // --- SEINE-ET-MARNE (77) ---
                new Doctor("Dr. Meaux", 48.9596, 2.8794),
                new Doctor("Dr. Melun", 48.5400, 2.6586),
                new Doctor("Dr. Chelles", 48.8824, 2.5931),
                new Doctor("Dr. Pontault-Combault", 48.7981, 2.6044),
                new Doctor("Dr. Fontainebleau", 48.4047, 2.7016),
                new Doctor("Dr. Provins", 48.5604, 3.2990),

                new Doctor("Dr. Nemours", 48.2694, 2.6970),
                new Doctor("Dr. Montereau-Fault-Yonne", 48.3831, 2.9515),
                new Doctor("Dr. Dourdan", 48.5283, 2.0125),
                new Doctor("Dr. Arpajon", 48.5898, 2.2486),
                new Doctor("Dr. Milly-la-Forêt", 48.4026, 2.4697),
                new Doctor("Dr. Brie-Comte-Robert", 48.6925, 2.6105),
                new Doctor("Dr. Saint-Arnoult-en-Yvelines", 48.5721, 1.9332),
                new Doctor("Dr. Coulommiers", 48.8150, 3.0847),
                new Doctor("Dr. Tournan-en-Brie", 48.7410, 2.7667)
        );

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(FILE_PATH))) {
            for (Doctor d : defaultDoctors) {
                bw.write(d.getName() + ";" + d.getLat() + ";" + d.getLng());
                bw.newLine();
            }
        } catch (IOException e) {
            ConsoleLogger.log("ERREUR", "Impossible de créer le fichier par défaut : " + e.getMessage());
        }
    }
}
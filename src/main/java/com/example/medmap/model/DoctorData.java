package com.example.medmap.model;

import java.util.List;

// Coordonnés GPS issues d'OpenStreetMap
public class DoctorData {

    public static List<Doctor> getCergyDoctors() {
        return List.of(
                new Doctor("Dr. Cergy-Saint-Christophe",  49.0452,  2.0613),
                new Doctor("Dr. Pontoise Centre",          49.0505,  2.1005),
                new Doctor("Dr. Les Hauts de Cergy",       49.0612,  2.0487),
                new Doctor("Dr. Vauréal",                  49.0319,  2.0291),
                new Doctor("Dr. Éragny-sur-Oise",          49.0083,  2.1040),
                new Doctor("Dr. Osny",                     49.0672,  2.0671),
                new Doctor("Dr. Cergy Village",            49.0384,  2.0753),
                new Doctor("Dr. Neuville-sur-Oise",        49.0528,  2.0881),
                new Doctor("Dr. Jouy-le-Moutier",          49.0178,  2.0378),
                new Doctor("Dr. Menucourt",                49.0231,  2.0053),
                new Doctor("Dr. Courdimanche",             49.0464,  2.0187),
                new Doctor("Dr. Cergy-Préfecture",         49.0369,  2.0778),
                new Doctor("Dr. Puiseux-Pontoise",         49.0680,  2.0856),
                new Doctor("Dr. Boisemont",                49.0782,  1.9908),
                new Doctor("Dr. Maurecourt",               49.0012,  2.0732)
        );
    }
}
package com.example.medmap.model;

import java.util.List;

public class DoctorData {
    public static List<Doctor> getCergyDoctors() {
        return List.of(
                new Doctor("Dr. Cergy-Saint-Christophe",  49.0452,  2.0613),
                new Doctor("Dr. Pontoise Centre",         49.0505,  2.1005),
                new Doctor("Dr. Les Hauts de Cergy",      49.0550,  2.0300),
                new Doctor("Dr. Vauréal",                 49.0319,  2.0291),
                new Doctor("Dr. Éragny-sur-Oise",         49.0150,  2.0950),
                new Doctor("Dr. Osny",                    49.0620,  2.0600),
                new Doctor("Dr. Cergy Village",           49.0340,  2.0650),
                new Doctor("Dr. Neuville-sur-Oise",       49.0145,  2.0590),
                new Doctor("Dr. Jouy-le-Moutier",         49.0178,  2.0378),
                new Doctor("Dr. Menucourt",               49.0275,  1.9835),
                new Doctor("Dr. Courdimanche",            49.0350,  2.0010),
                new Doctor("Dr. Cergy-Préfecture",        49.0369,  2.0778),
                new Doctor("Dr. Puiseux-Pontoise",        49.0580,  2.0150),
                new Doctor("Dr. Boisemont",               49.0285,  2.0012),
                new Doctor("Dr. Maurecourt",              48.9950,  2.0650)
        );
    }
}
package com.jobsearch;

import java.util.HashMap;
import java.util.Map;

/**
 * Mapping des régions françaises et leurs départements
 */
public class RegionMapper {
    
    private static final Map<String, String[]> REGIONS = new HashMap<>();
    
    static {
        // Régions de France métropolitaine (13 régions)
        REGIONS.put("Auvergne-Rhône-Alpes", new String[]{
            "01", "03", "07", "15", "26", "38", "42", "43", "63", "69", "73", "74"
        });
        
        REGIONS.put("Bourgogne-Franche-Comté", new String[]{
            "21", "25", "39", "58", "70", "71", "89", "90"
        });
        
        REGIONS.put("Bretagne", new String[]{
            "22", "29", "35", "56"
        });
        
        REGIONS.put("Centre-Val de Loire", new String[]{
            "18", "28", "36", "37", "41", "45"
        });
        
        REGIONS.put("Corse", new String[]{
            "2A", "2B"
        });
        
        REGIONS.put("Grand Est", new String[]{
            "08", "10", "51", "52", "54", "55", "57", "67", "68", "88"
        });
        
        REGIONS.put("Hauts-de-France", new String[]{
            "02", "59", "60", "62", "80"
        });
        
        REGIONS.put("Île-de-France", new String[]{
            "75", "77", "78", "91", "92", "93", "94", "95"
        });
        
        REGIONS.put("Normandie", new String[]{
            "14", "27", "50", "61", "76"
        });
        
        REGIONS.put("Nouvelle-Aquitaine", new String[]{
            "16", "17", "19", "23", "24", "33", "40", "47", "64", "79", "86", "87"
        });
        
        REGIONS.put("Occitanie", new String[]{
            "09", "11", "12", "30", "31", "32", "34", "46", "48", "65", "66", "81", "82"
        });
        
        REGIONS.put("Pays de la Loire", new String[]{
            "44", "49", "53", "72", "85"
        });
        
        REGIONS.put("Provence-Alpes-Côte d'Azur", new String[]{
            "04", "05", "06", "13", "83", "84"
        });
        
        // Régions d'Outre-Mer (5 régions)
        REGIONS.put("Guadeloupe", new String[]{"971"});
        REGIONS.put("Martinique", new String[]{"972"});
        REGIONS.put("Guyane", new String[]{"973"});
        REGIONS.put("La Réunion", new String[]{"974"});
        REGIONS.put("Mayotte", new String[]{"976"});
    }
    
    /**
     * Obtenir toutes les régions (métropole + DOM-TOM)
     */
    public static String[] getAllRegions() {
        return REGIONS.keySet().toArray(new String[0]);
    }
    
    /**
     * Obtenir les régions métropolitaines uniquement
     */
    public static String[] getMetropolitanRegions() {
        return new String[]{
            "Auvergne-Rhône-Alpes",
            "Bourgogne-Franche-Comté",
            "Bretagne",
            "Centre-Val de Loire",
            "Corse",
            "Grand Est",
            "Hauts-de-France",
            "Île-de-France",
            "Normandie",
            "Nouvelle-Aquitaine",
            "Occitanie",
            "Pays de la Loire",
            "Provence-Alpes-Côte d'Azur"
        };
    }
    
    /**
     * Obtenir les régions d'Outre-Mer
     */
    public static String[] getOverseasRegions() {
        return new String[]{
            "Guadeloupe",
            "Martinique",
            "Guyane",
            "La Réunion",
            "Mayotte"
        };
    }
    
    /**
     * Obtenir les départements d'une région
     */
    public static String[] getDepartmentsByRegion(String region) {
        return REGIONS.getOrDefault(region, new String[0]);
    }
    
    /**
     * Obtenir tous les départements métropolitains
     */
    public static String[] getAllMetropolitanDepartments() {
        return new String[]{
            "01", "02", "03", "04", "05", "06", "07", "08", "09", "10",
            "11", "12", "13", "14", "15", "16", "17", "18", "19", "21",
            "22", "23", "24", "25", "26", "27", "28", "29", "2A", "2B",
            "30", "31", "32", "33", "34", "35", "36", "37", "38", "39",
            "40", "41", "42", "43", "44", "45", "46", "47", "48", "49",
            "50", "51", "52", "53", "54", "55", "56", "57", "58", "59",
            "60", "61", "62", "63", "64", "65", "66", "67", "68", "69",
            "70", "71", "72", "73", "74", "75", "76", "77", "78", "79",
            "80", "81", "82", "83", "84", "85", "86", "87", "88", "89",
            "90", "91", "92", "93", "94", "95"
        };
    }
    
    /**
     * Obtenir tous les départements d'Outre-Mer
     */
    public static String[] getAllOverseasDepartments() {
        return new String[]{"971", "972", "973", "974", "976"};
    }
    
    /**
     * Obtenir tous les départements (métropole + DOM-TOM)
     */
    public static String[] getAllDepartments() {
        String[] metro = getAllMetropolitanDepartments();
        String[] overseas = getAllOverseasDepartments();
        String[] all = new String[metro.length + overseas.length];
        System.arraycopy(metro, 0, all, 0, metro.length);
        System.arraycopy(overseas, 0, all, metro.length, overseas.length);
        return all;
    }
    
    /**
     * Obtenir le nom de la région pour un département donné
     */
    public static String getRegionByDepartment(String department) {
        for (Map.Entry<String, String[]> entry : REGIONS.entrySet()) {
            for (String dept : entry.getValue()) {
                if (dept.equals(department)) {
                    return entry.getKey();
                }
            }
        }
        return "Inconnue";
    }
    
    /**
     * Vérifier si une région existe
     */
    public static boolean isValidRegion(String region) {
        return REGIONS.containsKey(region);
    }
}

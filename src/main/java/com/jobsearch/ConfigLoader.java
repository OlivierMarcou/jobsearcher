package com.jobsearch;

import java.io.*;
import java.util.Properties;

/**
 * Classe utilitaire pour charger et gérer la configuration de l'application
 * à partir du fichier config.properties dans les resources.
 */
public class ConfigLoader {
    
    private static final String CONFIG_FILE = "config.properties";
    private static ConfigLoader instance;
    private Properties properties;
    
    private ConfigLoader() {
        properties = new Properties();
        loadProperties();
    }
    
    /**
     * Obtenir l'instance singleton du ConfigLoader
     */
    public static ConfigLoader getInstance() {
        if (instance == null) {
            instance = new ConfigLoader();
        }
        return instance;
    }
    
    /**
     * Charger les propriétés depuis le fichier de configuration
     */
    private void loadProperties() {
        try (InputStream input = getClass().getClassLoader().getResourceAsStream(CONFIG_FILE)) {
            if (input == null) {
                System.err.println("Impossible de trouver " + CONFIG_FILE);
                // Créer des propriétés par défaut
                setDefaultProperties();
                return;
            }
            properties.load(input);
            System.out.println("Configuration chargée depuis " + CONFIG_FILE);
        } catch (IOException e) {
            System.err.println("Erreur lors du chargement de la configuration: " + e.getMessage());
            setDefaultProperties();
        }
    }
    
    /**
     * Définir les propriétés par défaut si le fichier n'existe pas
     */
    private void setDefaultProperties() {
        properties.setProperty("francetravail.api.base.url", "https://api.francetravail.io/partenaire");
        properties.setProperty("francetravail.token.url", 
            "https://entreprise.francetravail.fr/connexion/oauth2/access_token?realm=/partenaire");
        properties.setProperty("francetravail.scope", "api_offresdemploiv2 o2dsoffre");
        
        properties.setProperty("insee.api.base.url", "https://api.insee.fr/entreprises/sirene/V3.11");
        
        properties.setProperty("idf.departments", "75,77,78,91,92,93,94,95");
        properties.setProperty("france.departments", "01,02,03,04,05,06,07,08,09,10,11,12,13,14,15,16,17,18,19,21,22,23,24,25,26,27,28,29,2A,2B,30,31,32,33,34,35,36,37,38,39,40,41,42,43,44,45,46,47,48,49,50,51,52,53,54,55,56,57,58,59,60,61,62,63,64,65,66,67,68,69,70,71,72,73,74,75,76,77,78,79,80,81,82,83,84,85,86,87,88,89,90,91,92,93,94,95");
        properties.setProperty("dom.departments", "971,972,973,974,976");
        properties.setProperty("naf.codes.it", "62.01Z,62.02A,62.02B,62.03Z,62.09Z,63.11Z,63.12Z");
        
        properties.setProperty("api.max.results.jobs", "100");
        properties.setProperty("api.max.results.companies", "20");
        properties.setProperty("http.timeout", "10");
        
        properties.setProperty("ui.window.width", "1400");
        properties.setProperty("ui.window.height", "900");
        properties.setProperty("ui.default.keywords", "développeur java");
        
        properties.setProperty("export.default.filename.csv", "resultats_entreprises.csv");
        properties.setProperty("export.default.filename.json", "resultats_entreprises.json");
        properties.setProperty("export.csv.encoding", "UTF-8");
        properties.setProperty("export.csv.separator", ";");
    }
    
    /**
     * Obtenir une propriété String
     */
    public String getProperty(String key) {
        return properties.getProperty(key);
    }
    
    /**
     * Obtenir une propriété String avec valeur par défaut
     */
    public String getProperty(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }
    
    /**
     * Obtenir une propriété int
     */
    public int getIntProperty(String key, int defaultValue) {
        String value = properties.getProperty(key);
        if (value != null) {
            try {
                return Integer.parseInt(value);
            } catch (NumberFormatException e) {
                System.err.println("Erreur de conversion pour " + key + ": " + value);
            }
        }
        return defaultValue;
    }
    
    /**
     * Obtenir une propriété sous forme de tableau de String
     */
    public String[] getArrayProperty(String key) {
        String value = properties.getProperty(key);
        if (value != null && !value.isEmpty()) {
            return value.split(",");
        }
        return new String[0];
    }
    
    /**
     * Définir une propriété
     */
    public void setProperty(String key, String value) {
        properties.setProperty(key, value);
    }
    
    /**
     * Sauvegarder les propriétés dans un fichier externe
     * (utilisé pour sauvegarder les identifiants API)
     */
    public void saveProperties(File file) throws IOException {
        try (OutputStream output = new FileOutputStream(file)) {
            properties.store(output, "Company Job Searcher - Configuration");
        }
    }
    
    /**
     * Charger les propriétés depuis un fichier externe
     */
    public void loadPropertiesFromFile(File file) throws IOException {
        try (InputStream input = new FileInputStream(file)) {
            properties.load(input);
        }
    }
    
    // Méthodes d'accès aux propriétés spécifiques
    
    public String getFranceTravailClientId() {
        return getProperty("francetravail.client.id", "");
    }
    
    public String getFranceTravailClientSecret() {
        return getProperty("francetravail.client.secret", "");
    }
    
    public String getFranceTravailScope() {
        return getProperty("francetravail.scope");
    }
    
    public String getFranceTravailApiBaseUrl() {
        return getProperty("francetravail.api.base.url");
    }
    
    public String getFranceTravailTokenUrl() {
        return getProperty("francetravail.token.url");
    }
    
    public String getInseeApiKey() {
        return getProperty("insee.api.key", "");
    }
    
    public String getInseeApiBaseUrl() {
        return getProperty("insee.api.base.url");
    }
    
    public String[] getIdfDepartments() {
        return getArrayProperty("idf.departments");
    }
    
    public String[] getFranceDepartments() {
        return getArrayProperty("france.departments");
    }
    
    public String[] getDomDepartments() {
        return getArrayProperty("dom.departments");
    }
    
    public String[] getAllDepartments() {
        String[] france = getFranceDepartments();
        String[] dom = getDomDepartments();
        String[] all = new String[france.length + dom.length];
        System.arraycopy(france, 0, all, 0, france.length);
        System.arraycopy(dom, 0, all, france.length, dom.length);
        return all;
    }
    
    public String[] getDefaultDepartments() {
        String defaultDepts = getProperty("default.departments");
        if (defaultDepts != null && !defaultDepts.isEmpty()) {
            return defaultDepts.split(",");
        }
        return getFranceDepartments();
    }
    
    public String[] getNafCodesIT() {
        return getArrayProperty("naf.codes.it");
    }
    
    public int getMaxResultsJobs() {
        return getIntProperty("api.max.results.jobs", 100);
    }
    
    public int getMaxResultsCompanies() {
        return getIntProperty("api.max.results.companies", 20);
    }
    
    public int getHttpTimeout() {
        return getIntProperty("http.timeout", 10);
    }
    
    public int getWindowWidth() {
        return getIntProperty("ui.window.width", 1200);
    }
    
    public int getWindowHeight() {
        return getIntProperty("ui.window.height", 800);
    }
    
    public String getDefaultKeywords() {
        return getProperty("ui.default.keywords", "développeur java");
    }
    
    public String getExportDefaultFilename() {
        return getProperty("export.default.filename");
    }
    
    public String getExportEncoding() {
        return getProperty("export.csv.encoding");
    }
    
    public String getExportSeparator() {
        return getProperty("export.csv.separator");
    }
    
    /**
     * Vérifier si les identifiants France Travail sont configurés
     */
    public boolean hasFranceTravailCredentials() {
        String clientId = getFranceTravailClientId();
        String clientSecret = getFranceTravailClientSecret();
        return clientId != null && !clientId.isEmpty() && 
               clientSecret != null && !clientSecret.isEmpty();
    }
    
    /**
     * Vérifier si la clé API INSEE est configurée
     */
    public boolean hasInseeApiKey() {
        String apiKey = getInseeApiKey();
        return apiKey != null && !apiKey.isEmpty();
    }
    
    /**
     * Afficher toutes les propriétés (pour debug)
     */
    public void printAllProperties() {
        System.out.println("=== Configuration actuelle ===");
        properties.forEach((key, value) -> {
            // Masquer les secrets
            String displayValue = value.toString();
            if (key.toString().contains("secret") || key.toString().contains("key")) {
                displayValue = "***";
            }
            System.out.println(key + " = " + displayValue);
        });
        System.out.println("==============================");
    }
}

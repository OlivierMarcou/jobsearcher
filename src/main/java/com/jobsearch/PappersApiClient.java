package com.jobsearch;

import com.google.gson.*;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * Client pour l'API Pappers.fr - Données entreprises françaises enrichies
 * https://www.pappers.fr/api/documentation
 */
public class PappersApiClient {
    
    private final String apiKey;
    private final HttpClient httpClient;
    private final Gson gson;
    
    private static final String BASE_URL = "https://api.pappers.fr/v2";
    
    public PappersApiClient(String apiKey) {
        this.apiKey = apiKey;
        this.httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();
        this.gson = new Gson();
    }
    
    /**
     * Rechercher des entreprises selon critères avancés
     */
    public List<Company> searchCompanies(PappersSearchCriteria criteria) throws Exception {
        StringBuilder urlBuilder = new StringBuilder(BASE_URL);
        urlBuilder.append("/recherche?api_token=").append(apiKey);
        
        // Mots-clés (nom, activité, etc.)
        if (criteria.getQuery() != null && !criteria.getQuery().isEmpty()) {
            urlBuilder.append("&q=").append(URLEncoder.encode(criteria.getQuery(), StandardCharsets.UTF_8));
        }
        
        // Département
        if (criteria.getDepartement() != null && !criteria.getDepartement().isEmpty()) {
            urlBuilder.append("&departement=").append(criteria.getDepartement());
        }
        
        // Région
        if (criteria.getRegion() != null && !criteria.getRegion().isEmpty()) {
            urlBuilder.append("&region=").append(URLEncoder.encode(criteria.getRegion(), StandardCharsets.UTF_8));
        }
        
        // Code NAF (secteur d'activité)
        if (criteria.getCodeNAF() != null && !criteria.getCodeNAF().isEmpty()) {
            urlBuilder.append("&code_naf=").append(criteria.getCodeNAF());
        }
        
        // Chiffre d'affaires minimum
        if (criteria.getCaMin() != null) {
            urlBuilder.append("&chiffre_affaires_min=").append(criteria.getCaMin());
        }
        
        // Chiffre d'affaires maximum
        if (criteria.getCaMax() != null) {
            urlBuilder.append("&chiffre_affaires_max=").append(criteria.getCaMax());
        }
        
        // Résultat net minimum (rentabilité)
        if (criteria.getResultatMin() != null) {
            urlBuilder.append("&resultat_min=").append(criteria.getResultatMin());
        }
        
        // Date de création (année minimum)
        if (criteria.getDateCreationMin() != null) {
            urlBuilder.append("&date_creation_min=").append(criteria.getDateCreationMin());
        }
        
        // Date de création (année maximum)
        if (criteria.getDateCreationMax() != null) {
            urlBuilder.append("&date_creation_max=").append(criteria.getDateCreationMax());
        }
        
        // Effectif minimum
        if (criteria.getEffectifMin() != null) {
            urlBuilder.append("&effectif_min=").append(criteria.getEffectifMin());
        }
        
        // Effectif maximum
        if (criteria.getEffectifMax() != null) {
            urlBuilder.append("&effectif_max=").append(criteria.getEffectifMax());
        }
        
        // Entreprises en activité uniquement (exclure radiées)
        if (criteria.isExcludeRadiees()) {
            urlBuilder.append("&entreprise_cessee=false");
        }
        
        // Forme juridique (exclure auto-entrepreneurs)
        if (criteria.isExcludeAutoEntrepreneurs()) {
            // Exclure : EI (Entrepreneur individuel), EIRL
            // On cherche plutôt : SARL, SAS, SASU, SA, SNC, etc.
            urlBuilder.append("&categorie_juridique=5499,5505,5510,5515,5520,5530,5542,5543,5546,5547,5548,5551,5552,5553,5554,5558,5559,5560,5570,5585,5599,5605,5610,5615,5620,5622,5625,5630,5650,5651,5660,5670,5685,5699,5710,5720,5770,5785,5800");
            // Liste des codes pour SARL, SAS, SASU, SA, etc. (sociétés commerciales)
        }
        
        // Nombre de résultats par page
        urlBuilder.append("&par_page=").append(criteria.getPageSize());
        
        // Page
        urlBuilder.append("&page=").append(criteria.getPage());
        
        // Tri par pertinence ou CA
        if (criteria.getSortBy() != null) {
            urlBuilder.append("&tri=").append(criteria.getSortBy());
        }
        
        System.out.println("🔍 URL Pappers: " + urlBuilder.toString().replace(apiKey, "***"));
        
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(urlBuilder.toString()))
            .header("Accept", "application/json")
            .GET()
            .build();
        
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        
        System.out.println("📡 Code réponse Pappers: " + response.statusCode());
        
        if (response.statusCode() != 200) {
            throw new Exception("Erreur API Pappers: " + response.statusCode() + " - " + response.body());
        }
        
        // Parser la réponse
        JsonObject jsonResponse = gson.fromJson(response.body(), JsonObject.class);
        
        List<Company> companies = new ArrayList<>();
        
        if (jsonResponse.has("resultats")) {
            JsonArray resultats = jsonResponse.getAsJsonArray("resultats");
            System.out.println("✓ " + resultats.size() + " entreprises trouvées");
            
            for (JsonElement element : resultats) {
                JsonObject entrepriseJson = element.getAsJsonObject();
                Company company = parseCompanyFromPappers(entrepriseJson);
                companies.add(company);
            }
        }
        
        return companies;
    }
    
    /**
     * Parser une entreprise depuis la réponse Pappers
     */
    private Company parseCompanyFromPappers(JsonObject json) {
        Company company = new Company();
        company.setSource("API Pappers");
        
        // SIREN
        if (json.has("siren")) {
            company.setSiren(json.get("siren").getAsString());
        }
        
        // SIRET (siège)
        if (json.has("siege")) {
            JsonObject siege = json.getAsJsonObject("siege");
            if (siege.has("siret")) {
                company.setSiret(siege.get("siret").getAsString());
            }
        }
        
        // Nom
        if (json.has("nom_entreprise")) {
            company.setNom(json.get("nom_entreprise").getAsString());
        }
        
        // Nom commercial
        if (json.has("nom_commercial")) {
            company.setNomCommercial(json.get("nom_commercial").getAsString());
        }
        
        // Site web
        if (json.has("site_internet")) {
            company.setSiteWeb(json.get("site_internet").getAsString());
        }
        
        // Téléphone
        if (json.has("telephone")) {
            company.setTelephone(json.get("telephone").getAsString());
        }
        
        // Email
        if (json.has("email")) {
            company.setEmail(json.get("email").getAsString());
        }
        
        // Date de création
        if (json.has("date_creation")) {
            company.setDateCreation(json.get("date_creation").getAsString());
        }
        
        // Code NAF
        if (json.has("code_naf")) {
            company.setCodeNAF(json.get("code_naf").getAsString());
        }
        if (json.has("libelle_code_naf")) {
            company.setLibelleNAF(json.get("libelle_code_naf").getAsString());
        }
        
        // Forme juridique
        if (json.has("forme_juridique")) {
            company.setCategorieEntreprise(json.get("forme_juridique").getAsString());
        }
        
        // Adresse du siège
        if (json.has("siege")) {
            JsonObject siege = json.getAsJsonObject("siege");
            
            if (siege.has("adresse_ligne_1")) {
                company.setAdresse(siege.get("adresse_ligne_1").getAsString());
            }
            if (siege.has("code_postal")) {
                String codePostal = siege.get("code_postal").getAsString();
                company.setCodePostal(codePostal);
                
                // Déduire département du code postal
                if (codePostal.length() >= 2) {
                    String dept = codePostal.substring(0, 2);
                    company.setDepartement(dept);
                    company.setRegion(RegionMapper.getRegionByDepartment(dept));
                }
            }
            if (siege.has("ville")) {
                company.setVille(siege.get("ville").getAsString());
            }
        }
        
        // Finances (dernier exercice)
        if (json.has("finances")) {
            JsonArray finances = json.getAsJsonArray("finances");
            if (finances.size() > 0) {
                JsonObject dernierExercice = finances.get(0).getAsJsonObject();
                
                // Chiffre d'affaires
                if (dernierExercice.has("chiffre_affaires")) {
                    int ca = dernierExercice.get("chiffre_affaires").getAsInt();
                    company.setChiffreAffaires(formatChiffreAffaires(ca));
                }
                
                // Résultat net
                if (dernierExercice.has("resultat")) {
                    int resultat = dernierExercice.get("resultat").getAsInt();
                    if (resultat > 0) {
                        company.setChiffreAffaires(company.getChiffreAffaires() + " (bénéfice: " + formatChiffreAffaires(resultat) + ")");
                    }
                }
            }
        }
        
        // Effectif
        if (json.has("tranche_effectif_salarie")) {
            String tranche = json.get("tranche_effectif_salarie").getAsString();
            company.setTrancheEffectifWithRange(tranche);
        }
        
        // Nombre d'établissements
        if (json.has("nombre_etablissements")) {
            int nbEtab = json.get("nombre_etablissements").getAsInt();
            if (nbEtab > 1) {
                company.setSecteurActivite(company.getLibelleNAF() + " (" + nbEtab + " établissements)");
            } else {
                company.setSecteurActivite(company.getLibelleNAF());
            }
        }
        
        return company;
    }
    
    /**
     * Formater un chiffre d'affaires en texte lisible
     */
    private String formatChiffreAffaires(int ca) {
        if (ca >= 1_000_000_000) {
            return String.format("%.1f Md€", ca / 1_000_000_000.0);
        } else if (ca >= 1_000_000) {
            return String.format("%.1f M€", ca / 1_000_000.0);
        } else if (ca >= 1_000) {
            return String.format("%d k€", ca / 1_000);
        } else {
            return ca + " €";
        }
    }
    
    /**
     * Obtenir les détails d'une entreprise par SIREN
     */
    public Company getCompanyBySiren(String siren) throws Exception {
        String url = BASE_URL + "/entreprise?api_token=" + apiKey + "&siren=" + siren;
        
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .header("Accept", "application/json")
            .GET()
            .build();
        
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        
        if (response.statusCode() != 200) {
            throw new Exception("Erreur API Pappers: " + response.statusCode());
        }
        
        JsonObject json = gson.fromJson(response.body(), JsonObject.class);
        return parseCompanyFromPappers(json);
    }
}

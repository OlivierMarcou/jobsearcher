package com.jobsearch;

/**
 * Critères de recherche pour l'API Pappers
 */
public class PappersSearchCriteria {
    
    // Recherche textuelle
    private String query; // Mots-clés (nom entreprise, activité, etc.)
    
    // Localisation
    private String pays = "FR"; // France par défaut
    private String departement;
    private String region;
    private String codeNAF; // Secteur d'activité
    
    // Finances
    private Integer caMin; // Chiffre d'affaires minimum
    private Integer caMax; // Chiffre d'affaires maximum
    private Integer resultatMin; // Résultat net minimum (rentabilité)
    
    // Date de création
    private Integer dateCreationMin; // Année minimum (ex: 2020)
    private Integer dateCreationMax; // Année maximum
    
    // Effectif
    private Integer effectifMin;
    private Integer effectifMax;
    
    // Filtres booléens
    private boolean excludeRadiees = true; // Exclure entreprises radiées par défaut
    private boolean excludeAutoEntrepreneurs = true; // Exclure auto-entrepreneurs par défaut
    
    // Pagination
    private int page = 1;
    private int pageSize = 20;
    
    // Tri
    private String sortBy; // "pertinence", "chiffre_affaires", "date_creation"
    
    public PappersSearchCriteria() {
        // Constructeur par défaut avec valeurs par défaut
    }
    
    // Getters et Setters
    
    public String getQuery() { return query; }
    public void setQuery(String query) { this.query = query; }
    
    public String getPays() { return pays; }
    public void setPays(String pays) { this.pays = pays; }
    
    public String getDepartement() { return departement; }
    public void setDepartement(String departement) { this.departement = departement; }
    
    public String getRegion() { return region; }
    public void setRegion(String region) { this.region = region; }
    
    public String getCodeNAF() { return codeNAF; }
    public void setCodeNAF(String codeNAF) { this.codeNAF = codeNAF; }
    
    public Integer getCaMin() { return caMin; }
    public void setCaMin(Integer caMin) { this.caMin = caMin; }
    
    public Integer getCaMax() { return caMax; }
    public void setCaMax(Integer caMax) { this.caMax = caMax; }
    
    public Integer getResultatMin() { return resultatMin; }
    public void setResultatMin(Integer resultatMin) { this.resultatMin = resultatMin; }
    
    public Integer getDateCreationMin() { return dateCreationMin; }
    public void setDateCreationMin(Integer dateCreationMin) { this.dateCreationMin = dateCreationMin; }
    
    public Integer getDateCreationMax() { return dateCreationMax; }
    public void setDateCreationMax(Integer dateCreationMax) { this.dateCreationMax = dateCreationMax; }
    
    public Integer getEffectifMin() { return effectifMin; }
    public void setEffectifMin(Integer effectifMin) { this.effectifMin = effectifMin; }
    
    public Integer getEffectifMax() { return effectifMax; }
    public void setEffectifMax(Integer effectifMax) { this.effectifMax = effectifMax; }
    
    public boolean isExcludeRadiees() { return excludeRadiees; }
    public void setExcludeRadiees(boolean excludeRadiees) { this.excludeRadiees = excludeRadiees; }
    
    public boolean isExcludeAutoEntrepreneurs() { return excludeAutoEntrepreneurs; }
    public void setExcludeAutoEntrepreneurs(boolean excludeAutoEntrepreneurs) { 
        this.excludeAutoEntrepreneurs = excludeAutoEntrepreneurs; 
    }
    
    public int getPage() { return page; }
    public void setPage(int page) { this.page = page; }
    
    public int getPageSize() { return pageSize; }
    public void setPageSize(int pageSize) { this.pageSize = pageSize; }
    
    public String getSortBy() { return sortBy; }
    public void setSortBy(String sortBy) { this.sortBy = sortBy; }
    
    /**
     * Résumé des critères pour affichage
     */
    public String getSummary() {
        StringBuilder sb = new StringBuilder();
        
        if (query != null && !query.isEmpty()) {
            sb.append("Recherche: \"").append(query).append("\" | ");
        }
        
        if (region != null && !region.isEmpty()) {
            sb.append("Région: ").append(region).append(" | ");
        } else if (departement != null && !departement.isEmpty()) {
            sb.append("Dép: ").append(departement).append(" | ");
        }
        
        if (caMin != null || caMax != null) {
            sb.append("CA: ");
            if (caMin != null) sb.append("≥").append(formatMontant(caMin));
            if (caMin != null && caMax != null) sb.append(" - ");
            if (caMax != null) sb.append("≤").append(formatMontant(caMax));
            sb.append(" | ");
        }
        
        if (dateCreationMin != null) {
            sb.append("Créées depuis: ").append(dateCreationMin).append(" | ");
        }
        
        if (effectifMin != null || effectifMax != null) {
            sb.append("Effectif: ");
            if (effectifMin != null) sb.append("≥").append(effectifMin);
            if (effectifMin != null && effectifMax != null) sb.append(" - ");
            if (effectifMax != null) sb.append("≤").append(effectifMax);
            sb.append(" | ");
        }
        
        if (excludeRadiees) {
            sb.append("✓ En activité | ");
        }
        
        if (excludeAutoEntrepreneurs) {
            sb.append("✓ Sociétés uniquement | ");
        }
        
        String summary = sb.toString();
        if (summary.endsWith(" | ")) {
            summary = summary.substring(0, summary.length() - 3);
        }
        
        return summary.isEmpty() ? "Toutes entreprises" : summary;
    }
    
    private String formatMontant(int montant) {
        if (montant >= 1_000_000) {
            return (montant / 1_000_000) + "M€";
        } else if (montant >= 1_000) {
            return (montant / 1_000) + "k€";
        }
        return montant + "€";
    }
}

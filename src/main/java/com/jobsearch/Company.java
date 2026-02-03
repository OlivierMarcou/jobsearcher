package com.jobsearch;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Classe représentant une entreprise avec ses informations complètes
 */
public class Company {
    // Identification
    private String siret;
    private String siren;
    private String nom;
    private String nomCommercial;
    
    // Contact
    private String email;
    private String emailRH;
    private String telephone;
    private String siteWeb;
    
    // Localisation
    private String adresse;
    private String codePostal;
    private String ville;
    private String departement;
    private String region;
    
    // Activité
    private String codeNAF;
    private String libelleNAF;
    private String secteurActivite;
    
    // Taille et CA
    private String trancheEffectif;
    private Integer effectifMin;
    private Integer effectifMax;
    private String categorieEntreprise; // PME, ETI, GE
    private String chiffreAffaires;
    
    // Dates
    private String dateCreation;
    private String dateDerniereMaj;
    
    // Source
    private String source; // "API SIRENE", "Offre emploi", etc.
    
    public Company() {
        this.source = "API SIRENE";
    }
    
    // Getters et Setters
    public String getSiret() { return siret; }
    public void setSiret(String siret) { this.siret = siret; }
    
    public String getSiren() { return siren; }
    public void setSiren(String siren) { this.siren = siren; }
    
    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }
    
    public String getNomCommercial() { return nomCommercial; }
    public void setNomCommercial(String nomCommercial) { this.nomCommercial = nomCommercial; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public String getEmailRH() { return emailRH; }
    public void setEmailRH(String emailRH) { this.emailRH = emailRH; }
    
    public String getTelephone() { return telephone; }
    public void setTelephone(String telephone) { this.telephone = telephone; }
    
    public String getSiteWeb() { return siteWeb; }
    public void setSiteWeb(String siteWeb) { this.siteWeb = siteWeb; }
    
    public String getAdresse() { return adresse; }
    public void setAdresse(String adresse) { this.adresse = adresse; }
    
    public String getCodePostal() { return codePostal; }
    public void setCodePostal(String codePostal) { this.codePostal = codePostal; }
    
    public String getVille() { return ville; }
    public void setVille(String ville) { this.ville = ville; }
    
    public String getDepartement() { return departement; }
    public void setDepartement(String departement) { this.departement = departement; }
    
    public String getRegion() { return region; }
    public void setRegion(String region) { this.region = region; }
    
    public String getCodeNAF() { return codeNAF; }
    public void setCodeNAF(String codeNAF) { this.codeNAF = codeNAF; }
    
    public String getLibelleNAF() { return libelleNAF; }
    public void setLibelleNAF(String libelleNAF) { this.libelleNAF = libelleNAF; }
    
    public String getSecteurActivite() { return secteurActivite; }
    public void setSecteurActivite(String secteurActivite) { this.secteurActivite = secteurActivite; }
    
    public String getTrancheEffectif() { return trancheEffectif; }
    public void setTrancheEffectif(String trancheEffectif) { this.trancheEffectif = trancheEffectif; }
    
    public Integer getEffectifMin() { return effectifMin; }
    public void setEffectifMin(Integer effectifMin) { this.effectifMin = effectifMin; }
    
    public Integer getEffectifMax() { return effectifMax; }
    public void setEffectifMax(Integer effectifMax) { this.effectifMax = effectifMax; }
    
    public String getCategorieEntreprise() { return categorieEntreprise; }
    public void setCategorieEntreprise(String categorieEntreprise) { this.categorieEntreprise = categorieEntreprise; }
    
    public String getChiffreAffaires() { return chiffreAffaires; }
    public void setChiffreAffaires(String chiffreAffaires) { this.chiffreAffaires = chiffreAffaires; }
    
    public String getDateCreation() { return dateCreation; }
    public void setDateCreation(String dateCreation) { this.dateCreation = dateCreation; }
    
    public String getDateDerniereMaj() { return dateDerniereMaj; }
    public void setDateDerniereMaj(String dateDerniereMaj) { this.dateDerniereMaj = dateDerniereMaj; }
    
    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }
    
    /**
     * Définir la tranche d'effectif et calculer min/max
     */
    public void setTrancheEffectifWithRange(String tranche) {
        this.trancheEffectif = tranche;
        
        // Codes INSEE pour les tranches d'effectifs
        switch (tranche) {
            case "NN": // Non renseigné
                effectifMin = null;
                effectifMax = null;
                break;
            case "00": // 0 salarié
                effectifMin = 0;
                effectifMax = 0;
                break;
            case "01": // 1 ou 2 salariés
                effectifMin = 1;
                effectifMax = 2;
                break;
            case "02": // 3 à 5 salariés
                effectifMin = 3;
                effectifMax = 5;
                break;
            case "03": // 6 à 9 salariés
                effectifMin = 6;
                effectifMax = 9;
                break;
            case "11": // 10 à 19 salariés
                effectifMin = 10;
                effectifMax = 19;
                break;
            case "12": // 20 à 49 salariés
                effectifMin = 20;
                effectifMax = 49;
                break;
            case "21": // 50 à 99 salariés
                effectifMin = 50;
                effectifMax = 99;
                break;
            case "22": // 100 à 199 salariés
                effectifMin = 100;
                effectifMax = 199;
                break;
            case "31": // 200 à 249 salariés
                effectifMin = 200;
                effectifMax = 249;
                break;
            case "32": // 250 à 499 salariés
                effectifMin = 250;
                effectifMax = 499;
                break;
            case "41": // 500 à 999 salariés
                effectifMin = 500;
                effectifMax = 999;
                break;
            case "42": // 1000 à 1999 salariés
                effectifMin = 1000;
                effectifMax = 1999;
                break;
            case "51": // 2000 à 4999 salariés
                effectifMin = 2000;
                effectifMax = 4999;
                break;
            case "52": // 5000 à 9999 salariés
                effectifMin = 5000;
                effectifMax = 9999;
                break;
            case "53": // 10000 salariés et plus
                effectifMin = 10000;
                effectifMax = null;
                break;
        }
    }
    
    /**
     * Obtenir un libellé lisible de la taille
     */
    public String getTailleLibelle() {
        if (effectifMin == null) return "Non renseigné";
        if (effectifMax == null) return effectifMin + "+ salariés";
        if (effectifMin.equals(effectifMax)) return effectifMin + " salarié" + (effectifMin > 1 ? "s" : "");
        return effectifMin + "-" + effectifMax + " salariés";
    }
    
    /**
     * Clé unique pour déduplication (SIREN ou nom+ville)
     */
    public String getUniqueKey() {
        if (siren != null && !siren.isEmpty()) {
            return "SIREN_" + siren;
        } else if (nom != null && ville != null) {
            return "NAME_" + nom.toLowerCase() + "_" + ville.toLowerCase();
        }
        return "UNKNOWN_" + System.currentTimeMillis();
    }
    
    /**
     * Convertir en JSON
     */
    public String toJson() {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        return gson.toJson(this);
    }
    
    /**
     * Ligne CSV pour export
     */
    public String toCsvLine(String separator) {
        return String.join(separator,
            csvEscape(nom),
            csvEscape(nomCommercial),
            csvEscape(siren),
            csvEscape(siret),
            csvEscape(getTailleLibelle()),
            csvEscape(categorieEntreprise),
            csvEscape(chiffreAffaires),
            csvEscape(siteWeb),
            csvEscape(email),
            csvEscape(emailRH),
            csvEscape(telephone),
            csvEscape(adresse),
            csvEscape(codePostal),
            csvEscape(ville),
            csvEscape(departement),
            csvEscape(region),
            csvEscape(codeNAF),
            csvEscape(libelleNAF),
            csvEscape(dateCreation),
            csvEscape(source)
        );
    }
    
    private String csvEscape(String value) {
        if (value == null) return "";
        if (value.contains(";") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
    
    /**
     * En-têtes CSV
     */
    public static String getCsvHeaders(String separator) {
        return String.join(separator,
            "Nom entreprise",
            "Nom commercial",
            "SIREN",
            "SIRET",
            "Taille (effectif)",
            "Catégorie",
            "Chiffre d'affaires",
            "🌐 Site web",
            "📧 Email général",
            "📧 Email RH",
            "📞 Téléphone",
            "Adresse",
            "Code postal",
            "Ville",
            "Département",
            "Région",
            "Code NAF",
            "Secteur activité",
            "Date création",
            "Source"
        );
    }
}

package com.jobsearch;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Classe représentant une offre d'emploi avec toutes ses informations détaillées
 */
public class JobOffer {
    // Informations de base
    private String id;
    private String intitule;
    private String description;
    private String dateCreation;
    private String dateActualisation;
    
    // Entreprise
    private String entrepriseNom;
    private String entrepriseDescription;
    private String entrepriseUrl;
    private String entrepriseLogoUrl;
    
    // Contact recruteur (PRIORITÉ)
    private String contactEmail;
    private String contactNom;
    private String contactTelephone;
    private String contactUrl;
    
    // Localisation
    private String lieuTravail;
    private String ville;
    private String codePostal;
    private String departement;
    private String region;
    private String pays;
    private Double latitude;
    private Double longitude;
    
    // Détails du poste
    private String typeContrat;
    private String typeContratLibelle;
    private String natureContrat;
    private String experienceLibelle;
    private String experienceExige;
    private String formations;
    private String langues;
    private String permis;
    private String competences;
    
    // Conditions
    private String salaire;
    private String dureeTravail;
    private String dureeTravailLibelle;
    private String qualification;
    private String secteurActivite;
    private String secteurActiviteLibelle;
    
    // Liens
    private String origineOffre;
    private String urlOrigine;
    private String urlPostulation;
    
    // Métadonnées
    private String source;
    private String typeSource;
    
    // Constructeur
    public JobOffer() {
        this.source = "Offre d'emploi";
        this.typeSource = "API France Travail";
    }
    
    // Getters et Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getIntitule() { return intitule; }
    public void setIntitule(String intitule) { this.intitule = intitule; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public String getDateCreation() { return dateCreation; }
    public void setDateCreation(String dateCreation) { this.dateCreation = dateCreation; }
    
    public String getDateActualisation() { return dateActualisation; }
    public void setDateActualisation(String dateActualisation) { this.dateActualisation = dateActualisation; }
    
    public String getEntrepriseNom() { return entrepriseNom; }
    public void setEntrepriseNom(String entrepriseNom) { this.entrepriseNom = entrepriseNom; }
    
    public String getEntrepriseDescription() { return entrepriseDescription; }
    public void setEntrepriseDescription(String entrepriseDescription) { this.entrepriseDescription = entrepriseDescription; }
    
    public String getEntrepriseUrl() { return entrepriseUrl; }
    public void setEntrepriseUrl(String entrepriseUrl) { this.entrepriseUrl = entrepriseUrl; }
    
    public String getEntrepriseLogoUrl() { return entrepriseLogoUrl; }
    public void setEntrepriseLogoUrl(String entrepriseLogoUrl) { this.entrepriseLogoUrl = entrepriseLogoUrl; }
    
    public String getContactEmail() { return contactEmail; }
    public void setContactEmail(String contactEmail) { this.contactEmail = contactEmail; }
    
    public String getContactNom() { return contactNom; }
    public void setContactNom(String contactNom) { this.contactNom = contactNom; }
    
    public String getContactTelephone() { return contactTelephone; }
    public void setContactTelephone(String contactTelephone) { this.contactTelephone = contactTelephone; }
    
    public String getContactUrl() { return contactUrl; }
    public void setContactUrl(String contactUrl) { this.contactUrl = contactUrl; }
    
    public String getLieuTravail() { return lieuTravail; }
    public void setLieuTravail(String lieuTravail) { this.lieuTravail = lieuTravail; }
    
    public String getVille() { return ville; }
    public void setVille(String ville) { this.ville = ville; }
    
    public String getCodePostal() { return codePostal; }
    public void setCodePostal(String codePostal) { this.codePostal = codePostal; }
    
    public String getDepartement() { return departement; }
    public void setDepartement(String departement) { this.departement = departement; }
    
    public String getRegion() { return region; }
    public void setRegion(String region) { this.region = region; }
    
    public String getPays() { return pays; }
    public void setPays(String pays) { this.pays = pays; }
    
    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }
    
    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }
    
    public String getTypeContrat() { return typeContrat; }
    public void setTypeContrat(String typeContrat) { this.typeContrat = typeContrat; }
    
    public String getTypeContratLibelle() { return typeContratLibelle; }
    public void setTypeContratLibelle(String typeContratLibelle) { this.typeContratLibelle = typeContratLibelle; }
    
    public String getNatureContrat() { return natureContrat; }
    public void setNatureContrat(String natureContrat) { this.natureContrat = natureContrat; }
    
    public String getExperienceLibelle() { return experienceLibelle; }
    public void setExperienceLibelle(String experienceLibelle) { this.experienceLibelle = experienceLibelle; }
    
    public String getExperienceExige() { return experienceExige; }
    public void setExperienceExige(String experienceExige) { this.experienceExige = experienceExige; }
    
    public String getFormations() { return formations; }
    public void setFormations(String formations) { this.formations = formations; }
    
    public String getLangues() { return langues; }
    public void setLangues(String langues) { this.langues = langues; }
    
    public String getPermis() { return permis; }
    public void setPermis(String permis) { this.permis = permis; }
    
    public String getCompetences() { return competences; }
    public void setCompetences(String competences) { this.competences = competences; }
    
    public String getSalaire() { return salaire; }
    public void setSalaire(String salaire) { this.salaire = salaire; }
    
    public String getDureeTravail() { return dureeTravail; }
    public void setDureeTravail(String dureeTravail) { this.dureeTravail = dureeTravail; }
    
    public String getDureeTravailLibelle() { return dureeTravailLibelle; }
    public void setDureeTravailLibelle(String dureeTravailLibelle) { this.dureeTravailLibelle = dureeTravailLibelle; }
    
    public String getQualification() { return qualification; }
    public void setQualification(String qualification) { this.qualification = qualification; }
    
    public String getSecteurActivite() { return secteurActivite; }
    public void setSecteurActivite(String secteurActivite) { this.secteurActivite = secteurActivite; }
    
    public String getSecteurActiviteLibelle() { return secteurActiviteLibelle; }
    public void setSecteurActiviteLibelle(String secteurActiviteLibelle) { this.secteurActiviteLibelle = secteurActiviteLibelle; }
    
    public String getOrigineOffre() { return origineOffre; }
    public void setOrigineOffre(String origineOffre) { this.origineOffre = origineOffre; }
    
    public String getUrlOrigine() { return urlOrigine; }
    public void setUrlOrigine(String urlOrigine) { this.urlOrigine = urlOrigine; }
    
    public String getUrlPostulation() { return urlPostulation; }
    public void setUrlPostulation(String urlPostulation) { this.urlPostulation = urlPostulation; }
    
    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }
    
    public String getTypeSource() { return typeSource; }
    public void setTypeSource(String typeSource) { this.typeSource = typeSource; }
    
    /**
     * Convertir en JSON
     */
    public String toJson() {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        return gson.toJson(this);
    }
    
    /**
     * Résumé pour affichage dans le tableau
     */
    public String getResume() {
        StringBuilder sb = new StringBuilder();
        if (intitule != null) sb.append(intitule);
        if (typeContratLibelle != null) sb.append(" | ").append(typeContratLibelle);
        if (salaire != null) sb.append(" | ").append(salaire);
        return sb.toString();
    }
    
    /**
     * Informations de contact formatées
     */
    public String getContactInfo() {
        StringBuilder sb = new StringBuilder();
        if (contactEmail != null && !contactEmail.isEmpty()) {
            sb.append("📧 ").append(contactEmail);
        }
        if (contactTelephone != null && !contactTelephone.isEmpty()) {
            if (sb.length() > 0) sb.append(" | ");
            sb.append("📞 ").append(contactTelephone);
        }
        if (contactNom != null && !contactNom.isEmpty()) {
            if (sb.length() > 0) sb.append(" | ");
            sb.append("👤 ").append(contactNom);
        }
        return sb.length() > 0 ? sb.toString() : "N/A";
    }
}

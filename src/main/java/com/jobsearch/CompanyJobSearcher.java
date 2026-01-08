package com.jobsearch;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import com.google.gson.*;

/**
 * Application Java 21 avec interface Swing moderne pour rechercher des entreprises
 * en Île-de-France ayant des besoins en développement informatique.
 * 
 * Utilise :
 * - API SIRENE (INSEE) pour les données d'entreprises
 * - API France Travail pour les offres d'emploi IT
 * - Configuration via fichier config.properties
 */
public class CompanyJobSearcher extends JFrame {
    
    // Configuration chargée depuis config.properties
    private final ConfigLoader config;
    
    // Composants UI
    private JTextField searchField;
    private JComboBox<String> departmentCombo;
    private JComboBox<String> searchTypeCombo;
    private JButton searchButton;
    private JButton stopButton;
    private JTable resultTable;
    private DefaultTableModel tableModel;
    private JProgressBar progressBar;
    private JLabel statusLabel;
    private JTextField tokenClientIdField;
    private JPasswordField tokenClientSecretField;
    
    // HTTP Client
    private final HttpClient httpClient;
    private final Gson gson;
    private volatile boolean searchInProgress = false;
    private String franceTravailAccessToken = null;
    
    public CompanyJobSearcher() {
        super("Recherche d'Entreprises IT - Île-de-France");
        
        // Charger la configuration
        this.config = ConfigLoader.getInstance();
        
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(config.getHttpTimeout()))
                .build();
        this.gson = new GsonBuilder().setPrettyPrinting().create();
        
        initializeUI();
        setSize(config.getWindowWidth(), config.getWindowHeight());
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        
        // Charger les identifiants depuis la config si disponibles
        if (config.hasFranceTravailCredentials()) {
            tokenClientIdField.setText(config.getFranceTravailClientId());
            tokenClientSecretField.setText(config.getFranceTravailClientSecret());
        }
    }
    
    private void initializeUI() {
        setLayout(new BorderLayout(10, 10));
        
        // Panel principal avec marges
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(new EmptyBorder(15, 15, 15, 15));
        
        // Panel de recherche
        mainPanel.add(createSearchPanel(), BorderLayout.NORTH);
        
        // Panel des résultats
        mainPanel.add(createResultsPanel(), BorderLayout.CENTER);
        
        // Panel de statut
        mainPanel.add(createStatusPanel(), BorderLayout.SOUTH);
        
        add(mainPanel);
    }
    
    private JPanel createSearchPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createTitledBorder("Paramètres de recherche"));
        
        // Panel de configuration API
        JPanel configPanel = createApiConfigPanel();
        panel.add(configPanel, BorderLayout.NORTH);
        
        // Panel de critères de recherche
        JPanel searchCriteriaPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        // Type de recherche
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0;
        searchCriteriaPanel.add(new JLabel("Type de recherche:"), gbc);
        
        gbc.gridx = 1; gbc.weightx = 1;
        searchTypeCombo = new JComboBox<>(new String[]{
            "Offres d'emploi IT",
            "Entreprises secteur informatique",
            "Recherche combinée"
        });
        searchCriteriaPanel.add(searchTypeCombo, gbc);
        
        // Département
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0;
        searchCriteriaPanel.add(new JLabel("Département:"), gbc);
        
        gbc.gridx = 1; gbc.weightx = 1;
        String[] idfDepartments = config.getIdfDepartments();
        String[] deptOptions = new String[idfDepartments.length + 1];
        deptOptions[0] = "Tous (Île-de-France)";
        System.arraycopy(idfDepartments, 0, deptOptions, 1, idfDepartments.length);
        departmentCombo = new JComboBox<>(deptOptions);
        searchCriteriaPanel.add(departmentCombo, gbc);
        
        // Mots-clés
        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0;
        searchCriteriaPanel.add(new JLabel("Mots-clés:"), gbc);
        
        gbc.gridx = 1; gbc.weightx = 1;
        searchField = new JTextField(config.getDefaultKeywords());
        searchField.setFont(new Font("Arial", Font.PLAIN, 14));
        searchCriteriaPanel.add(searchField, gbc);
        
        // Boutons
        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2;
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        
        searchButton = new JButton("🔍 Rechercher");
        searchButton.setFont(new Font("Arial", Font.BOLD, 14));
        searchButton.setPreferredSize(new Dimension(150, 35));
        searchButton.addActionListener(e -> startSearch());
        
        stopButton = new JButton("⏹ Arrêter");
        stopButton.setFont(new Font("Arial", Font.BOLD, 14));
        stopButton.setPreferredSize(new Dimension(150, 35));
        stopButton.setEnabled(false);
        stopButton.addActionListener(e -> stopSearch());
        
        buttonPanel.add(searchButton);
        buttonPanel.add(stopButton);
        searchCriteriaPanel.add(buttonPanel, gbc);
        
        panel.add(searchCriteriaPanel, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createApiConfigPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Configuration API France Travail (optionnel)"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(3, 5, 3, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0;
        panel.add(new JLabel("Client ID:"), gbc);
        
        gbc.gridx = 1; gbc.weightx = 1;
        tokenClientIdField = new JTextField();
        panel.add(tokenClientIdField, gbc);
        
        gbc.gridx = 2; gbc.weightx = 0;
        panel.add(new JLabel("Client Secret:"), gbc);
        
        gbc.gridx = 3; gbc.weightx = 1;
        tokenClientSecretField = new JPasswordField();
        panel.add(tokenClientSecretField, gbc);
        
        gbc.gridx = 4; gbc.weightx = 0;
        JButton getTokenButton = new JButton("Obtenir Token");
        getTokenButton.addActionListener(e -> authenticateFranceTravail());
        panel.add(getTokenButton, gbc);
        
        return panel;
    }
    
    private JPanel createResultsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Résultats"));
        
        // Modèle de table
        String[] columns = {"Entreprise", "SIRET", "Secteur", "Département", "Ville", "Type", "Détails"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        resultTable = new JTable(tableModel);
        resultTable.setFont(new Font("Arial", Font.PLAIN, 12));
        resultTable.setRowHeight(25);
        resultTable.setAutoCreateRowSorter(true);
        resultTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));
        
        // Ajuster les largeurs de colonnes
        resultTable.getColumnModel().getColumn(0).setPreferredWidth(200);
        resultTable.getColumnModel().getColumn(1).setPreferredWidth(120);
        resultTable.getColumnModel().getColumn(2).setPreferredWidth(150);
        resultTable.getColumnModel().getColumn(3).setPreferredWidth(80);
        resultTable.getColumnModel().getColumn(4).setPreferredWidth(120);
        resultTable.getColumnModel().getColumn(5).setPreferredWidth(100);
        resultTable.getColumnModel().getColumn(6).setPreferredWidth(250);
        
        JScrollPane scrollPane = new JScrollPane(resultTable);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        // Panel d'actions sur les résultats
        JPanel actionsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton exportButton = new JButton("📊 Exporter CSV");
        exportButton.addActionListener(e -> exportToCSV());
        JButton clearButton = new JButton("🗑 Effacer");
        clearButton.addActionListener(e -> tableModel.setRowCount(0));
        
        actionsPanel.add(exportButton);
        actionsPanel.add(clearButton);
        actionsPanel.add(new JLabel("Total: "));
        
        JLabel countLabel = new JLabel("0");
        countLabel.setFont(new Font("Arial", Font.BOLD, 14));
        actionsPanel.add(countLabel);
        
        // Mettre à jour le compteur quand la table change
        tableModel.addTableModelListener(e -> 
            countLabel.setText(String.valueOf(tableModel.getRowCount())));
        
        panel.add(actionsPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private JPanel createStatusPanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        
        statusLabel = new JLabel("Prêt");
        statusLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        panel.add(statusLabel, BorderLayout.WEST);
        
        progressBar = new JProgressBar();
        progressBar.setStringPainted(true);
        panel.add(progressBar, BorderLayout.CENTER);
        
        return panel;
    }
    
    private void authenticateFranceTravail() {
        String clientId = tokenClientIdField.getText().trim();
        String clientSecret = new String(tokenClientSecretField.getPassword()).trim();
        
        if (clientId.isEmpty() || clientSecret.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Veuillez renseigner le Client ID et le Client Secret",
                "Erreur", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        System.out.println("🔐 Début authentification France Travail...");
        System.out.println("   Client ID: " + clientId.substring(0, Math.min(10, clientId.length())) + "...");
        
        CompletableFuture.runAsync(() -> {
            try {
                updateStatus("Authentification France Travail...");
                
                String formData = String.format(
                    "grant_type=client_credentials&client_id=%s&client_secret=%s&scope=%s",
                    URLEncoder.encode(clientId, StandardCharsets.UTF_8),
                    URLEncoder.encode(clientSecret, StandardCharsets.UTF_8),
                    URLEncoder.encode(config.getFranceTravailScope(), StandardCharsets.UTF_8)
                );
                
                System.out.println("   URL: " + config.getFranceTravailTokenUrl());
                System.out.println("   Scope: " + config.getFranceTravailScope());
                
                HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(config.getFranceTravailTokenUrl()))
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .POST(HttpRequest.BodyPublishers.ofString(formData))
                    .build();
                
                HttpResponse<String> response = httpClient.send(request, 
                    HttpResponse.BodyHandlers.ofString());
                
                System.out.println("   Code réponse: " + response.statusCode());
                
                if (response.statusCode() == 200) {
                    JsonObject json = gson.fromJson(response.body(), JsonObject.class);
                    franceTravailAccessToken = json.get("access_token").getAsString();
                    
                    System.out.println("✓ Token obtenu: " + franceTravailAccessToken.substring(0, Math.min(20, franceTravailAccessToken.length())) + "...");
                    System.out.println("✓ Token stocké dans franceTravailAccessToken");
                    
                    // Sauvegarder les identifiants dans la config
                    config.setProperty("francetravail.client.id", clientId);
                    config.setProperty("francetravail.client.secret", clientSecret);
                    
                    SwingUtilities.invokeLater(() -> {
                        updateStatus("✓ Authentification réussie");
                        JOptionPane.showMessageDialog(this,
                            "Authentification réussie !\nVous pouvez maintenant lancer une recherche.",
                            "Succès", JOptionPane.INFORMATION_MESSAGE);
                    });
                } else {
                    System.err.println("❌ Erreur HTTP: " + response.statusCode());
                    System.err.println("   Réponse: " + response.body());
                    throw new IOException("Erreur HTTP: " + response.statusCode() + " - " + response.body());
                }
                
            } catch (Exception e) {
                System.err.println("❌ Exception authentification: " + e.getClass().getName());
                System.err.println("   Message: " + e.getMessage());
                e.printStackTrace();
                
                SwingUtilities.invokeLater(() -> {
                    updateStatus("✗ Erreur d'authentification");
                    JOptionPane.showMessageDialog(this,
                        "Erreur d'authentification:\n" + e.getMessage() + 
                        "\n\nVérifiez vos identifiants Client ID et Client Secret.",
                        "Erreur", JOptionPane.ERROR_MESSAGE);
                });
            }
        });
    }
    
    private void startSearch() {
        if (searchInProgress) return;
        
        searchInProgress = true;
        searchButton.setEnabled(false);
        stopButton.setEnabled(true);
        
        // Sauvegarder le nombre de lignes avant d'effacer (pour comparaison)
        final int initialRowCount = tableModel.getRowCount();
        tableModel.setRowCount(0); // Effacer les résultats précédents
        progressBar.setValue(0);
        
        String searchType = (String) searchTypeCombo.getSelectedItem();
        String keywords = searchField.getText().trim();
        String department = getDepartmentCode();
        
        CompletableFuture.runAsync(() -> {
            try {
                if (searchType.equals("Offres d'emploi IT") || searchType.equals("Recherche combinée")) {
                    searchJobOffers(keywords, department);
                }
                
                if (searchType.equals("Entreprises secteur informatique") || searchType.equals("Recherche combinée")) {
                    searchITCompanies(department);
                }
                
                // Calculer le nombre de résultats trouvés
                final int foundResults = tableModel.getRowCount();
                
                SwingUtilities.invokeLater(() -> {
                    if (foundResults > 0) {
                        updateStatus("✓ Recherche terminée: " + foundResults + " résultats trouvés");
                    } else {
                        updateStatus("⚠ Recherche terminée: Aucun résultat trouvé. Essayez des mots-clés plus génériques.");
                    }
                });
                
            } catch (Exception e) {
                SwingUtilities.invokeLater(() -> {
                    updateStatus("✗ Erreur: " + e.getMessage());
                    JOptionPane.showMessageDialog(this,
                        "Erreur lors de la recherche:\n" + e.getMessage() +
                        "\n\nConsultez la console pour plus de détails.",
                        "Erreur", JOptionPane.ERROR_MESSAGE);
                });
            } finally {
                SwingUtilities.invokeLater(() -> {
                    searchButton.setEnabled(true);
                    stopButton.setEnabled(false);
                    searchInProgress = false;
                    progressBar.setValue(100);
                });
            }
        });
    }
    
    private void stopSearch() {
        searchInProgress = false;
        updateStatus("Recherche interrompue");
    }
    
    private String getDepartmentCode() {
        int index = departmentCombo.getSelectedIndex();
        if (index == 0) return null; // Tous départements
        String[] idfDepartments = config.getIdfDepartments();
        return idfDepartments[index - 1];
    }
    
    private void searchJobOffers(String keywords, String department) throws Exception {
        updateStatus("Recherche d'offres d'emploi IT...");
        
        // Si pas de département spécifique, on doit diviser l'IDF en plusieurs requêtes
        // car l'API limite à 5 départements maximum
        if (department == null) {
            String[] idfDepartments = config.getIdfDepartments();
            System.out.println("🔍 Recherche dans tous les départements IDF (limite API: 5 départements/requête)");
            
            // Diviser en groupes de 5 départements max
            for (int i = 0; i < idfDepartments.length; i += 5) {
                if (!searchInProgress) break;
                
                int end = Math.min(i + 5, idfDepartments.length);
                String[] deptGroup = new String[end - i];
                System.arraycopy(idfDepartments, i, deptGroup, 0, end - i);
                
                String deptList = String.join(",", deptGroup);
                System.out.println("  → Groupe " + ((i / 5) + 1) + ": Départements " + deptList);
                
                searchJobOffersForDepartments(keywords, deptList);
            }
        } else {
            searchJobOffersForDepartments(keywords, department);
        }
    }
    
    private void searchJobOffersForDepartments(String keywords, String departments) throws Exception {
        // Construction de l'URL
        StringBuilder urlBuilder = new StringBuilder(config.getFranceTravailApiBaseUrl());
        urlBuilder.append("/offresdemploi/v2/offres/search?");
        
        if (!keywords.isEmpty()) {
            urlBuilder.append("motsCles=").append(URLEncoder.encode(keywords, StandardCharsets.UTF_8));
            urlBuilder.append("&");
        }
        
        urlBuilder.append("departement=").append(departments);
        urlBuilder.append("&range=0-").append(config.getMaxResultsJobs() - 1);
        
        String url = urlBuilder.toString();
        System.out.println("🔍 URL recherche: " + url);
        System.out.println("🔑 Token présent: " + (franceTravailAccessToken != null ? "Oui" : "Non"));
        
        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .header("Accept", "application/json")
            .GET();
        
        // Ajouter le token d'authentification si disponible
        if (franceTravailAccessToken != null) {
            requestBuilder.header("Authorization", "Bearer " + franceTravailAccessToken);
            System.out.println("✓ Token ajouté à la requête");
        } else {
            System.err.println("⚠ ATTENTION: Aucun token disponible !");
            updateStatus("⚠ Token manquant - Cliquez sur 'Obtenir Token' d'abord");
            return;
        }
        
        try {
            HttpResponse<String> response = httpClient.send(requestBuilder.build(), 
                HttpResponse.BodyHandlers.ofString());
            
            int statusCode = response.statusCode();
            System.out.println("📡 Code réponse API: " + statusCode);
            
            if (statusCode == 200) {
                System.out.println("✓ Réponse reçue, parsing JSON...");
                JsonObject json = gson.fromJson(response.body(), JsonObject.class);
                
                // Debug: afficher la structure de la réponse
                System.out.println("📄 Clés JSON: " + json.keySet());
                
                JsonArray offers = json.getAsJsonArray("resultats");
                
                if (offers != null && offers.size() > 0) {
                    System.out.println("✓ " + offers.size() + " offres trouvées");
                    updateStatus("✓ " + offers.size() + " offres trouvées, ajout en cours...");
                    
                    for (int i = 0; i < offers.size() && searchInProgress; i++) {
                        JsonObject offer = offers.get(i).getAsJsonObject();
                        addJobOfferToTable(offer);
                        
                        int progress = (i + 1) * 100 / offers.size();
                        SwingUtilities.invokeLater(() -> progressBar.setValue(progress));
                    }
                    
                    System.out.println("✓ Toutes les offres ont été ajoutées");
                } else {
                    System.out.println("⚠ Aucune offre trouvée dans la réponse");
                    updateStatus("⚠ Aucune offre dans ce groupe de départements");
                }
            } else if (statusCode == 204) {
                // 204 No Content = requête réussie mais aucun résultat
                System.out.println("ℹ Code 204: Aucun résultat pour ces critères (c'est normal, pas une erreur)");
                updateStatus("ℹ Aucun résultat pour départements " + departments);
                // Ne pas lancer d'exception, c'est un cas normal
            } else if (statusCode == 206) {
                // 206 Partial Content = résultats partiels (acceptable)
                System.out.println("✓ Réponse partielle (code 206), parsing JSON...");
                JsonObject json = gson.fromJson(response.body(), JsonObject.class);
                JsonArray offers = json.getAsJsonArray("resultats");
                
                if (offers != null && offers.size() > 0) {
                    System.out.println("✓ " + offers.size() + " offres trouvées (résultats partiels)");
                    updateStatus("✓ " + offers.size() + " offres trouvées");
                    
                    for (int i = 0; i < offers.size() && searchInProgress; i++) {
                        JsonObject offer = offers.get(i).getAsJsonObject();
                        addJobOfferToTable(offer);
                    }
                }
            } else {
                String errorMsg = "Erreur API France Travail: " + statusCode;
                System.err.println("❌ " + errorMsg);
                String responseBody = response.body();
                if (responseBody != null && !responseBody.isEmpty()) {
                    System.err.println("📄 Réponse: " + responseBody.substring(0, Math.min(500, responseBody.length())));
                }
                throw new IOException(errorMsg + (responseBody != null ? " - Réponse: " + responseBody : ""));
            }
        } catch (Exception e) {
            System.err.println("❌ Exception lors de la recherche: " + e.getClass().getName() + ": " + e.getMessage());
            e.printStackTrace();
            updateStatus("⚠ Erreur: " + e.getMessage());
            throw e;
        }
    }
    
    private void addJobOfferToTable(JsonObject offer) {
        try {
            System.out.println("📝 Parsing offre: " + offer.keySet());
            
            String company = offer.has("entreprise") && 
                           offer.getAsJsonObject("entreprise").has("nom") ?
                           offer.getAsJsonObject("entreprise").get("nom").getAsString() : "N/A";
            
            String title = offer.has("intitule") ? 
                         offer.get("intitule").getAsString() : "N/A";
            
            System.out.println("  → Entreprise: " + company + ", Poste: " + title);
            
            String location = "N/A";
            if (offer.has("lieuTravail")) {
                JsonObject lieu = offer.getAsJsonObject("lieuTravail");
                location = (lieu.has("libelle") ? lieu.get("libelle").getAsString() : "");
            }
            
            String dept = "N/A";
            String city = "N/A";
            if (location.contains(" - ")) {
                String[] parts = location.split(" - ");
                if (parts.length >= 2) {
                    city = parts[0];
                    dept = parts[1].replaceAll("[^0-9]", "");
                }
            }
            
            String contract = offer.has("typeContrat") ? 
                            offer.get("typeContrat").getAsString() : "N/A";
            
            String details = String.format("Offre: %s | Contrat: %s", title, contract);
            
            // Créer des variables final pour utilisation dans la lambda
            final String finalCompany = company;
            final String finalDept = dept;
            final String finalCity = city;
            final String finalDetails = details;
            
            System.out.println("  → Ajout à la table: " + finalCompany + " - " + finalCity);
            
            SwingUtilities.invokeLater(() -> 
                tableModel.addRow(new Object[]{
                    finalCompany, "N/A", "Offre IT", finalDept, finalCity, "Offre d'emploi", finalDetails
                }));
                
        } catch (Exception e) {
            System.err.println("❌ Erreur parsing offre: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void searchITCompanies(String department) throws Exception {
        updateStatus("Recherche d'entreprises du secteur informatique...");
        
        // Codes NAF pour le secteur informatique depuis la config
        String[] itNafCodes = config.getNafCodesIT();
        
        for (String nafCode : itNafCodes) {
            if (!searchInProgress) break;
            
            searchCompaniesByNAF(nafCode, department);
        }
    }
    
    private void searchCompaniesByNAF(String nafCode, String department) throws Exception {
        updateStatus(String.format("Recherche entreprises NAF %s...", nafCode));
        
        StringBuilder urlBuilder = new StringBuilder(config.getInseeApiBaseUrl());
        urlBuilder.append("/siret?q=");
        
        // Filtres
        urlBuilder.append("activitePrincipaleUniteLegale:").append(nafCode);
        
        if (department != null) {
            urlBuilder.append(" AND codeCommuneEtablissement:").append(department).append("*");
        } else {
            // Tous les départements IDF
            String[] idfDepartments = config.getIdfDepartments();
            urlBuilder.append(" AND (");
            for (int i = 0; i < idfDepartments.length; i++) {
                if (i > 0) urlBuilder.append(" OR ");
                urlBuilder.append("codeCommuneEtablissement:").append(idfDepartments[i]).append("*");
            }
            urlBuilder.append(")");
        }
        
        urlBuilder.append("&nombre=").append(config.getMaxResultsCompanies());
        
        try {
            HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                .uri(URI.create(urlBuilder.toString()))
                .header("Accept", "application/json")
                .GET();
            
            // Ajouter la clé API INSEE si configurée
            if (config.hasInseeApiKey()) {
                requestBuilder.header("Authorization", "Bearer " + config.getInseeApiKey());
            }
            
            HttpRequest request = requestBuilder.build();
            
            HttpResponse<String> response = httpClient.send(request, 
                HttpResponse.BodyHandlers.ofString());
            
            if (response.statusCode() == 200) {
                JsonObject json = gson.fromJson(response.body(), JsonObject.class);
                
                if (json.has("etablissements")) {
                    JsonArray etablissements = json.getAsJsonArray("etablissements");
                    
                    for (int i = 0; i < etablissements.size() && searchInProgress; i++) {
                        JsonObject etab = etablissements.get(i).getAsJsonObject();
                        addCompanyToTable(etab, nafCode);
                    }
                }
            } else if (response.statusCode() == 401) {
                updateStatus("⚠ API SIRENE: authentification requise (clé API INSEE)");
            }
        } catch (Exception e) {
            updateStatus("⚠ Entreprises NAF " + nafCode + ": " + e.getMessage());
        }
    }
    
    private void addCompanyToTable(JsonObject etablissement, String nafCode) {
        try {
            String company = "N/A";
            String siret = "N/A";
            String city = "N/A";
            String dept = "N/A";
            String sector = getNAFDescription(nafCode);
            
            if (etablissement.has("uniteLegale")) {
                JsonObject ul = etablissement.getAsJsonObject("uniteLegale");
                if (ul.has("denominationUniteLegale")) {
                    company = ul.get("denominationUniteLegale").getAsString();
                }
            }
            
            if (etablissement.has("siret")) {
                siret = etablissement.get("siret").getAsString();
            }
            
            if (etablissement.has("adresseEtablissement")) {
                JsonObject adresse = etablissement.getAsJsonObject("adresseEtablissement");
                if (adresse.has("libelleCommuneEtablissement")) {
                    city = adresse.get("libelleCommuneEtablissement").getAsString();
                }
                if (adresse.has("codeCommuneEtablissement")) {
                    String codeCommune = adresse.get("codeCommuneEtablissement").getAsString();
                    if (codeCommune.length() >= 2) {
                        dept = codeCommune.substring(0, 2);
                    }
                }
            }
            
            String details = String.format("NAF: %s | Secteur: %s", nafCode, sector);
            
            String finalCompany = company;
            String finalSiret = siret;
            String finalCity = city;
            String finalDept = dept;
            String finalDetails = details;
            
            SwingUtilities.invokeLater(() -> 
                tableModel.addRow(new Object[]{
                    finalCompany, finalSiret, sector, finalDept, 
                    finalCity, "Entreprise IT", finalDetails
                }));
                
        } catch (Exception e) {
            System.err.println("Erreur parsing établissement: " + e.getMessage());
        }
    }
    
    private String getNAFDescription(String nafCode) {
        return switch (nafCode) {
            case "62.01Z" -> "Programmation informatique";
            case "62.02A" -> "Conseil systèmes informatiques";
            case "62.02B" -> "Tierce maintenance informatique";
            case "62.03Z" -> "Gestion installations informatiques";
            case "62.09Z" -> "Autres activités informatiques";
            case "63.11Z" -> "Traitement de données";
            case "63.12Z" -> "Portails Internet";
            default -> "Informatique";
        };
    }
    
    private void exportToCSV() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Exporter en CSV");
        fileChooser.setSelectedFile(new java.io.File(config.getExportDefaultFilename()));
        
        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try (java.io.PrintWriter writer = new java.io.PrintWriter(
                    fileChooser.getSelectedFile(), 
                    java.nio.charset.Charset.forName(config.getExportEncoding()))) {
                
                String separator = config.getExportSeparator();
                
                // En-têtes
                for (int i = 0; i < tableModel.getColumnCount(); i++) {
                    if (i > 0) writer.print(separator);
                    writer.print(tableModel.getColumnName(i));
                }
                writer.println();
                
                // Données
                for (int row = 0; row < tableModel.getRowCount(); row++) {
                    for (int col = 0; col < tableModel.getColumnCount(); col++) {
                        if (col > 0) writer.print(separator);
                        Object value = tableModel.getValueAt(row, col);
                        writer.print(value != null ? value.toString().replace(separator, ",") : "");
                    }
                    writer.println();
                }
                
                JOptionPane.showMessageDialog(this,
                    "Export réussi: " + fileChooser.getSelectedFile().getName(),
                    "Succès", JOptionPane.INFORMATION_MESSAGE);
                    
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this,
                    "Erreur lors de l'export: " + e.getMessage(),
                    "Erreur", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void updateStatus(String message) {
        SwingUtilities.invokeLater(() -> statusLabel.setText(message));
    }
    
    public static void main(String[] args) {
        // Définir le Look and Feel système
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        SwingUtilities.invokeLater(() -> {
            CompanyJobSearcher frame = new CompanyJobSearcher();
            frame.setVisible(true);
        });
    }
}

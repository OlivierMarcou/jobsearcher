package com.jobsearch;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Fenêtre de recherche avancée d'entreprises via API Pappers.fr
 * Pour candidatures spontanées
 */
public class PappersSearchWindow extends JFrame {
    
    private final ConfigLoader config;
    private PappersApiClient pappersClient;
    
    // Champs de formulaire
    private JTextField queryField;
    private JComboBox<String> regionCombo;
    private JComboBox<String> departementCombo;
    private JSpinner dateCreationMinSpinner;
    private JTextField caMinField;
    private JTextField caMaxField;
    private JTextField effectifMinField;
    private JTextField effectifMaxField;
    private JCheckBox excludeRadieesCheckbox;
    private JCheckBox excludeAutoEntCheckbox;
    private JComboBox<String> sortByCombo;
    
    // Boutons
    private JButton searchButton;
    private JButton exportCsvButton;
    private JButton exportJsonButton;
    
    // Résultats
    private JTable resultTable;
    private DefaultTableModel tableModel;
    private JLabel statusLabel;
    private JLabel countLabel;
    
    // Stockage des entreprises
    private final Map<String, Company> companies = new HashMap<>();
    
    public PappersSearchWindow(ConfigLoader config) {
        this.config = config;
        
        setTitle("🔍 Recherche Avancée Entreprises - API Pappers.fr");
        setSize(1400, 900);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        
        // Layout principal
        setLayout(new BorderLayout(10, 10));
        
        // Panel de recherche
        add(createSearchPanel(), BorderLayout.NORTH);
        
        // Panel de résultats
        add(createResultsPanel(), BorderLayout.CENTER);
        
        // Panel de status
        add(createStatusPanel(), BorderLayout.SOUTH);
    }
    
    private JPanel createSearchPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createTitledBorder("Critères de recherche"));
        
        // Panel de configuration API
        JPanel apiPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        if (config.hasPappersApiKey()) {
            apiPanel.add(new JLabel("✓ Clé API Pappers configurée"));
            pappersClient = new PappersApiClient(config.getPappersApiKey());
        } else {
            JLabel warningLabel = new JLabel("⚠ Clé API Pappers manquante");
            warningLabel.setForeground(Color.RED);
            apiPanel.add(warningLabel);
            
            JButton helpButton = new JButton("Comment configurer ?");
            helpButton.addActionListener(e -> showApiKeyHelp());
            apiPanel.add(helpButton);
        }
        
        panel.add(apiPanel, BorderLayout.NORTH);
        
        // Panel des critères
        JPanel criteriaPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        int row = 0;
        
        // Mots-clés
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0;
        criteriaPanel.add(new JLabel("Mots-clés (optionnel):"), gbc);
        
        gbc.gridx = 1; gbc.weightx = 1; gbc.gridwidth = 3;
        queryField = new JTextField();
        queryField.setToolTipText("Nom d'entreprise, activité, mot-clé...");
        criteriaPanel.add(queryField, gbc);
        
        row++;
        
        // Région
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0; gbc.gridwidth = 1;
        criteriaPanel.add(new JLabel("Région:"), gbc);
        
        gbc.gridx = 1; gbc.weightx = 1;
        String[] regionOptions = new String[RegionMapper.getAllRegions().length + 2];
        regionOptions[0] = "(Toutes régions)";
        regionOptions[1] = "Département spécifique";
        String[] allRegions = RegionMapper.getAllRegions();
        System.arraycopy(allRegions, 0, regionOptions, 2, allRegions.length);
        regionCombo = new JComboBox<>(regionOptions);
        regionCombo.addActionListener(e -> updateDepartmentCombo());
        criteriaPanel.add(regionCombo, gbc);
        
        // Département
        gbc.gridx = 2; gbc.weightx = 0;
        criteriaPanel.add(new JLabel("Département:"), gbc);
        
        gbc.gridx = 3; gbc.weightx = 1;
        departementCombo = new JComboBox<>();
        departementCombo.setEnabled(false);
        updateDepartmentCombo();
        criteriaPanel.add(departementCombo, gbc);
        
        row++;
        
        // Date de création
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0;
        criteriaPanel.add(new JLabel("Créées depuis:"), gbc);
        
        gbc.gridx = 1; gbc.weightx = 1;
        SpinnerModel yearModel = new SpinnerNumberModel(2020, 1900, 2026, 1);
        dateCreationMinSpinner = new JSpinner(yearModel);
        dateCreationMinSpinner.setToolTipText("Entreprises créées à partir de cette année");
        criteriaPanel.add(dateCreationMinSpinner, gbc);
        
        row++;
        
        // Chiffre d'affaires
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0;
        criteriaPanel.add(new JLabel("CA minimum (€):"), gbc);
        
        gbc.gridx = 1; gbc.weightx = 1;
        caMinField = new JTextField();
        caMinField.setToolTipText("Ex: 100000 pour 100k€, 1000000 pour 1M€");
        criteriaPanel.add(caMinField, gbc);
        
        gbc.gridx = 2; gbc.weightx = 0;
        criteriaPanel.add(new JLabel("CA maximum (€):"), gbc);
        
        gbc.gridx = 3; gbc.weightx = 1;
        caMaxField = new JTextField();
        caMaxField.setToolTipText("Laisser vide pour pas de limite");
        criteriaPanel.add(caMaxField, gbc);
        
        row++;
        
        // Effectif
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0;
        criteriaPanel.add(new JLabel("Effectif min:"), gbc);
        
        gbc.gridx = 1; gbc.weightx = 1;
        effectifMinField = new JTextField();
        effectifMinField.setToolTipText("Ex: 10 pour au moins 10 salariés");
        criteriaPanel.add(effectifMinField, gbc);
        
        gbc.gridx = 2; gbc.weightx = 0;
        criteriaPanel.add(new JLabel("Effectif max:"), gbc);
        
        gbc.gridx = 3; gbc.weightx = 1;
        effectifMaxField = new JTextField();
        criteriaPanel.add(effectifMaxField, gbc);
        
        row++;
        
        // Filtres
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 2;
        excludeRadieesCheckbox = new JCheckBox("Exclure entreprises radiées (recommandé)", true);
        criteriaPanel.add(excludeRadieesCheckbox, gbc);
        
        gbc.gridx = 2; gbc.gridwidth = 2;
        excludeAutoEntCheckbox = new JCheckBox("Exclure auto-entrepreneurs (recommandé)", true);
        excludeAutoEntCheckbox.setToolTipText("Chercher uniquement des sociétés (SARL, SAS, SA, etc.)");
        criteriaPanel.add(excludeAutoEntCheckbox, gbc);
        
        row++;
        
        // Tri
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0; gbc.gridwidth = 1;
        criteriaPanel.add(new JLabel("Trier par:"), gbc);
        
        gbc.gridx = 1; gbc.weightx = 1;
        sortByCombo = new JComboBox<>(new String[]{
            "Pertinence",
            "Chiffre d'affaires (décroissant)",
            "Date de création (récent)"
        });
        criteriaPanel.add(sortByCombo, gbc);
        
        row++;
        
        // Boutons d'action
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 4;
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        
        searchButton = new JButton("🔍 Rechercher");
        searchButton.setFont(new Font("Arial", Font.BOLD, 14));
        searchButton.setPreferredSize(new Dimension(150, 35));
        searchButton.addActionListener(e -> startSearch());
        buttonPanel.add(searchButton);
        
        JButton clearButton = new JButton("🗑 Effacer");
        clearButton.addActionListener(e -> clearResults());
        buttonPanel.add(clearButton);
        
        criteriaPanel.add(buttonPanel, gbc);
        
        panel.add(criteriaPanel, BorderLayout.CENTER);
        
        return panel;
    }
    
    private void updateDepartmentCombo() {
        String selectedRegion = (String) regionCombo.getSelectedItem();
        departementCombo.removeAllItems();
        
        if (selectedRegion.equals("Département spécifique")) {
            departementCombo.setEnabled(true);
            String[] allDepts = RegionMapper.getAllDepartments();
            for (String dept : allDepts) {
                departementCombo.addItem(dept);
            }
        } else {
            departementCombo.setEnabled(false);
            departementCombo.addItem("(Tous de la région)");
        }
    }
    
    private JPanel createResultsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Résultats"));
        
        // Table
        String[] columns = {
            "Nom entreprise", 
            "SIREN", 
            "CA", 
            "Effectif", 
            "Forme juridique",
            "Ville", 
            "Dép.", 
            "🌐 Site web",
            "📧 Email"
        };
        
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
        
        // Largeurs colonnes
        resultTable.getColumnModel().getColumn(0).setPreferredWidth(250); // Nom
        resultTable.getColumnModel().getColumn(1).setPreferredWidth(100); // SIREN
        resultTable.getColumnModel().getColumn(2).setPreferredWidth(100); // CA
        resultTable.getColumnModel().getColumn(3).setPreferredWidth(100); // Effectif
        resultTable.getColumnModel().getColumn(4).setPreferredWidth(120); // Forme
        resultTable.getColumnModel().getColumn(5).setPreferredWidth(120); // Ville
        resultTable.getColumnModel().getColumn(6).setPreferredWidth(50);  // Dép
        resultTable.getColumnModel().getColumn(7).setPreferredWidth(150); // Site
        resultTable.getColumnModel().getColumn(8).setPreferredWidth(180); // Email
        
        JScrollPane scrollPane = new JScrollPane(resultTable);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        // Boutons d'export
        JPanel exportPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        
        exportCsvButton = new JButton("📊 Exporter CSV");
        exportCsvButton.addActionListener(e -> exportToCSV());
        exportPanel.add(exportCsvButton);
        
        exportJsonButton = new JButton("📄 Exporter JSON");
        exportJsonButton.addActionListener(e -> exportToJSON());
        exportPanel.add(exportJsonButton);
        
        exportPanel.add(new JLabel(" | Total: "));
        countLabel = new JLabel("0");
        countLabel.setFont(new Font("Arial", Font.BOLD, 14));
        exportPanel.add(countLabel);
        
        panel.add(exportPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private JPanel createStatusPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        statusLabel = new JLabel("Prêt. Configurez votre clé API Pappers et lancez une recherche.");
        statusLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        panel.add(statusLabel);
        return panel;
    }
    
    private void showApiKeyHelp() {
        String message = 
            "Configuration de la clé API Pappers:\n\n" +
            "1. Obtenez votre clé API sur:\n" +
            "   https://www.pappers.fr/api\n\n" +
            "2. Ouvrez le fichier:\n" +
            "   src/main/resources/config.properties\n\n" +
            "3. Ajoutez la ligne:\n" +
            "   pappers.api.key=VOTRE_CLE_ICI\n\n" +
            "4. Relancez l'application\n\n" +
            "Plan gratuit: 100 recherches/mois";
        
        JOptionPane.showMessageDialog(this,
            message,
            "Configuration clé API Pappers",
            JOptionPane.INFORMATION_MESSAGE);
    }
    
    private void startSearch() {
        if (!config.hasPappersApiKey()) {
            JOptionPane.showMessageDialog(this,
                "Clé API Pappers non configurée.\n\n" +
                "Configurez votre clé dans:\n" +
                "src/main/resources/config.properties\n\n" +
                "Ajoutez la ligne:\n" +
                "pappers.api.key=VOTRE_CLE_ICI\n\n" +
                "Obtenez une clé gratuite sur:\n" +
                "https://www.pappers.fr/api",
                "Configuration requise",
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        if (pappersClient == null) {
            pappersClient = new PappersApiClient(config.getPappersApiKey());
        }
        
        // Construire les critères
        PappersSearchCriteria criteria = new PappersSearchCriteria();
        
        // Mots-clés
        String query = queryField.getText().trim();
        if (!query.isEmpty()) {
            criteria.setQuery(query);
        }
        
        // Région/Département
        String selectedRegion = (String) regionCombo.getSelectedItem();
        if (!selectedRegion.equals("(Toutes régions)")) {
            if (selectedRegion.equals("Département spécifique")) {
                String dept = (String) departementCombo.getSelectedItem();
                criteria.setDepartement(dept);
            } else {
                criteria.setRegion(selectedRegion);
            }
        }
        
        // Date de création
        Integer dateMin = (Integer) dateCreationMinSpinner.getValue();
        criteria.setDateCreationMin(dateMin);
        
        // CA
        try {
            String caMinStr = caMinField.getText().trim();
            if (!caMinStr.isEmpty()) {
                criteria.setCaMin(Integer.parseInt(caMinStr));
            }
            
            String caMaxStr = caMaxField.getText().trim();
            if (!caMaxStr.isEmpty()) {
                criteria.setCaMax(Integer.parseInt(caMaxStr));
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this,
                "Erreur: Le chiffre d'affaires doit être un nombre",
                "Erreur de saisie",
                JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // Effectif
        try {
            String effMinStr = effectifMinField.getText().trim();
            if (!effMinStr.isEmpty()) {
                criteria.setEffectifMin(Integer.parseInt(effMinStr));
            }
            
            String effMaxStr = effectifMaxField.getText().trim();
            if (!effMaxStr.isEmpty()) {
                criteria.setEffectifMax(Integer.parseInt(effMaxStr));
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this,
                "Erreur: L'effectif doit être un nombre",
                "Erreur de saisie",
                JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // Filtres
        criteria.setExcludeRadiees(excludeRadieesCheckbox.isSelected());
        criteria.setExcludeAutoEntrepreneurs(excludeAutoEntCheckbox.isSelected());
        
        // Tri
        String sortOption = (String) sortByCombo.getSelectedItem();
        if (sortOption.contains("Chiffre")) {
            criteria.setSortBy("chiffre_affaires");
        } else if (sortOption.contains("création")) {
            criteria.setSortBy("date_creation");
        }
        
        // Lancer la recherche
        updateStatus("Recherche en cours...");
        searchButton.setEnabled(false);
        
        new Thread(() -> {
            try {
                List<Company> results = pappersClient.searchCompanies(criteria);
                
                SwingUtilities.invokeLater(() -> {
                    displayResults(results);
                    updateStatus("✓ " + results.size() + " entreprises trouvées | " + criteria.getSummary());
                    searchButton.setEnabled(true);
                });
                
            } catch (Exception e) {
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(this,
                        "Erreur lors de la recherche:\n" + e.getMessage(),
                        "Erreur",
                        JOptionPane.ERROR_MESSAGE);
                    updateStatus("❌ Erreur: " + e.getMessage());
                    searchButton.setEnabled(true);
                });
                e.printStackTrace();
            }
        }).start();
    }
    
    private void displayResults(List<Company> results) {
        tableModel.setRowCount(0);
        companies.clear();
        
        for (Company company : results) {
            companies.put(company.getUniqueKey(), company);
            
            tableModel.addRow(new Object[]{
                company.getNom() != null ? company.getNom() : "N/A",
                company.getSiren() != null ? company.getSiren() : "N/A",
                company.getChiffreAffaires() != null ? company.getChiffreAffaires() : "N/A",
                company.getTailleLibelle(),
                company.getCategorieEntreprise() != null ? company.getCategorieEntreprise() : "N/A",
                company.getVille() != null ? company.getVille() : "N/A",
                company.getDepartement() != null ? company.getDepartement() : "N/A",
                company.getSiteWeb() != null ? company.getSiteWeb() : "N/A",
                company.getEmail() != null ? company.getEmail() : "N/A"
            });
        }
        
        countLabel.setText(String.valueOf(results.size()));
    }
    
    private void clearResults() {
        tableModel.setRowCount(0);
        companies.clear();
        countLabel.setText("0");
        updateStatus("Résultats effacés");
    }
    
    private void exportToCSV() {
        if (companies.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Aucune entreprise à exporter",
                "Information",
                JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Exporter en CSV");
        fileChooser.setSelectedFile(new java.io.File("entreprises_pappers.csv"));
        
        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try (java.io.PrintWriter writer = new java.io.PrintWriter(
                    fileChooser.getSelectedFile(), 
                    java.nio.charset.Charset.forName("UTF-8"))) {
                
                String sep = ";";
                writer.println(Company.getCsvHeaders(sep));
                
                companies.values().stream()
                    .sorted((c1, c2) -> {
                        String n1 = c1.getNom() != null ? c1.getNom() : "";
                        String n2 = c2.getNom() != null ? c2.getNom() : "";
                        return n1.compareToIgnoreCase(n2);
                    })
                    .forEach(company -> writer.println(company.toCsvLine(sep)));
                
                JOptionPane.showMessageDialog(this,
                    "Export CSV réussi: " + companies.size() + " entreprises\n" +
                    "Fichier: " + fileChooser.getSelectedFile().getName(),
                    "Succès",
                    JOptionPane.INFORMATION_MESSAGE);
                    
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this,
                    "Erreur lors de l'export CSV:\n" + e.getMessage(),
                    "Erreur",
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void exportToJSON() {
        if (companies.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Aucune entreprise à exporter",
                "Information",
                JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Exporter en JSON");
        fileChooser.setSelectedFile(new java.io.File("entreprises_pappers.json"));
        
        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try (java.io.FileWriter writer = new java.io.FileWriter(fileChooser.getSelectedFile())) {
                
                List<Company> sortedCompanies = new java.util.ArrayList<>(companies.values());
                sortedCompanies.sort((c1, c2) -> {
                    String n1 = c1.getNom() != null ? c1.getNom() : "";
                    String n2 = c2.getNom() != null ? c2.getNom() : "";
                    return n1.compareToIgnoreCase(n2);
                });
                
                Gson gsonPretty = new GsonBuilder().setPrettyPrinting().create();
                String json = gsonPretty.toJson(sortedCompanies);
                writer.write(json);
                
                JOptionPane.showMessageDialog(this,
                    "Export JSON réussi: " + companies.size() + " entreprises\n" +
                    "Fichier: " + fileChooser.getSelectedFile().getName(),
                    "Succès",
                    JOptionPane.INFORMATION_MESSAGE);
                    
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this,
                    "Erreur lors de l'export JSON:\n" + e.getMessage(),
                    "Erreur",
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void updateStatus(String message) {
        statusLabel.setText(message);
    }
}

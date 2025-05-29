package vue;

import controller.ControleurRendezVous;
import model.GestionnairePatients;
import model.GestionnaireRendezVous;
import model.RendezVous;
import model.Patient;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.*;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.regex.PatternSyntaxException;

public class VueRendezVous implements Vue {
    private JPanel panel;
    private JTable rendezVousTable;
    private TableRowSorter<DefaultTableModel> rendezVousSorter;
    private JTextField searchField;
    private GestionnaireRendezVous gestionnaire;
    private GestionnairePatients gestionnairePatients;
    private Accueil medcin;
    ControleurRendezVous controleur; // Référence au contrôleur
    private final Color ERROR_COLOR = new Color(255, 200, 200);
    private final Color MAIN_BLUE = new Color(70, 130, 180);
    private final Color HOVER_BLUE = new Color(100, 150, 200);
    private JTextField patientField, dateField, heureField, descriptionField;
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

    public VueRendezVous(GestionnaireRendezVous gestionnaire, GestionnairePatients gestionnairePatients, Accueil medcin, ControleurRendezVous controleur) {
        this.gestionnaire = gestionnaire;
        this.gestionnairePatients = gestionnairePatients;
        this.medcin = medcin;
        this.controleur = controleur;
        initializeUI();
    }

    private void initializeUI() {
        panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        panel.setBackground(new Color(245, 245, 245));

        // Barre de recherche
        JPanel searchPanel = createSearchPanel();
        panel.add(searchPanel, BorderLayout.NORTH);

        // Table des rendez-vous
        initializeTable();
        JScrollPane scrollPane = new JScrollPane(rendezVousTable);
        panel.add(scrollPane, BorderLayout.CENTER);

        // Boutons d'action
        JPanel buttonPanel = createButtonPanel();
        panel.add(buttonPanel, BorderLayout.SOUTH);
    }

    private JPanel createSearchPanel() {
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.setBackground(new Color(245, 245, 245));
        
        searchField = new JTextField(20);
        JButton searchButton = createBlueButton("Rechercher");
        
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) { notifyRechercher(); }
            @Override
            public void removeUpdate(DocumentEvent e) { notifyRechercher(); }
            @Override
            public void changedUpdate(DocumentEvent e) { notifyRechercher(); }
        });

        searchButton.addActionListener(e -> notifyRechercher());
        searchField.addActionListener(e -> notifyRechercher());
        
        searchPanel.add(new JLabel("Rechercher:"));
        searchPanel.add(searchField);
        searchPanel.add(searchButton);
        
        return searchPanel;
    }

    private void initializeTable() {
        String[] columnNames = {"ID", "Patient", "Date", "Heure", "Description"};
        DefaultTableModel model = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        rendezVousTable = new JTable(model);
        rendezVousTable.setFont(new Font("Arial", Font.PLAIN, 14));
        rendezVousTable.setRowHeight(25);
        customizeTableAppearance();
        
        rendezVousSorter = new TableRowSorter<>(model);
        rendezVousTable.setRowSorter(rendezVousSorter);
    }

    private void customizeTableAppearance() {
        rendezVousTable.setBackground(Color.WHITE);
        rendezVousTable.setSelectionBackground(HOVER_BLUE);
        rendezVousTable.setSelectionForeground(Color.WHITE);
        rendezVousTable.setGridColor(new Color(200, 200, 200));
        
        JTableHeader header = rendezVousTable.getTableHeader();
        header.setBackground(Color.WHITE);
        header.setForeground(Color.BLACK);
        header.setFont(new Font("Arial", Font.BOLD, 14));
    }

    private JPanel createButtonPanel() {
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(new Color(245, 245, 245));
        
        JButton addButton = createBlueButton("Ajouter Rendez-vous");
        JButton editButton = createBlueButton("Modifier Rendez-vous");
        JButton deleteButton = createBlueButton("Supprimer Rendez-vous");
        
        addButton.addActionListener(e -> notifyAjouter());
        editButton.addActionListener(e -> notifyModifier());
        deleteButton.addActionListener(e -> notifySupprimer());
        
        buttonPanel.add(addButton);
        buttonPanel.add(editButton);
        buttonPanel.add(deleteButton);
        
        return buttonPanel;
    }

    private JButton createBlueButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        button.setBackground(MAIN_BLUE);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        button.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent evt) { button.setBackground(HOVER_BLUE); }
            public void mouseExited(MouseEvent evt) { button.setBackground(MAIN_BLUE); }
        });
        
        return button;
    }

    public String[] afficherFormulaireAjout() {
        return afficherFormulaire("Ajouter un Rendez-vous", null);
    }

    public String[] afficherFormulaireModification(RendezVous rendezVous) {
        return afficherFormulaire("Modifier un Rendez-vous", rendezVous);
    }

    private String[] afficherFormulaire(String title, RendezVous rendezVous) {
        JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(panel), title, Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setSize(550, 500);
        dialog.setLocationRelativeTo(SwingUtilities.getWindowAncestor(panel));

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        formPanel.setBackground(Color.WHITE);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(15, 15, 15, 15);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Titre
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setForeground(MAIN_BLUE);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        formPanel.add(titleLabel, gbc);

        gbc.gridwidth = 1;
        gbc.anchor = GridBagConstraints.WEST;

        // Champs du formulaire
        gbc.gridx = 0;
        gbc.gridy = 1;
        formPanel.add(new JLabel("Patient:"), gbc);
        gbc.gridx = 1;
        patientField = new JTextField(rendezVous != null ? rendezVous.getPatientNom() : "", 25);
        patientField.setFont(new Font("Arial", Font.PLAIN, 14));
        patientField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(150, 150, 150), 2),
            BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));
        formPanel.add(patientField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        formPanel.add(new JLabel("Date (dd/MM/yyyy):"), gbc);
        gbc.gridx = 1;
        dateField = new JTextField(rendezVous != null ? rendezVous.getDate().format(dateFormatter) : "", 25);
        dateField.setFont(new Font("Arial", Font.PLAIN, 14));
        dateField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(150, 150, 150), 2),
            BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));
        formPanel.add(dateField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        formPanel.add(new JLabel("Heure (HH:mm):"), gbc);
        gbc.gridx = 1;
        heureField = new JTextField(rendezVous != null ? rendezVous.getHeure().format(timeFormatter) : "", 25);
        heureField.setFont(new Font("Arial", Font.PLAIN, 14));
        heureField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(150, 150, 150), 2),
            BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));
        formPanel.add(heureField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 4;
        formPanel.add(new JLabel("Description:"), gbc);
        gbc.gridx = 1;
        descriptionField = new JTextField(rendezVous != null ? rendezVous.getDescription() : "", 25);
        descriptionField.setFont(new Font("Arial", Font.PLAIN, 14));
        descriptionField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(150, 150, 150), 2),
            BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));
        formPanel.add(descriptionField, gbc);

        // Bouton Enregistrer
        JButton saveButton = createBlueButton("Enregistrer");
        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        formPanel.add(saveButton, gbc);

        // Gestion de l'autocomplétion pour les patients
        setupPatientAutocomplete(patientField);

        // Ajouter des listeners pour réinitialiser la couleur des champs lors de la modification
        patientField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) { resetFieldColor(patientField); }
            @Override
            public void removeUpdate(DocumentEvent e) { resetFieldColor(patientField); }
            @Override
            public void changedUpdate(DocumentEvent e) { resetFieldColor(patientField); }
        });
        dateField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) { resetFieldColor(dateField); }
            @Override
            public void removeUpdate(DocumentEvent e) { resetFieldColor(dateField); }
            @Override
            public void changedUpdate(DocumentEvent e) { resetFieldColor(dateField); }
        });
        heureField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) { resetFieldColor(heureField); }
            @Override
            public void removeUpdate(DocumentEvent e) { resetFieldColor(heureField); }
            @Override
            public void changedUpdate(DocumentEvent e) { resetFieldColor(heureField); }
        });
        descriptionField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) { resetFieldColor(descriptionField); }
            @Override
            public void removeUpdate(DocumentEvent e) { resetFieldColor(descriptionField); }
            @Override
            public void changedUpdate(DocumentEvent e) { resetFieldColor(descriptionField); }
        });

        String[] result = new String[4];
        saveButton.addActionListener(e -> {
            result[0] = patientField.getText().trim();
            result[1] = dateField.getText().trim();
            result[2] = heureField.getText().trim();
            result[3] = descriptionField.getText().trim();
            dialog.dispose();
        });

        dialog.add(formPanel);
        dialog.setVisible(true);
        return result[0] == null ? null : result;
    }

    private void setupPatientAutocomplete(JTextField patientField) {
        JPopupMenu popupMenu = new JPopupMenu();
        
        patientField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                popupMenu.removeAll();
                String text = patientField.getText().trim().toLowerCase();
                if (text.isEmpty()) {
                    popupMenu.setVisible(false);
                    return;
                }
                
                List<Patient> patients = gestionnairePatients.getTous();
                for (Patient p : patients) {
                    if (p.getNom().toLowerCase().contains(text)) {
                        JMenuItem item = new JMenuItem(p.getNom());
                        item.addActionListener(e1 -> {
                            patientField.setText(p.getNom());
                            popupMenu.setVisible(false);
                        });
                        popupMenu.add(item);
                    }
                }
                
                if (popupMenu.getComponentCount() > 0) {
                    popupMenu.show(patientField, 0, patientField.getHeight());
                } else {
                    popupMenu.setVisible(false);
                }
            }
        });

        // Ajouter un FocusListener pour cacher le popup lorsque le champ perd le focus
        patientField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                popupMenu.setVisible(false);
            }
        });
    }

    private void resetFieldColor(JTextField field) {
        field.setBackground(Color.WHITE);
    }

    public void marquerChampErreur(String champ) {
        if ("patient".equals(champ)) patientField.setBackground(ERROR_COLOR);
        else if ("date".equals(champ)) dateField.setBackground(ERROR_COLOR);
        else if ("heure".equals(champ)) heureField.setBackground(ERROR_COLOR);
        else if ("description".equals(champ)) descriptionField.setBackground(ERROR_COLOR);
    }

    public void afficherErreur(String message) {
        JOptionPane.showMessageDialog(panel, message, "Erreur", JOptionPane.ERROR_MESSAGE);
    }

    public void afficherMessageSucces(String message) {
        JOptionPane.showMessageDialog(panel, message, "Succès", JOptionPane.INFORMATION_MESSAGE);
    }

    public int demanderConfirmationSuppression() {
        return JOptionPane.showConfirmDialog(panel, 
            "Êtes-vous sûr de vouloir supprimer ce rendez-vous ?", 
            "Confirmer la suppression", 
            JOptionPane.YES_NO_OPTION);
    }

    public int getSelectedRendezVousId() {
        int row = rendezVousTable.getSelectedRow();
        if (row < 0) return -1;
        int modelRow = rendezVousTable.convertRowIndexToModel(row);
        return (int) rendezVousTable.getModel().getValueAt(modelRow, 0);
    }

    public String getSearchText() {
        return searchField.getText().trim();
    }

    public void filtrerTableau(String text) {
        if (text.isEmpty()) {
            rendezVousSorter.setRowFilter(null);
        } else {
            try {
                rendezVousSorter.setRowFilter(RowFilter.regexFilter("(?i)" + text));
            } catch (PatternSyntaxException ex) {
                afficherErreur("Erreur dans le filtre de recherche : expression régulière invalide.");
            }
        }
    }

    @Override
    public JPanel getPanel() {
        return panel;
    }

    public void mettreAJour(List<RendezVous> rendezVous) {
        DefaultTableModel model = (DefaultTableModel) rendezVousTable.getModel();
        model.setRowCount(0);
        
        for (RendezVous rv : rendezVous) {
            model.addRow(new Object[]{
                rv.getId(), 
                rv.getPatientNom(), 
                rv.getDate().format(dateFormatter), // Formatter LocalDate pour l'affichage
                rv.getHeure().format(timeFormatter), // Formatter LocalTime pour l'affichage
                rv.getDescription()
            });
        }
    }

    @Override
    public void mettreAJour() {
        // Cette méthode est conservée pour compatibilité avec Accueil, mais elle appelle le contrôleur
        if (controleur != null) {
            controleur.mettreAJourVue();
        }
    }

    // Méthodes pour ajouter les listeners
    private ActionListener ajouterListener, modifierListener, supprimerListener, rechercherListener;

    public void ajouterListenerAjouter(ActionListener listener) { this.ajouterListener = listener; }
    public void ajouterListenerModifier(ActionListener listener) { this.modifierListener = listener; }
    public void ajouterListenerSupprimer(ActionListener listener) { this.supprimerListener = listener; }
    public void ajouterListenerRechercher(ActionListener listener) { this.rechercherListener = listener; }

    private void notifyAjouter() { if (ajouterListener != null) ajouterListener.actionPerformed(null); }
    private void notifyModifier() { if (modifierListener != null) modifierListener.actionPerformed(null); }
    private void notifySupprimer() { if (supprimerListener != null) supprimerListener.actionPerformed(null); }
    private void notifyRechercher() { if (rechercherListener != null) rechercherListener.actionPerformed(null); }

    @Override
    public void ajouterListener(ActionListener listener) {
        // Implémentation si nécessaire
    }
}
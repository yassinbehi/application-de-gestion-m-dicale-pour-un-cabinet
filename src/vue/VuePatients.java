package vue;

import model.GestionnairePatients;
import model.GestionnaireRendezVous;
import model.Patient;
import controller.ControleurPatients;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.regex.PatternSyntaxException;

public class VuePatients implements Vue {
    private JPanel panel;
    private JTable patientsTable;
    private TableRowSorter<DefaultTableModel> patientsSorter;
    private JTextField searchField;
    private GestionnairePatients gestionnaire;
    private GestionnaireRendezVous gestionRdv;
    private Accueil medcin;
    ControleurPatients controleur;
    private final Color ERROR_COLOR = new Color(255, 200, 200);
    private final Color MAIN_BLUE = new Color(70, 130, 180);
    private final Color HOVER_BLUE = new Color(100, 150, 200);
    private JTextField nomField, ageField, telephoneField, adresseField;
    private final boolean isDoctor;

    public VuePatients(GestionnairePatients gestionnaire, GestionnaireRendezVous gestionRdv, Accueil medcin, 
                      ControleurPatients controleur, boolean isDoctor) {
        this.gestionnaire = gestionnaire;
        this.gestionRdv = gestionRdv;
        this.medcin = medcin;
        this.controleur = controleur;
        this.isDoctor = isDoctor;

        panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        panel.setBackground(new Color(245, 245, 245));

        // Barre de recherche
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.setBackground(new Color(245, 245, 245));
        searchField = new JTextField(20);
        JButton searchButton = createNavButton("Rechercher");
        searchPanel.add(new JLabel("Rechercher:"));
        searchPanel.add(searchField);
        searchPanel.add(searchButton);
        panel.add(searchPanel, BorderLayout.NORTH);

        // Colonnes de la table - Conditional based on role
        String[] columnNames = isDoctor ? 
            new String[]{"ID", "Nom", "Âge", "Téléphone", "Adresse", "Actions"} :
            new String[]{"ID", "Nom", "Âge", "Téléphone", "Adresse"};
            
        DefaultTableModel model = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return isDoctor && column == 5; // Editable only for doctor in Actions column
            }

            @Override
            public Class<?> getColumnClass(int columnIndex) {
                return isDoctor && columnIndex == 5 ? JButton.class : super.getColumnClass(columnIndex);
            }
        };
        
        patientsTable = new JTable(model);
        patientsTable.setFont(new Font("Arial", Font.PLAIN, 14));
        patientsTable.setRowHeight(25);
        patientsTable.setBackground(Color.WHITE);
        patientsTable.setSelectionBackground(HOVER_BLUE);
        patientsTable.setSelectionForeground(Color.WHITE);
        patientsTable.setGridColor(new Color(200, 200, 200));

        JTableHeader header = patientsTable.getTableHeader();
        header.setBackground(Color.WHITE);
        header.setForeground(Color.BLACK);
        header.setFont(new Font("Arial", Font.BOLD, 14));

        if (isDoctor) {
            TableColumn actionColumn = patientsTable.getColumnModel().getColumn(5);
            actionColumn.setCellRenderer(new ButtonRenderer());
            actionColumn.setCellEditor(new ButtonEditor(new JCheckBox(), this));
        }

        patientsSorter = new TableRowSorter<>(model);
        patientsTable.setRowSorter(patientsSorter);

        JScrollPane scrollPane = new JScrollPane(patientsTable);
        scrollPane.setBackground(Color.WHITE);
        scrollPane.getViewport().setBackground(Color.WHITE);
        panel.add(scrollPane, BorderLayout.CENTER);

        // Boutons de gestion - Visible uniquement pour le médecin
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(new Color(245, 245, 245));
        
            JButton addButton = createNavButton("Ajouter Patient");
            JButton editButton = createNavButton("Modifier Patient");
            JButton deleteButton = createNavButton("Supprimer Patient");

            buttonPanel.add(addButton);
            buttonPanel.add(editButton);
            buttonPanel.add(deleteButton);

            // Associer les boutons aux listeners
            addButton.addActionListener(e -> notifyAjouter());
            editButton.addActionListener(e -> notifyModifier());
            deleteButton.addActionListener(e -> notifySupprimer());
        
        panel.add(buttonPanel, BorderLayout.SOUTH);

        // Recherche dynamique
        searchButton.addActionListener(e -> notifyRechercher());
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) { notifyRechercher(); }
            @Override
            public void removeUpdate(DocumentEvent e) { notifyRechercher(); }
            @Override
            public void changedUpdate(DocumentEvent e) { notifyRechercher(); }
        });
    }

    private JButton createNavButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        button.setBackground(MAIN_BLUE);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) { button.setBackground(HOVER_BLUE); }
            public void mouseExited(java.awt.event.MouseEvent evt) { button.setBackground(MAIN_BLUE); }
        });
        return button;
    }

    public String[] afficherFormulaireAjout() {
        return afficherFormulaire("Ajouter un Patient", null);
    }

    public String[] afficherFormulaireModification(Patient patient) {
        return afficherFormulaire("Modifier un Patient", patient);
    }

    private String[] afficherFormulaire(String titre, Patient patient) {
        JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(panel), titre, Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setSize(550, 500);
        dialog.setLocationRelativeTo(SwingUtilities.getWindowAncestor(panel));

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        formPanel.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(15, 15, 15, 15);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel titleLabel = new JLabel(titre);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setForeground(MAIN_BLUE);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        formPanel.add(titleLabel, gbc);

        gbc.gridwidth = 1;
        gbc.anchor = GridBagConstraints.WEST;

        gbc.gridx = 0;
        gbc.gridy = 1;
        formPanel.add(new JLabel("Nom:"), gbc);
        gbc.gridx = 1;
        nomField = new JTextField(patient != null ? patient.getNom() : "", 25);
        nomField.setFont(new Font("Arial", Font.PLAIN, 14));
        nomField.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(150, 150, 150), 2), BorderFactory.createEmptyBorder(5, 5, 5, 5)));
        nomField.setPreferredSize(new Dimension(300, 35));
        formPanel.add(nomField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        formPanel.add(new JLabel("Âge:"), gbc);
        gbc.gridx = 1;
        ageField = new JTextField(patient != null ? String.valueOf(patient.getAge()) : "", 25);
        ageField.setFont(new Font("Arial", Font.PLAIN, 14));
        ageField.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(150, 150, 150), 2), BorderFactory.createEmptyBorder(5, 5, 5, 5)));
        ageField.setPreferredSize(new Dimension(300, 35));
        formPanel.add(ageField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        formPanel.add(new JLabel("Téléphone:"), gbc);
        gbc.gridx = 1;
        telephoneField = new JTextField(patient != null ? patient.getTelephone() : "", 25);
        telephoneField.setFont(new Font("Arial", Font.PLAIN, 14));
        telephoneField.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(150, 150, 150), 2), BorderFactory.createEmptyBorder(5, 5, 5, 5)));
        telephoneField.setPreferredSize(new Dimension(300, 35));
        formPanel.add(telephoneField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 4;
        formPanel.add(new JLabel("Adresse:"), gbc);
        gbc.gridx = 1;
        adresseField = new JTextField(patient != null ? patient.getAdresse() : "", 25);
        adresseField.setFont(new Font("Arial", Font.PLAIN, 14));
        adresseField.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(150, 150, 150), 2), BorderFactory.createEmptyBorder(5, 5, 5, 5)));
        adresseField.setPreferredSize(new Dimension(300, 35));
        formPanel.add(adresseField, gbc);

        JButton saveButton = createNavButton("Enregistrer");
        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        formPanel.add(saveButton, gbc);

        String[] result = new String[4];
        saveButton.addActionListener(e -> {
            result[0] = nomField.getText().trim();
            result[1] = ageField.getText().trim();
            result[2] = telephoneField.getText().trim();
            result[3] = adresseField.getText().trim();
            dialog.dispose();
        });

        dialog.add(formPanel);
        dialog.setVisible(true);
        return result[0] == null ? null : result;
    }

    public void marquerChampErreur(String champ) {
        if ("nom".equals(champ)) nomField.setBackground(ERROR_COLOR);
        else if ("age".equals(champ)) ageField.setBackground(ERROR_COLOR);
        else if ("telephone".equals(champ)) telephoneField.setBackground(ERROR_COLOR);
        else if ("adresse".equals(champ)) adresseField.setBackground(ERROR_COLOR);
    }

    public void afficherErreur(String message) {
        JOptionPane.showMessageDialog(panel, message, "Erreur", JOptionPane.ERROR_MESSAGE);
    }

    public void afficherMessageSucces(String message) {
        JOptionPane.showMessageDialog(panel, message, "Succès", JOptionPane.INFORMATION_MESSAGE);
    }

    public int demanderConfirmationSuppression() {
        return JOptionPane.showConfirmDialog(panel, "Êtes-vous sûr de vouloir supprimer ce patient ? Cela supprimera également ses rendez-vous.", "Confirmer la suppression", JOptionPane.YES_NO_OPTION);
    }

    public int getSelectedPatientId() {
        int row = patientsTable.getSelectedRow();
        if (row < 0) return -1;
        int modelRow = patientsTable.convertRowIndexToModel(row);
        return (int) patientsTable.getModel().getValueAt(modelRow, 0);
    }

    public String getSearchText() {
        return searchField.getText().trim();
    }

    public void filtrerTableau(String text) {
        if (text.isEmpty()) {
            patientsSorter.setRowFilter(null);
        } else {
            try {
                patientsSorter.setRowFilter(RowFilter.regexFilter("(?i)" + text));
            } catch (PatternSyntaxException ex) {
                afficherErreur("Erreur dans le filtre de recherche.");
            }
        }
    }

    @Override
    public JPanel getPanel() {
        return panel;
    }

    public void mettreAJour(List<Patient> patients) {
        DefaultTableModel model = (DefaultTableModel) patientsTable.getModel();
        model.setRowCount(0);
        for (Patient p : patients) {
            if (isDoctor) {
                JButton ficheButton = new JButton("Voir Fiche Médicale");
                ficheButton.setFont(new Font("Segoe UI", Font.PLAIN, 12));
                ficheButton.setBackground(MAIN_BLUE);
                ficheButton.setForeground(Color.WHITE);
                ficheButton.setFocusPainted(false);
                model.addRow(new Object[]{p.getId(), p.getNom(), p.getAge(), p.getTelephone(), p.getAdresse(), ficheButton});
            } else {
                model.addRow(new Object[]{p.getId(), p.getNom(), p.getAge(), p.getTelephone(), p.getAdresse()});
            }
        }
    }

    public void showFichePatient(Patient patient) {
        VueFichePatient vueFiche = new VueFichePatient((JFrame) SwingUtilities.getWindowAncestor(panel), patient);
        vueFiche.show();
    }

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
        // À implémenter si besoin
    }

    @Override
    public void mettreAJour() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    class ButtonRenderer extends JButton implements TableCellRenderer {
        public ButtonRenderer() {
            if (!isDoctor) return;
            setOpaque(true);
            setFont(new Font("Segoe UI", Font.PLAIN, 12));
            setBackground(MAIN_BLUE);
            setForeground(Color.WHITE);
            setFocusPainted(false);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            if (!isDoctor) return null;
            setText("Voir Fiche Médicale");
            if (isSelected) {
                setBackground(table.getSelectionBackground());
                setForeground(table.getSelectionForeground());
            } else {
                setBackground(MAIN_BLUE);
                setForeground(Color.WHITE);
            }
            return this;
        }
    }

    class ButtonEditor extends DefaultCellEditor {
        private JButton button;
        private String label;
        private boolean isPushed;
        private VuePatients vuePatients;
        private int row;

        public ButtonEditor(JCheckBox checkBox, VuePatients vuePatients) {
            super(checkBox);
            if (!isDoctor) return;
            this.vuePatients = vuePatients;
            button = new JButton();
            button.setOpaque(true);
            button.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            button.setBackground(MAIN_BLUE);
            button.setForeground(Color.WHITE);
            button.setFocusPainted(false);
            button.addActionListener(e -> fireEditingStopped());
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            if (!isDoctor) return null;
            this.row = row;
            label = "Voir Fiche Médicale";
            button.setText(label);
            if (isSelected) {
                button.setBackground(table.getSelectionBackground());
                button.setForeground(table.getSelectionForeground());
            } else {
                button.setBackground(MAIN_BLUE);
                button.setForeground(Color.WHITE);
            }
            isPushed = true;
            return button;
        }

        @Override
        public Object getCellEditorValue() {
            if (isPushed) {
                int modelRow = patientsTable.convertRowIndexToModel(row);
                int id = (int) patientsTable.getModel().getValueAt(modelRow, 0);
                vuePatients.controleur.afficherFichePatient(id);
            }
            isPushed = false;
            return label;
        }

        @Override
        public boolean stopCellEditing() {
            isPushed = false;
            return super.stopCellEditing();
        }

        @Override
        protected void fireEditingStopped() {
            super.fireEditingStopped();
        }
    }
}
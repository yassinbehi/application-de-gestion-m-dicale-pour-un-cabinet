package vue;

import model.FicheMedicale;
import model.Patient;
import connexion.connexiondb;

import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class VueFichePatient {
    private JDialog dialog;
    private JTextArea historiqueArea, allergiesArea, traitementsArea, notesArea;
    private Patient patient;
    private JButton saveButton;
    private final Color MAIN_BLUE = new Color(70, 130, 180);
    private final Color DARKER_BLUE = new Color(50, 110, 150);
    private final Color LIGHT_BG = new Color(245, 248, 250);

    public VueFichePatient(JFrame parent, Patient patient) {
        this.patient = patient;
        initializeDialog(parent);
        createUI();
    }

    private void initializeDialog(JFrame parent) {
        dialog = new JDialog(parent, "Fiche Médicale - " + patient.getNom(), true);
        dialog.setSize(850, 700);
        dialog.setLocationRelativeTo(parent);
        dialog.setLayout(new BorderLayout());
        dialog.getContentPane().setBackground(LIGHT_BG);
    }

    private void createUI() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        mainPanel.setBackground(Color.WHITE);

        mainPanel.add(createHeaderPanel(), BorderLayout.NORTH);

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        FicheMedicale fiche = loadFicheMedicaleFromDatabase();
        patient.setFicheMedicale(fiche);

        historiqueArea = createEditableTextArea(fiche.getHistoriqueMedical());
        allergiesArea = createEditableTextArea(fiche.getAllergies());
        traitementsArea = createEditableTextArea(fiche.getTraitements());
        notesArea = createEditableTextArea(fiche.getNotes());

        tabbedPane.addTab("Historique", createScrollPane(historiqueArea));
        tabbedPane.addTab("Allergies", createScrollPane(allergiesArea));
        tabbedPane.addTab("Traitements", createScrollPane(traitementsArea));
        tabbedPane.addTab("Notes", createScrollPane(notesArea));

        mainPanel.add(tabbedPane, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 10));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        buttonPanel.setBackground(Color.WHITE);

        JButton cancelButton = createBlueButton("Annuler");
        cancelButton.addActionListener(e -> dialog.dispose());

        saveButton = createBlueButton("Enregistrer");
        saveButton.addActionListener(e -> {
            try {
                saveFicheMedicaleToDatabase();
                showMessage("Fiche médicale enregistrée avec succès !");
                dialog.dispose(); // Close dialog after successful save
            } catch (SQLException ex) {
                showMessage("Erreur lors de l'enregistrement : " + ex.getMessage());
            }
        });

        buttonPanel.add(cancelButton);
        buttonPanel.add(saveButton);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        dialog.add(mainPanel);
    }

    private FicheMedicale loadFicheMedicaleFromDatabase() {
        String sql = "SELECT historique, Allergie, traitements, notes FROM fichemedicale WHERE IDfiche = ?";
        try (Connection conn = connexiondb.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, patient.getId());
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return new FicheMedicale(
                    rs.getString("historique") != null ? rs.getString("historique") : "",
                    rs.getString("Allergie") != null ? rs.getString("Allergie") : "",
                    rs.getString("traitements") != null ? rs.getString("traitements") : "",
                    rs.getString("notes") != null ? rs.getString("notes") : ""
                );
            }
        } catch (SQLException ex) {
            showMessage("Erreur lors du chargement de la fiche : " + ex.getMessage());
        }
        return new FicheMedicale("", "", "", "");
    }

    private void saveFicheMedicaleToDatabase() throws SQLException {
        FicheMedicale updatedFiche = getUpdatedFiche();
        String sql = "UPDATE fichemedicale SET historique = ?, Allergie = ?, traitements = ?, notes = ? WHERE IDfiche = ?";
        
        try (Connection conn = connexiondb.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, updatedFiche.getHistoriqueMedical());
            stmt.setString(2, updatedFiche.getAllergies());
            stmt.setString(3, updatedFiche.getTraitements());
            stmt.setString(4, updatedFiche.getNotes());
            stmt.setInt(5, patient.getId());
            
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected == 0) {
                // If no rows were updated, the record might not exist; insert a new one
                insertFicheMedicale(updatedFiche);
            }
        }
    }

    private void insertFicheMedicale(FicheMedicale fiche) throws SQLException {
        String sql = "INSERT INTO fichemedicale (historique, Allergie, traitements, notes) VALUES (?, ?, ?, ?)";
        
        try (Connection conn = connexiondb.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, fiche.getHistoriqueMedical());
            stmt.setString(2, fiche.getAllergies());
            stmt.setString(3, fiche.getTraitements());
            stmt.setString(4, fiche.getNotes());
            
            stmt.executeUpdate();
        }
    }

    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));
        panel.setBackground(Color.WHITE);

        JLabel title = new JLabel("Dossier Médical");
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        title.setForeground(MAIN_BLUE);

        JPanel infoPanel = new JPanel(new GridLayout(2, 3, 10, 5));
        infoPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        infoPanel.setBackground(Color.WHITE);

        addInfoItem(infoPanel, "ID Patient:", String.valueOf(patient.getId()));
        addInfoItem(infoPanel, "Nom:", patient.getNom());
        addInfoItem(infoPanel, "Âge:", String.valueOf(patient.getAge()));
        addInfoItem(infoPanel, "Téléphone:", patient.getTelephone());
        addInfoItem(infoPanel, "Adresse:", patient.getAdresse());

        panel.add(title, BorderLayout.NORTH);
        panel.add(infoPanel, BorderLayout.CENTER);
        return panel;
    }

    private JTextArea createEditableTextArea(String text) {
        JTextArea textArea = new JTextArea(text);
        textArea.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        textArea.setBackground(Color.WHITE);
        return textArea;
    }

    private JScrollPane createScrollPane(JTextArea textArea) {
        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(230, 230, 230)));
        return scrollPane;
    }

    private void addInfoItem(JPanel panel, String label, String value) {
        JPanel itemPanel = new JPanel(new BorderLayout(5, 0));
        itemPanel.setBackground(Color.WHITE);

        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lbl.setForeground(new Color(100, 100, 100));

        JLabel valueLbl = new JLabel(value);
        valueLbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        valueLbl.setBorder(BorderFactory.createEmptyBorder(3, 5, 3, 5));

        itemPanel.add(lbl, BorderLayout.WEST);
        itemPanel.add(valueLbl, BorderLayout.CENTER);
        panel.add(itemPanel);
    }

    private JButton createBlueButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        button.setBackground(MAIN_BLUE);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(DARKER_BLUE),
            BorderFactory.createEmptyBorder(8, 20, 8, 20)
        ));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(DARKER_BLUE);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(MAIN_BLUE);
            }
        });
        return button;
    }

    public JButton getSaveButton() {
        return saveButton;
    }

    public FicheMedicale getUpdatedFiche() {
        return new FicheMedicale(
            historiqueArea.getText(),
            allergiesArea.getText(),
            traitementsArea.getText(),
            notesArea.getText()
        );
    }

    public Patient getPatient() {
        return patient;
    }

    public void show() {
        dialog.setVisible(true);
    }

    public void close() {
        dialog.dispose();
    }

    public void showMessage(String message) {
        JOptionPane.showMessageDialog(dialog, message);
    }
}
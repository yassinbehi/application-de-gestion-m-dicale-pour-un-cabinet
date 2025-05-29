package controller;

import vue.VueFichePatient;
import model.Patient;
import model.FicheMedicale;
import connexion.connexiondb;

import java.awt.event.ActionEvent;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class FichePatientController {

    private VueFichePatient view;
    private Patient patient;

    public FichePatientController(VueFichePatient view) {
        this.view = view;
        this.patient = view.getPatient();
        initializeListeners();
    }

    private void initializeListeners() {
        view.getSaveButton().addActionListener(e -> {
            try {
                FicheMedicale updatedFiche = view.getUpdatedFiche();
                if (isValidFiche(updatedFiche)) {
                    patient.setFicheMedicale(updatedFiche);
                    saveToDatabase(patient);
                    view.showMessage("Fiche médicale enregistrée avec succès !");
                    view.close();
                } else {
                    view.showMessage("Erreur : L'historique médical est requis.");
                }
            } catch (SQLException ex) {
                view.showMessage("Erreur lors de l'enregistrement : " + ex.getMessage());
            }
        });
    }

    private boolean isValidFiche(FicheMedicale fiche) {
        return fiche.getHistoriqueMedical() != null && !fiche.getHistoriqueMedical().trim().isEmpty();
    }

    private void saveToDatabase(Patient patient) throws SQLException {
        String sql = "INSERT INTO fiche_medicale (IDfiche, historique, Allergie, traitements, notes) " +
                     "VALUES (?, ?, ?, ?, ?) " +
                     "ON DUPLICATE KEY UPDATE " +
                     "historique = VALUES(historique), Allergie = VALUES(Allergie), " +
                     "traitements = VALUES(traitements), notes = VALUES(notes)";

        try (Connection conn = connexiondb.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            FicheMedicale fiche = patient.getFicheMedicale();
            stmt.setInt(1, patient.getId());
            stmt.setString(2, fiche.getHistoriqueMedical());
            stmt.setString(3, fiche.getAllergies());
            stmt.setString(4, fiche.getTraitements());
            stmt.setString(5, fiche.getNotes());
            stmt.executeUpdate();
        }
    }
}
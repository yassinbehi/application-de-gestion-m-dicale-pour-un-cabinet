package model;

import connexion.connexiondb;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class GestionnairePatients {

    public GestionnairePatients() {
    }

    public void ajouter(Patient patient) {
        Connection conn = null;
        try {
            conn = connexiondb.getConnection();
            conn.setAutoCommit(false);

            String sqlFiche = "INSERT INTO fichemedicale (historique, allergie, traitements, notes) VALUES (?, ?, ?, ?)";
            try (PreparedStatement stmtFiche = conn.prepareStatement(sqlFiche, PreparedStatement.RETURN_GENERATED_KEYS)) {
                FicheMedicale fiche = patient.getFicheMedicale();
                if (fiche == null) {
                    throw new IllegalArgumentException("La fiche médicale du patient ne peut pas être null");
                }
                stmtFiche.setString(1, fiche.getHistoriqueMedical());
                stmtFiche.setString(2, fiche.getAllergies());
                stmtFiche.setString(3, fiche.getTraitements());
                stmtFiche.setString(4, fiche.getNotes());
                stmtFiche.executeUpdate();

                try (ResultSet rsFiche = stmtFiche.getGeneratedKeys()) {
                    int idFiche = 0;
                    if (rsFiche.next()) {
                        idFiche = rsFiche.getInt(1);
                    }

                    String sqlPatient = "INSERT INTO Patient (nompatient, age, téléphone, adresse, IDFiche) VALUES (?, ?, ?, ?, ?)";
                    try (PreparedStatement stmtPatient = conn.prepareStatement(sqlPatient, PreparedStatement.RETURN_GENERATED_KEYS)) {
                        stmtPatient.setString(1, patient.getNom());
                        stmtPatient.setInt(2, patient.getAge());
                        stmtPatient.setString(3, patient.getTelephone());
                        stmtPatient.setString(4, patient.getAdresse());
                        stmtPatient.setInt(5, idFiche);
                        stmtPatient.executeUpdate();

                        try (ResultSet rsPatient = stmtPatient.getGeneratedKeys()) {
                            if (rsPatient.next()) {
                                patient.setId(rsPatient.getInt(1));
                            }
                        }
                    }
                }
            }

            conn.commit();
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            throw new RuntimeException("Erreur lors de l'ajout du patient : " + e.getMessage());
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void modifier(int id, Patient updatedPatient) {
        Connection conn = null;
        try {
            conn = connexiondb.getConnection();
            conn.setAutoCommit(false);

            String sqlGetFiche = "SELECT IDFiche FROM Patient WHERE IDPatient = ?";
            try (PreparedStatement stmtGetFiche = conn.prepareStatement(sqlGetFiche)) {
                stmtGetFiche.setInt(1, id);
                try (ResultSet rs = stmtGetFiche.executeQuery()) {
                    int idFiche = 0;
                    if (rs.next()) {
                        idFiche = rs.getInt("IDFiche");
                    } else {
                        throw new SQLException("Patient non trouvé avec ID : " + id);
                    }

                    String sqlFiche = "UPDATE fichemedicale SET historique = ?, allergie = ?, traitements = ?, notes = ? WHERE IDFiche = ?";
                    try (PreparedStatement stmtFiche = conn.prepareStatement(sqlFiche)) {
                        FicheMedicale fiche = updatedPatient.getFicheMedicale();
                        if (fiche == null) {
                            throw new IllegalArgumentException("La fiche médicale du patient ne peut pas être null");
                        }
                        stmtFiche.setString(1, fiche.getHistoriqueMedical());
                        stmtFiche.setString(2, fiche.getAllergies());
                        stmtFiche.setString(3, fiche.getTraitements());
                        stmtFiche.setString(4, fiche.getNotes());
                        stmtFiche.setInt(5, idFiche);
                        stmtFiche.executeUpdate();
                    }

                    String sqlPatient = "UPDATE Patient SET nompatient = ?, age = ?, téléphone = ?, adresse = ? WHERE IDPatient = ?";
                    try (PreparedStatement stmtPatient = conn.prepareStatement(sqlPatient)) {
                        stmtPatient.setString(1, updatedPatient.getNom());
                        stmtPatient.setInt(2, updatedPatient.getAge());
                        stmtPatient.setString(3, updatedPatient.getTelephone());
                        stmtPatient.setString(4, updatedPatient.getAdresse());
                        stmtPatient.setInt(5, id);
                        stmtPatient.executeUpdate();
                    }
                }
            }

            conn.commit();
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            throw new RuntimeException("Erreur lors de la modification du patient : " + e.getMessage());
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void supprimer(int id) {
        Connection conn = null;
        try {
            conn = connexiondb.getConnection();
            conn.setAutoCommit(false);

            String sqlGetFiche = "SELECT IDFiche FROM Patient WHERE IDPatient = ?";
            try (PreparedStatement stmtGetFiche = conn.prepareStatement(sqlGetFiche)) {
                stmtGetFiche.setInt(1, id);
                try (ResultSet rs = stmtGetFiche.executeQuery()) {
                    int idFiche = 0;
                    if (rs.next()) {
                        idFiche = rs.getInt("IDFiche");
                    } else {
                        throw new SQLException("Patient non trouvé avec ID : " + id);
                    }

                    String sqlPatient = "DELETE FROM Patient WHERE IDPatient = ?";
                    try (PreparedStatement stmtPatient = conn.prepareStatement(sqlPatient)) {
                        stmtPatient.setInt(1, id);
                        stmtPatient.executeUpdate();
                    }

                    String sqlFiche = "DELETE FROM fichemedicale WHERE IDFiche = ?";
                    try (PreparedStatement stmtFiche = conn.prepareStatement(sqlFiche)) {
                        stmtFiche.setInt(1, idFiche);
                        stmtFiche.executeUpdate();
                    }
                }
            }

            conn.commit();
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            throw new RuntimeException("Erreur lors de la suppression du patient : " + e.getMessage());
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public List<Patient> getTous() {
    List<Patient> patients = new ArrayList<>();
    try (Connection conn = connexiondb.getConnection()) {
        System.out.println("Connection opened for getTous: " + conn);
        try (PreparedStatement stmt = conn.prepareStatement(
                "SELECT p.IDPatient, p.nompatient, p.age, p.téléphone, p.adresse, " +
                "f.IDFiche, f.historique, f.allergie, f.traitements, f.notes " +
                "FROM Patient p JOIN fichemedicale f ON p.IDfiche = f.IDfiche");
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                FicheMedicale fiche = new FicheMedicale(
                    rs.getString("historique"),
                    rs.getString("allergie"),
                    rs.getString("traitements"),
                    rs.getString("notes")
                );
                Patient patient = new Patient(
                    rs.getInt("IDPatient"),
                    rs.getString("nompatient"),
                    rs.getInt("age"),
                    rs.getString("téléphone"),
                    rs.getString("adresse"),
                    fiche
                );
                patients.add(patient);
            }
        }
    } catch (SQLException e) {
        throw new RuntimeException("Erreur lors de la récupération des patients : " + e.getMessage());
    }
    return patients;
}

    public Patient getPatientById(int id) {
        try (Connection conn = connexiondb.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                 "SELECT p.IDPatient, p.nompatient, p.age, p.téléphone, p.adresse, " +
                 "f.IDfiche, f.historique, f.allergie, f.traitements, f.notes " +
                 "FROM Patient p JOIN fichemedicale f ON p.IDfiche = f.IDfiche " +
                 "WHERE p.IDPatient = ?")) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    FicheMedicale fiche = new FicheMedicale(
                        rs.getString("historique"),
                        rs.getString("allergie"),
                        rs.getString("traitements"),
                        rs.getString("notes")
                    );
                    return new Patient(
                        rs.getInt("IDPatient"),
                        rs.getString("nompatient"),
                        rs.getInt("age"),
                        rs.getString("téléphone"),
                        rs.getString("adresse"),
                        fiche
                    );
                }
                return null;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la récupération du patient : " + e.getMessage());
        }
    }
}
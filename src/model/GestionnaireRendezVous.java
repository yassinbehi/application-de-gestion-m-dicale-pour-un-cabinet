package model;

import connexion.connexiondb;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class GestionnaireRendezVous {

    public GestionnaireRendezVous() {
    }

    public void ajouter(RendezVous rv) {
        Connection conn = null;
        try {
            conn = connexiondb.getConnection();
            System.out.println("affiche conn "+conn.toString());
            String sql = "INSERT INTO rendez_vous (nompatient, date, heure, description) VALUES (? ,?, ?, ?)";
            PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            
            
            stmt.setString(1, rv.getPatientNom());
            stmt.setObject(2, rv.getDate()); // Changé de setString à setObject pour LocalDate
            stmt.setObject(3, rv.getHeure()); // Changé de setString à setObject pour LocalTime
            stmt.setString(4, rv.getDescription());
            stmt.executeUpdate();

            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                rv.setId(rs.getInt(1));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de l'ajout du rendez_vous : " + e.getMessage());
        } finally {
            try {
                if (conn != null) conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public void modifier(int id, RendezVous rv) {
        Connection conn = null;
        try {
            conn = connexiondb.getConnection();
            String sql = "UPDATE rendez_vous SET nompatient = ?, date = ?, heure = ?, description = ? WHERE IDRendezvous = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, rv.getPatientNom());
            stmt.setObject(2, rv.getDate()); // Changé de setString à setObject
            stmt.setObject(3, rv.getHeure()); // Changé de setString à setObject
            stmt.setString(4, rv.getDescription());
            stmt.setInt(5, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la modification du rendez-vous : " + e.getMessage());
        } finally {
            try {
                if (conn != null) conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public void supprimer(int id) {
        Connection conn = null;
        try {
            conn = connexiondb.getConnection();
            String sql = "DELETE FROM rendez_vous WHERE IDRendezvous = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la suppression du rendez-vous : " + e.getMessage());
        } finally {
            try {
                if (conn != null) conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public void supprimerParPatient(String patientNom) {
        Connection conn = null;
        try {
            conn = connexiondb.getConnection();
            String sql = "DELETE FROM rendez_vous WHERE nompatient = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, patientNom);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la suppression des rendez-vous du patient : " + e.getMessage());
        } finally {
            try {
                if (conn != null) conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public List<RendezVous> getTous() {
        List<RendezVous> rendezVous = new ArrayList<>();
        Connection conn = null;
        try {
            conn = connexiondb.getConnection();
            String sql = "SELECT IDRendezvous, nompatient, date, heure, description FROM rendez_vous";
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                RendezVous rv = new RendezVous(
                    rs.getInt("IDRendezvous"),
                    rs.getString("nompatient"),
                    rs.getObject("date", LocalDate.class), // Changé de getString à getObject
                    rs.getObject("heure", LocalTime.class), // Changé de getString à getObject
                    rs.getString("description")
                );
                rendezVous.add(rv);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la récupération des rendez-vous : " + e.getMessage());
        } finally {
            try {
                if (conn != null) conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return rendezVous;
    }

    public RendezVous getRendezVousById(int id) {
        Connection conn = null;
        try {
            conn = connexiondb.getConnection();
            String sql = "SELECT IDRendezvous, nompatient, date, heure, description FROM rendez_vous WHERE IDRendezvous = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return new RendezVous(
                    rs.getInt("IDRendezvous"),
                    rs.getString("nompatient"),
                    rs.getObject("date", LocalDate.class), // Changé de getString à getObject
                    rs.getObject("heure", LocalTime.class), // Changé de getString à getObject
                    rs.getString("description")
                );
            }
            return null;
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la récupération du rendez-vous : " + e.getMessage());
        } finally {
            try {
                if (conn != null) conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
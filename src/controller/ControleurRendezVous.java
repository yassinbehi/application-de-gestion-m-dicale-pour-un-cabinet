package controller;

import model.GestionnairePatients;
import model.GestionnaireRendezVous;
import model.RendezVous;
import model.Patient;
import vue.VueRendezVous;
import vue.Accueil;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

public class ControleurRendezVous {
    private final VueRendezVous vue;
    private final GestionnaireRendezVous gestionnaire;
    private final GestionnairePatients gestionnairePatients;
    private final Accueil medcin;
    private final Color ERROR_COLOR = new Color(255, 200, 200);

    public ControleurRendezVous(VueRendezVous vue, GestionnaireRendezVous gestionnaire, GestionnairePatients gestionnairePatients, Accueil medcin) {
        this.vue = vue;
        this.gestionnaire = gestionnaire;
        this.gestionnairePatients = gestionnairePatients;
        this.medcin = medcin;

        vue.ajouterListenerAjouter(e -> ajouterRendezVous());
        vue.ajouterListenerModifier(e -> modifierRendezVous());
        vue.ajouterListenerSupprimer(e -> supprimerRendezVous());
        vue.ajouterListenerRechercher(e -> rechercherRendezVous(vue.getSearchText()));

        mettreAJourVue();
    }

    private String validateRendezVousFormat(String patientNom, String date, String heure, String description) {
        if (patientNom == null || patientNom.trim().isEmpty()) return "Le nom du patient ne peut pas être vide.";
        if (date == null || date.trim().isEmpty()) return "La date ne peut pas être vide.";
        if (!date.matches("\\d{2}/\\d{2}/\\d{4}")) return "La date doit être au format dd/MM/yyyy (ex: 29/03/2025).";
        if (heure == null || heure.trim().isEmpty()) return "L'heure ne peut pas être vide.";
        if (!heure.matches("\\d{2}:\\d{2}")) return "L'heure doit être au format HH:mm (ex: 14:30).";
        if (description == null || description.trim().isEmpty()) return "La description ne peut pas être vide.";
        return null;
    }

    private String validateRendezVousValues(String patientNom, String date, String heure) {
        boolean patientExists = false;
        for (Patient p : gestionnairePatients.getTous()) {
            if (p.getNom().equalsIgnoreCase(patientNom)) {
                patientExists = true;
                break;
            }
        }
        if (!patientExists) return "Le patient " + patientNom + " n'existe pas.";

        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            LocalDate rdvDate = LocalDate.parse(date, formatter);
            LocalDate today = LocalDate.of(2025, 4, 11); // Date actuelle
            if (rdvDate.isBefore(today)) return "La date du rendez-vous doit être aujourd'hui ou dans le futur.";
        } catch (DateTimeParseException e) {
            return "La date doit être au format dd/MM/yyyy (ex: 29/03/2025).";
        }

        try {
            DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
            LocalTime rdvTime = LocalTime.parse(heure, timeFormatter);
            if (rdvTime.isBefore(LocalTime.of(8, 0)) || rdvTime.isAfter(LocalTime.of(18, 0))) {
                return "L'heure du rendez-vous doit être entre 08:00 et 18:00.";
            }
        } catch (DateTimeParseException e) {
            return "L'heure doit être au format HH:mm (ex: 14:30).";
        }

        return null;
    }

    private boolean hasTimeConflict(LocalDate date, LocalTime heure, int excludeId) {
        try {
            LocalDateTime startDateTime = LocalDateTime.of(date, heure);
            LocalDateTime endDateTime = startDateTime.plusMinutes(30);

            for (RendezVous rv : gestionnaire.getTous()) {
                if (rv.getId() == excludeId) continue;
                LocalDateTime rvStart = LocalDateTime.of(rv.getDate(), rv.getHeure());
                LocalDateTime rvEnd = rvStart.plusMinutes(30);
                if (startDateTime.isBefore(rvEnd) && endDateTime.isAfter(rvStart)) return true;
            }
            return false;
        } catch (Exception e) {
            return true;
        }
    }

    public void ajouterRendezVous() {
        String[] valeurs = vue.afficherFormulaireAjout();
        if (valeurs == null) return;

        String patientNom = valeurs[0];
        String dateStr = valeurs[1];
        String heureStr = valeurs[2];
        String description = valeurs[3];

        String formatError = validateRendezVousFormat(patientNom, dateStr, heureStr, description);
        if (formatError != null) {
            vue.marquerChampErreur(formatError.contains("patient") ? "patient" : formatError.contains("date") ? "date" : formatError.contains("heure") ? "heure" : "description");
            vue.afficherErreur(formatError);
            return;
        }

        String valueError = validateRendezVousValues(patientNom, dateStr, heureStr);
        if (valueError != null) {
            vue.marquerChampErreur(valueError.contains("patient") ? "patient" : valueError.contains("date") ? "date" : valueError.contains("heure") ? "heure" : "");
            vue.afficherErreur(valueError);
            return;
        }

        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
        LocalDate date = LocalDate.parse(dateStr, dateFormatter);
        LocalTime heure = LocalTime.parse(heureStr, timeFormatter);

        if (hasTimeConflict(date, heure, -1)) {
            vue.marquerChampErreur("heure");
            vue.afficherErreur("Ce créneau est déjà pris. Chaque rendez-vous dure 30 minutes.");
            return;
        }

        try {
            int idPatient = 0;
            for (Patient p : gestionnairePatients.getTous()) {
                if (p.getNom().equalsIgnoreCase(patientNom)) {
                    idPatient = p.getId();
                    break;
                }
            }
            if (idPatient == 0) {
                vue.afficherErreur("Patient non trouvé : " + patientNom);
                return;
            }

            RendezVous newRendezVous = new RendezVous(0,  patientNom, date, heure, description);
            gestionnaire.ajouter(newRendezVous);
            mettreAJourVue();
            medcin.mettreAJourTableauDeBord();
            vue.afficherMessageSucces("Rendez-vous ajouté avec succès.");
        } catch (RuntimeException e) {
            vue.afficherErreur("Erreur lors de l'ajout du rendez-vous : " + e.getMessage());
        }
    }

    public void modifierRendezVous() {
        int id = vue.getSelectedRendezVousId();
        if (id == -1) {
            vue.afficherErreur("Veuillez sélectionner un rendez-vous à modifier.");
            return;
        }

        RendezVous rendezVous = gestionnaire.getRendezVousById(id);
        if (rendezVous == null) {
            vue.afficherErreur("Rendez-vous non trouvé (ID: " + id + ").");
            return;
        }

        String[] valeurs = vue.afficherFormulaireModification(rendezVous);
        if (valeurs == null) return;

        String patientNom = valeurs[0];
        String dateStr = valeurs[1];
        String heureStr = valeurs[2];
        String description = valeurs[3];

        String formatError = validateRendezVousFormat(patientNom, dateStr, heureStr, description);
        if (formatError != null) {
            vue.marquerChampErreur(formatError.contains("patient") ? "patient" : formatError.contains("date") ? "date" : formatError.contains("heure") ? "heure" : "description");
            vue.afficherErreur(formatError);
            return;
        }

        String valueError = validateRendezVousValues(patientNom, dateStr, heureStr);
        if (valueError != null) {
            vue.marquerChampErreur(valueError.contains("patient") ? "patient" : valueError.contains("date") ? "date" : valueError.contains("heure") ? "heure" : "");
            vue.afficherErreur(valueError);
            return;
        }

        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
        LocalDate date = LocalDate.parse(dateStr, dateFormatter);
        LocalTime heure = LocalTime.parse(heureStr, timeFormatter);

        if (hasTimeConflict(date, heure, id)) {
            vue.marquerChampErreur("heure");
            vue.afficherErreur("Ce créneau est déjà pris. Chaque rendez-vous dure 30 minutes.");
            return;
        }

        try {
            int idPatient = 0;
            for (Patient p : gestionnairePatients.getTous()) {
                if (p.getNom().equalsIgnoreCase(patientNom)) {
                    idPatient = p.getId();
                    break;
                }
            }
            if (idPatient == 0) {
                vue.afficherErreur("Patient non trouvé : " + patientNom);
                return;
            }

            RendezVous updatedRendezVous = new RendezVous(id, patientNom, date, heure, description);
            gestionnaire.modifier(id, updatedRendezVous);
            mettreAJourVue();
            medcin.mettreAJourTableauDeBord();
            vue.afficherMessageSucces("Rendez-vous modifié avec succès.");
        } catch (RuntimeException e) {
            vue.afficherErreur("Erreur lors de la modification du rendez-vous : " + e.getMessage());
        }
    }

    public void supprimerRendezVous() {
        int id = vue.getSelectedRendezVousId();
        if (id == -1) {
            vue.afficherErreur("Veuillez sélectionner un rendez-vous à supprimer.");
            return;
        }

        int confirm = vue.demanderConfirmationSuppression();
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                gestionnaire.supprimer(id);
                mettreAJourVue();
                medcin.mettreAJourTableauDeBord();
            } catch (RuntimeException e) {
                vue.afficherErreur("Erreur lors de la suppression du rendez-vous : " + e.getMessage());
            }
        }
    }

    public void rechercherRendezVous(String text) {
        vue.filtrerTableau(text);
    }

    public void mettreAJourVue() {
        try {
            vue.mettreAJour(gestionnaire.getTous());
        } catch (RuntimeException e) {
            vue.afficherErreur("Erreur lors de la mise à jour de la vue : " + e.getMessage());
        }
    }
}
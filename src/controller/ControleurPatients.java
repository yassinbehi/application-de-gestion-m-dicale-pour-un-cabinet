package controller;
import model.FicheMedicale;
import model.GestionnairePatients;
import model.GestionnaireRendezVous;
import model.Patient;
import vue.VuePatients;

import javax.swing.*;
import java.awt.*;
import vue.Accueil;

public class ControleurPatients {
    private final VuePatients vue;
    private final GestionnairePatients gestionnaire;
    private final GestionnaireRendezVous gestionRdv;
    private final Accueil medcin;
    private final Color ERROR_COLOR = new Color(255, 200, 200);

    public ControleurPatients(VuePatients vue, GestionnairePatients gestionnaire, GestionnaireRendezVous gestionRdv, Accueil medcin) {
        this.vue = vue;
        this.gestionnaire = gestionnaire;
        this.gestionRdv = gestionRdv;
        this.medcin = medcin;

        vue.ajouterListenerAjouter(e -> ajouterPatient());
        vue.ajouterListenerModifier(e -> modifierPatient());
        vue.ajouterListenerSupprimer(e -> supprimerPatient());
        vue.ajouterListenerRechercher(e -> rechercherPatient(vue.getSearchText()));

        mettreAJourVue();
    }

    private String validatePatientFormat(String nom, String ageText, String telephone, String adresse) {
        if (nom == null || nom.trim().isEmpty()) return "Le nom ne peut pas être vide.";
        if (!nom.matches("[a-zA-Z\\s]+")) return "Le nom ne doit contenir que des lettres et des espaces.";
        if (ageText == null || ageText.trim().isEmpty()) return "L'âge ne peut pas être vide.";
        try {
            Integer.parseInt(ageText);
        } catch (NumberFormatException e) {
            return "L'âge doit être un nombre valide.";
        }
        if (telephone == null || telephone.trim().isEmpty()) return "Le numéro de téléphone ne peut pas être vide.";
        if (!telephone.matches("\\d{8}")) return "Le numéro de téléphone doit contenir exactement 8 chiffres."; // Ajusté pour varchar
        if (adresse == null || adresse.trim().isEmpty()) return "L'adresse ne peut pas être vide.";
        return null;
    }

    private String validatePatientValues(String ageText) {
        try {
            int age = Integer.parseInt(ageText);
            if (age <= 0 || age > 120) return "L'âge doit être un nombre entre 1 et 120.";
            return null;
        } catch (NumberFormatException e) {
            return "L'âge doit être un nombre valide.";
        }
    }

    public void ajouterPatient() {
        String[] valeurs = vue.afficherFormulaireAjout();
        if (valeurs == null) return;

        String nom = valeurs[0];
        String ageText = valeurs[1];
        String telephone = valeurs[2];
        String adresse = valeurs[3];

        String formatError = validatePatientFormat(nom, ageText, telephone, adresse);
        if (formatError != null) {
            vue.marquerChampErreur(formatError.contains("nom") ? "nom" : formatError.contains("âge") ? "age" : formatError.contains("téléphone") ? "telephone" : "adresse");
            vue.afficherErreur(formatError);
            return;
        }

        String valueError = validatePatientValues(ageText);
        if (valueError != null) {
            vue.marquerChampErreur("age");
            vue.afficherErreur(valueError);
            return;
        }

        try {
            int age = Integer.parseInt(ageText);
            FicheMedicale fiche = new FicheMedicale("historiqueMedical","allergies","traitements","notes");
            Patient newPatient = new Patient(1, nom, age, telephone, adresse,fiche);
            gestionnaire.ajouter(newPatient);
            mettreAJourVue();
            medcin.mettreAJourTableauDeBord();
            vue.afficherMessageSucces("Patient ajouté avec succès.");
        } catch (RuntimeException e) {
            vue.afficherErreur("Erreur lors de l'ajout du patient : " + e.getMessage());
        }
    }

    public void modifierPatient() {
        int id = vue.getSelectedPatientId();
        if (id == -1) {
            vue.afficherErreur("Veuillez sélectionner un patient à modifier.");
            return;
        }

        Patient patient = gestionnaire.getPatientById(id);
        if (patient == null) {
            vue.afficherErreur("Patient non trouvé (ID: " + id + ").");
            return;
        }

        String[] valeurs = vue.afficherFormulaireModification(patient);
        if (valeurs == null) return;

        String nom = valeurs[0];
        String ageText = valeurs[1];
        String telephone = valeurs[2];
        String adresse = valeurs[3];

        String formatError = validatePatientFormat(nom, ageText, telephone, adresse);
        if (formatError != null) {
            vue.marquerChampErreur(formatError.contains("nom") ? "nom" : formatError.contains("âge") ? "age" : formatError.contains("téléphone") ? "telephone" : "adresse");
            vue.afficherErreur(formatError);
            return;
        }

        String valueError = validatePatientValues(ageText);
        if (valueError != null) {
            vue.marquerChampErreur("age");
            vue.afficherErreur(valueError);
            return;
        }

        try {
            int age = Integer.parseInt(ageText);
            Patient updatedPatient = new Patient(id, nom, age, telephone, adresse, patient.getFicheMedicale());
            gestionnaire.modifier(id, updatedPatient);
            mettreAJourVue();
            medcin.mettreAJourTableauDeBord();
            vue.afficherMessageSucces("Patient modifié avec succès.");
        } catch (RuntimeException e) {
            vue.afficherErreur("Erreur lors de la modification du patient : " + e.getMessage());
        }
    }

    public void supprimerPatient() {
        int id = vue.getSelectedPatientId();
        if (id == -1) {
            vue.afficherErreur("Veuillez sélectionner un patient à supprimer.");
            return;
        }

        int confirm = vue.demanderConfirmationSuppression();
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                Patient patient = gestionnaire.getPatientById(id);
                if (patient != null) {
                    gestionRdv.supprimerParPatient(patient.getNom());
                    gestionnaire.supprimer(id);
                    mettreAJourVue();
                    medcin.mettreAJourTableauDeBord();
                } else {
                    vue.afficherErreur("Patient non trouvé (ID: " + id + ").");
                }
            } catch (RuntimeException e) {
                vue.afficherErreur("Erreur lors de la suppression du patient : " + e.getMessage());
            }
        }
    }

    public void rechercherPatient(String text) {
        vue.filtrerTableau(text);
    }

    public void mettreAJourVue() {
        try {
            vue.mettreAJour(gestionnaire.getTous());
        } catch (RuntimeException e) {
            vue.afficherErreur("Erreur lors de la mise à jour de la vue : " + e.getMessage());
        }
    }

    public void afficherFichePatient(int id) {
        Patient patient = gestionnaire.getPatientById(id);
        if (patient != null) {
            vue.showFichePatient(patient);
        } else {
            vue.afficherErreur("Patient non trouvé (ID: " + id + ").");
        }
    }
}
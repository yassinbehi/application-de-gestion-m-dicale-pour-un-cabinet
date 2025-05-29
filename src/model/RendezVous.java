package model;

import java.time.LocalDate;
import java.time.LocalTime;

public class RendezVous {
    private int id;
    private int idPatient; // Nouveau champ
    private String patientNom; // Conservé pour l'affichage
    private LocalDate date; // Changé de String à LocalDate
    private LocalTime heure; // Changé de String à LocalTime
    private String description;

    public RendezVous(int id, String patientNom, LocalDate date, LocalTime heure, String description) {
        this.id = id;
        this.patientNom = patientNom;
        this.date = date;
        this.heure = heure;
        this.description = description;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    
    public String getPatientNom() {
        return patientNom;
    }

    public void setPatientNom(String patientNom) {
        this.patientNom = patientNom;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public LocalTime getHeure() {
        return heure;
    }

    public void setHeure(LocalTime heure) {
        this.heure = heure;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

  
}
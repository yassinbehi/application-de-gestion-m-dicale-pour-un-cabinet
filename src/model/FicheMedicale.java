package model;

public class FicheMedicale {
    private String historiqueMedical;
    private String allergies;
    private String traitements;
    private String notes;

    public FicheMedicale(String historiqueMedical, String allergies, String traitements, String notes) {
        this.historiqueMedical = historiqueMedical;
        this.allergies = allergies;
        this.traitements = traitements;
        this.notes = notes;
    }

    public String getHistoriqueMedical() {
        return historiqueMedical;
    }

    public void setHistoriqueMedical(String historiqueMedical) {
        this.historiqueMedical = historiqueMedical;
    }

    public String getAllergies() {
        return allergies;
    }

    public void setAllergies(String allergies) {
        this.allergies = allergies;
    }

    public String getTraitements() {
        return traitements;
    }

    public void setTraitements(String traitements) {
        this.traitements = traitements;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    
}
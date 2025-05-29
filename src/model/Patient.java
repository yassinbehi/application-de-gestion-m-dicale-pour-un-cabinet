package model;

public class Patient {
    private int id;
    private String nom;
    private int age;
    private String telephone;
    private String adresse;
    private FicheMedicale ficheMedicale;

    public Patient(int id, String nom, int age, String telephone, String adresse) {
        this.id = id;
        this.nom = nom;
        this.age = age;
        this.telephone = telephone;
        this.adresse = adresse;
        this.ficheMedicale = new FicheMedicale("", "", "", "");
    }

    public Patient(int id, String nom, int age, String telephone, String adresse, FicheMedicale ficheMedicale) {
        this.id = id;
        this.nom = nom;
        this.age = age;
        this.telephone = telephone;
        this.adresse = adresse;
        this.ficheMedicale = ficheMedicale;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public String getAdresse() {
        return adresse;
    }

    public void setAdresse(String adresse) {
        this.adresse = adresse;
    }

    public FicheMedicale getFicheMedicale() {
        return ficheMedicale;
    }

    public void setFicheMedicale(FicheMedicale ficheMedicale) {
        this.ficheMedicale = ficheMedicale;
    }

    
}
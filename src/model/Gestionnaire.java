package model;

import java.util.List;

public interface Gestionnaire<T extends Entite> {
    void ajouter(T entite);          // Ajoute une nouvelle entité
    void modifier(int id, T entite); // Modifie une entité existante
    void supprimer(int id);          // Supprime une entité par ID
    T trouverParId(int id);          // Trouve une entité par ID
    List<T> getTous();               // Retourne toutes les entités
}